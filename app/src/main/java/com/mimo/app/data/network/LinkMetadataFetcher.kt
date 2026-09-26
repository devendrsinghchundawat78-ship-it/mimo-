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
            "facebookexternalhit/1.1 (+http://www.facebook.com/externalhit_uatext.php)"
        } else {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        }

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", userAgent)
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .header("Accept-Language", "en-US,en;q=0.9")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return ""
            return response.body?.string() ?: ""
        }
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
