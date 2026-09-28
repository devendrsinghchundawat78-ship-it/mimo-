package com.mimo.app.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URI
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class ParsedMetadata(
    val title: String,
    val description: String,
    val imageUrl: String?,
    val videoUrl: String? = null,
    val platform: String,
    val canonicalUrl: String
)

object LinkMetadataFetcher {

    private const val UA_DESKTOP = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    private const val UA_WHATSAPP = "WhatsApp/2.23.23.78 i"
    private const val UA_FACEBOOK_EXTERNAL_HIT = "facebookexternalhit/1.1 (+http://www.facebook.com/externalhit_uatext.php)"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    suspend fun fetch(rawUrl: String): ParsedMetadata = withContext(Dispatchers.IO) {
        var cleanUrl = rawUrl.trim()
        if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
            cleanUrl = "https://$cleanUrl"
        }

        val platform = detectPlatform(cleanUrl)

        // YouTube thumbnail optimization
        if (platform == "YouTube") {
            val ytVideoId = extractYoutubeId(cleanUrl)
            if (ytVideoId != null) {
                val thumb = "https://img.youtube.com/vi/$ytVideoId/hqdefault.jpg"
                try {
                    val html = fetchHtml(cleanUrl, platform)
                    val title = extractTag(html, "og:title") ?: extractTitleTag(html) ?: "YouTube Video"
                    val desc = extractTag(html, "og:description") ?: cleanUrl
                    return@withContext ParsedMetadata(
                        title = unescape(title),
                        description = unescape(desc),
                        imageUrl = thumb,
                        platform = platform,
                        canonicalUrl = cleanUrl
                    )
                } catch (_: Exception) {
                    return@withContext ParsedMetadata(
                        title = "YouTube Video",
                        description = cleanUrl,
                        imageUrl = thumb,
                        platform = platform,
                        canonicalUrl = cleanUrl
                    )
                }
            }
        }

        // Instagram Reels & Posts optimization
        if (platform == "Instagram") {
            val shortcode = extractInstagramShortcode(cleanUrl)
            if (shortcode != null) {
                val canonical = "https://www.instagram.com/reel/$shortcode/"
                try {
                    // Strategy 1: Embed endpoint (public, fast, returns EmbeddedMediaImage and og:image)
                    val embedUrl = "https://www.instagram.com/p/$shortcode/embed/captioned/"
                    val embedHtml = fetchHtmlWithUA(embedUrl, UA_DESKTOP)
                    var image = extractInstagramThumbnail(embedHtml)
                    var title = extractInstagramTitle(embedHtml) ?: extractTag(embedHtml, "og:title")
                    var desc = extractTag(embedHtml, "og:description") ?: ""

                    // Strategy 2: Direct reel with WhatsApp preview bot user agent (Meta whitelisted)
                    if (image.isNullOrBlank()) {
                        val directHtml = fetchHtmlWithUA(canonical, UA_WHATSAPP)
                        val directImg = extractInstagramThumbnail(directHtml)
                        if (!directImg.isNullOrBlank()) {
                            image = directImg
                        }
                        if (title.isNullOrBlank()) {
                            title = extractInstagramTitle(directHtml) ?: extractTag(directHtml, "og:title")
                        }
                        if (desc.isBlank()) {
                            desc = extractTag(directHtml, "og:description") ?: ""
                        }
                    }

                    // Strategy 3: Facebook crawler user agent
                    if (image.isNullOrBlank()) {
                        val fbHtml = fetchHtmlWithUA("https://www.instagram.com/p/$shortcode/", UA_FACEBOOK_EXTERNAL_HIT)
                        val fbImg = extractInstagramThumbnail(fbHtml)
                        if (!fbImg.isNullOrBlank()) {
                            image = fbImg
                        }
                        if (title.isNullOrBlank()) {
                            title = extractInstagramTitle(fbHtml) ?: extractTag(fbHtml, "og:title")
                        }
                    }

                    val finalTitle = title?.let { cleanInstagramTitle(it) } ?: "Instagram Reel"
                    val finalDesc = if (desc.isNotBlank()) desc else "Instagram Reel • $shortcode"
                    // Instagram often denies anonymous image fetches. Do not save a guessed
                    // media URL as a thumbnail: a broken URL creates blank cards in the library.
                    val fallbackImg = image?.takeIf { it.startsWith("https://") }

                    return@withContext ParsedMetadata(
                        title = unescape(finalTitle),
                        description = unescape(finalDesc),
                        imageUrl = fallbackImg,
                        videoUrl = null,
                        platform = "Instagram",
                        canonicalUrl = canonical
                    )
                } catch (_: Exception) {
                    return@withContext ParsedMetadata(
                        title = "Instagram Reel",
                        description = cleanUrl,
                        imageUrl = null,
                        platform = "Instagram",
                        canonicalUrl = canonical
                    )
                }
            }
        }

