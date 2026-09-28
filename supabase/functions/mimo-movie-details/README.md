# Mimo movie details (draft, not deployed)

Apply the streaming migrations first, then the details migration. Deploy only after reviewing live migration versions and the provider key `MOVIE_OF_THE_NIGHT_API_KEY` in Supabase secrets. No TMDB key is required or used. Invoke with a user JWT and `{ "id": "tt0068646", "country": "IN" }`.

Shared MotN global-month 900 reserve across streaming and details. Per-user details request 40/hour, provider misses 12/hour, successful MotN cache TTL 24h; transient or Wikidata-only fallback is not cached. Reserved failed upstream calls still consume budget. Wikipedia is link-only; artwork and synopsis remain null because provider's commercial API grant explicitly excludes third-party content. Wikidata fallback is CC0 and matched on exact IMDb/movie identifier; TV lacks a checked property and may return 404. Trailer action opens an official YouTube search, not a verified trailer result. Android must open externally and must not imply trailer identity. No data here enters AI. Attribution should be visible and linked. No card or paid provider tier.

Source terms: https://developers.movieofthenight.com/terms-and-conditions ; https://www.movieofthenight.com/about/api/pricing ; https://www.wikidata.org/wiki/Wikidata:Licensing .
