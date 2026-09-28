// Authenticated, quota-limited movie/show availability. Provider key stays server-only.
const json = (value: unknown, status = 200) => new Response(JSON.stringify(value), {
  status, headers: {'content-type':'application/json; charset=utf-8', 'cache-control':'no-store', 'x-content-type-options':'nosniff'}
});
const validId = (s: unknown): s is string => typeof s === 'string' && /^(tt[0-9]{7,12}|(movie|tv)\/[0-9]{1,12})$/.test(s);
const validCountry = (s: unknown): s is string => typeof s === 'string' && /^[a-zA-Z]{2}$/.test(s);
const attribution = {text:'Streaming availability information provided by Streaming Availability API by Movie of the Night', url:'https://www.movieofthenight.com/about/api'};
function linksFor(data: any, country: string) {
  const options = data?.streamingOptions?.[country];
  if (!Array.isArray(options)) return [];
  return options.slice(0,60).flatMap((option: any) => {
    const name = option?.service?.name, type = option?.type, rawLink = option?.link;
    if (typeof name !== 'string' || !name.trim() || name.length > 100 ||
      !['subscription','free','rent','buy','addon'].includes(type) ||
      typeof rawLink !== 'string' || rawLink.length > 2048) return [];
    try {
      const url = new URL(rawLink);
      if (url.protocol !== 'https:' || url.username || url.password || url.port) return [];
      return [{service:name, type, url:url.href}];
    } catch { return []; }
  });
}
Deno.serve(async req => {
  if (req.method !== 'POST') return json({error:'POST only'},405);
  const length = Number(req.headers.get('content-length') || 0);
  if (length > 2048) return json({error:'Request too large'},413);
  const authHeader = req.headers.get('authorization') || '';
  const base = Deno.env.get('SUPABASE_URL');
  const anon = Deno.env.get('SUPABASE_ANON_KEY');
  const service = Deno.env.get('SUPABASE_SERVICE_ROLE_KEY');
  const providerKey = Deno.env.get('MOVIE_OF_THE_NIGHT_API_KEY');
  if (!base || !anon || !service || !providerKey) return json({error:'Service unavailable'},503);
  if (!/^Bearer [A-Za-z0-9._~-]+$/.test(authHeader)) return json({error:'Unauthorized'},401);
  let user: any;
  try {
    const r = await fetch(base+'/auth/v1/user', {headers:{authorization:authHeader, apikey:anon}, signal:AbortSignal.timeout(4000)});
    if (!r.ok) return json({error:'Unauthorized'},401);
    user = await r.json();
    if (typeof user.id !== 'string' || !/^[a-f0-9-]{36}$/.test(user.id)) return json({error:'Unauthorized'},401);
  } catch {return json({error:'Auth unavailable'},503);}
  let input: any;
  try {
    const text = await req.text();
    if (text.length > 2048) return json({error:'Request too large'},413);
    input = JSON.parse(text);
  } catch {return json({error:'Invalid JSON'},400);}
  if (!validId(input?.id) || !validCountry(input?.country)) return json({error:'Valid IMDb/TMDB ID and country required'},400);
  const id = input.id, country = input.country.toLowerCase();
  const serviceHeaders = {'content-type':'application/json',apikey:service,authorization:'Bearer '+service};
  let reservation: any;
  try {
    const r = await fetch(base+'/rest/v1/rpc/mimo_streaming_reserve', {
      method:'POST',headers:serviceHeaders,body:JSON.stringify({p_user:user.id,p_lookup:id,p_country:country.toUpperCase()}),signal:AbortSignal.timeout(4500)
    });
    if (!r.ok) throw Error('reserve failed');
    reservation = await r.json();
  } catch {return json({error:'Quota check unavailable'},503);}
  if (reservation?.state === 'limited') return json({error:'Rate or free quota limit reached',retryAfter:reservation.retryAfter},429);
  if (reservation?.state === 'invalid') return json({error:'Invalid lookup'},400);
  if (reservation?.state === 'cached') return json({...reservation.value, attribution, cached:true});
  if (reservation?.state !== 'reserved') return json({error:'Quota check unavailable'},503);
  let source: Response;
  try {
    // The slash is only allowed for the documented, validated TMDB movie/tv ID.
    const uri = 'https://api.movieofthenight.com/v4/shows/'+id+'?country='+country+'&series_granularity=show';
    source = await fetch(uri, {headers:{'X-API-Key':providerKey},redirect:'error',signal:AbortSignal.timeout(6000)});
    if (source.status === 404) return json({error:'Show not found'},404);
    if (source.status === 429) return json({error:'Provider limit reached'},503);
    if (!source.ok || !source.headers.get('content-type')?.includes('json')) throw Error('provider failed');
    if (Number(source.headers.get('content-length')||0) > 131072) throw Error('oversized');
    const text = await source.text();
    if (text.length > 131072) throw Error('oversized');
    const show = JSON.parse(text);
    if (show?.imdbId !== id && show?.tmdbId !== id) throw Error('ID mismatch');
    const result = {id, country:country.toUpperCase(), title:typeof show.title === 'string' ? show.title.slice(0,240) : '',
      releaseYear:Number.isInteger(show.releaseYear)?show.releaseYear:null,
      options:linksFor(show,country), attribution};
    // A cache failure costs one reserved call but cannot bypass budget checks.
    try { await fetch(base+'/rest/v1/rpc/mimo_streaming_cache_put',{
      method:'POST', headers:serviceHeaders,
      body:JSON.stringify({p_lookup:id,p_country:country.toUpperCase(),p_response:result}),signal:AbortSignal.timeout(3000)
    }); } catch { /* no cache; next lookup still needs another reservation */ }
    return json(result);
  } catch {return json({error:'Streaming source unavailable'},502);}
});