        try {
            val html = fetchHtml(cleanUrl, platform)

            val title = extractTag(html, "og:title")
                ?: extractTag(html, "twitter:title")
                ?: extractTitleTag(html)
                ?: "$platform Content"

            val description = extractTag(html, "og:description")
                ?: extractTag(html, "description")
                ?: extractTag(html, "twitter:description")
                ?: ""

            var image = extractTag(html, "og:image")
                ?: extractTag(html, "twitter:image")
                ?: extractTag(html, "og:image:url")

            // Fix relative image URLs
            if (image != null && !image.startsWith("http")) {
                image = try {
                    val baseUri = URI(cleanUrl)
                    baseUri.resolve(image).toString()
                } catch (_: Exception) {
                    null
                }
            }

            var video = extractTag(html, "og:video")
                ?: extractTag(html, "og:video:secure_url")
                ?: extractTag(html, "og:video:url")
                ?: extractTag(html, "twitter:player:stream")

            if (video == null && (cleanUrl.endsWith(".mp4") || cleanUrl.endsWith(".webm") || cleanUrl.contains(".mp4?"))) {
                video = cleanUrl
            }

            ParsedMetadata(
                title = unescape(title),
                description = unescape(description),
                imageUrl = image,
                videoUrl = video,
                platform = platform,
                canonicalUrl = cleanUrl
            )
        } catch (_: Exception) {
            // Graceful fallback from URL structure
            ParsedMetadata(
                title = "$platform Link",
                description = cleanUrl,
                imageUrl = null,
                platform = platform,
                canonicalUrl = cleanUrl
            )
        }
    }

    private fun fetchHtml(url: String, platform: String): String {
        val userAgent = if (platform == "Instagram" || platform == "TikTok" || platform == "Facebook") {
            UA_FACEBOOK_EXTERNAL_HIT
        } else {
            UA_DESKTOP
        }
        return fetchHtmlWithUA(url, userAgent)
    }

    private fun fetchHtmlWithUA(url: String, userAgent: String): String {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", userAgent)
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
            .header("Accept-Language", "en-US,en;q=0.9")
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) "" else response.body?.string() ?: ""
            }
        } catch (_: Exception) {
            ""
        }
    }

    private fun extractInstagramShortcode(url: String): String? {
        val pattern = Pattern.compile("/(?:reel|reels|p|tv|share/reel)/([A-Za-z0-9_-]+)")
        val matcher = pattern.matcher(url)
        return if (matcher.find()) matcher.group(1) else null
    }

    private fun extractInstagramThumbnail(html: String): String? {
        if (html.isBlank()) return null

        // 1. Meta og:image or twitter:image
        val ogImage = extractTag(html, "og:image")
            ?: extractTag(html, "og:image:secure_url")
            ?: extractTag(html, "twitter:image")
        if (!ogImage.isNullOrBlank()) {
            val unescaped = unescapeUrl(ogImage)
            if (isValidMediaUrl(unescaped)) return unescaped
        }

        // 2. EmbeddedMediaImage tag
        val imgClassPattern = Pattern.compile("<img[^>]+class=[\"'][^\"']*EmbeddedMediaImage[^\"']*[\"'][^>]+src=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE)
        val m1 = imgClassPattern.matcher(html)
        if (m1.find()) {
            val raw = m1.group(1)
            if (!raw.isNullOrBlank()) {
                val u = unescapeUrl(raw)
                if (isValidMediaUrl(u)) return u
            }
        }

        val imgClassPatternRev = Pattern.compile("<img[^>]+src=[\"']([^\"']+)[\"'][^>]+class=[\"'][^\"']*EmbeddedMediaImage[^\"']*[\"']", Pattern.CASE_INSENSITIVE)
        val m2 = imgClassPatternRev.matcher(html)
        if (m2.find()) {
            val raw = m2.group(1)
            if (!raw.isNullOrBlank()) {
                val u = unescapeUrl(raw)
                if (isValidMediaUrl(u)) return u
            }
        }

        // 3. JSON fields: "display_url", "thumbnail_src", "thumbnail_url"
        val jsonPattern = Pattern.compile("\"(?:display_url|thumbnail_src|thumbnail_url)\"\\s*:\\s*\"([^\"]+)\"", Pattern.CASE_INSENSITIVE)
        val m3 = jsonPattern.matcher(html)
        if (m3.find()) {
            val raw = m3.group(1)
            if (!raw.isNullOrBlank()) {
                val u = unescapeUrl(raw)
                if (isValidMediaUrl(u)) return u
            }
        }

        // 4. Any CDN image in scontent or cdninstagram
        val cdnPattern = Pattern.compile("(https?:[/\\\\]+[^\"'\\s<>()]+(?:scontent|cdninstagram)[^\"'\\s<>()]+\\.(?:jpg|jpeg|webp|png)[^\"'\\s<>()]*)", Pattern.CASE_INSENSITIVE)
        val m4 = cdnPattern.matcher(html)
        while (m4.find()) {
            val raw = m4.group(1)
            if (!raw.isNullOrBlank()) {
                val u = unescapeUrl(raw)
                if (isValidMediaUrl(u)) return u
            }
        }

        return null
    }

    private fun extractInstagramTitle(html: String): String? {
        val og = extractTag(html, "og:title")
        if (!og.isNullOrBlank()) return og
        val captionPattern = Pattern.compile("<div[^>]+class=[\"'][^\"']*Caption[^\"']*[\"'][^>]*>(.*?)</div>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        val m = captionPattern.matcher(html)
        if (m.find()) {
            val group = m.group(1)
            if (!group.isNullOrBlank()) {
                val text = group.replace(Regex("<[^>]*>"), " ").trim()
                if (text.isNotBlank()) return text
            }
        }
        return extractTitleTag(html)
    }

    private fun cleanInstagramTitle(title: String): String {
        var clean = title.trim()
        val regex = Pattern.compile("^(?:.*?on Instagram:\\s*[\"']?)(.+?)[\"']?$", Pattern.CASE_INSENSITIVE)
        val m = regex.matcher(clean)
        if (m.find()) {
            val group = m.group(1)
            if (!group.isNullOrBlank()) {
                clean = group.trim()
            }
        }
        return clean.ifBlank { "Instagram Reel" }
    }

    private fun isValidMediaUrl(url: String): Boolean {
        if (url.isBlank()) return false
        if (url.startsWith("data:")) return false
        if (url.contains("rsrc.php")) return false
        if (url.contains("static.cdninstagram.com")) return false
        return url.startsWith("http://") || url.startsWith("https://")
    }

    private fun unescapeUrl(url: String): String {
        return url
            .replace("\\/", "/")
            .replace("\\u0026", "&")
            .replace("&amp;", "&")
            .replace("&#038;", "&")
            .trim()
    }

    private fun detectPlatform(url: String): String {
        val lower = url.lowercase()
        return when {
            lower.contains("instagram.com") || lower.contains("instagr.am") -> "Instagram"
            lower.contains("tiktok.com") -> "TikTok"
            lower.contains("youtube.com") || lower.contains("youtu.be") -> "YouTube"
            lower.contains("twitter.com") || lower.contains("x.com") -> "X"
            lower.contains("github.com") -> "GitHub"
            lower.contains("medium.com") -> "Medium"
            lower.contains("reddit.com") -> "Reddit"
            else -> "Web"
        }
    }

    private fun extractYoutubeId(url: String): String? {
        val pattern = "(?<=watch\\?v=|/videos/|embed/|youtu.be/|/v/|/e/|watch\\?feature=player_embedded&v=)[^#&?]*"
        val compiled = Pattern.compile(pattern)
        val matcher = compiled.matcher(url)
        return if (matcher.find()) matcher.group() else null
    }

    private fun extractTag(html: String, property: String): String? {
        // Pattern 1: property="..." content="..."
        val p1 = Pattern.compile(
            "<meta\\s+[^>]*?(?:property|name)=[\"']" + Pattern.quote(property) + "[\"'][^>]*?content=[\"']([^\"']*)[\"']",
            Pattern.CASE_INSENSITIVE
        )
        val m1 = p1.matcher(html)
        if (m1.find()) return m1.group(1)

        // Pattern 2: content="..." property="..."
        val p2 = Pattern.compile(
            "<meta\\s+[^>]*?content=[\"']([^\"']*)[\"'][^>]*?(?:property|name)=[\"']" + Pattern.quote(property) + "[\"']",
            Pattern.CASE_INSENSITIVE
        )
        val m2 = p2.matcher(html)
        if (m2.find()) return m2.group(1)

        return null
    }

    private fun extractTitleTag(html: String): String? {
        val p = Pattern.compile("<title[^>]*>([^<]*)</title>", Pattern.CASE_INSENSITIVE)
        val m = p.matcher(html)
        return if (m.find()) m.group(1) else null
    }

    private fun unescape(input: String): String {
        return input
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#039;", "'")
            .replace("&#39;", "'")
            .replace("&#064;", "@")
            .replace("&#x2022;", "•")
            .replace("&middot;", "·")
            .trim()
    }
}
