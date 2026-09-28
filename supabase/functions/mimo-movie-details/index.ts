// Source-only draft. Do not pass any provider data into Mimo AI.
const json = (v: unknown, status=200) => new Response(JSON.stringify(v), {status,headers:{'content-type':'application/json; charset=utf-8','cache-control':'no-store','x-content-type-options':'nosniff'}});
const validId = (x:unknown):x is string => typeof x==='string' && /^(tt[0-9]{7,12}|(movie|tv)\/[0-9]{1,12})$/.test(x);
const validUuid = (x:unknown):x is string => typeof x==='string' && /^[0-9a-f]{8}-[0-9a-f]{4}-[1-8][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(x);
const validCountry = (x:unknown):x is string => typeof x==='string' && /^[A-Za-z]{2}$/.test(x);
const small=(x:unknown,n=160) => typeof x==='string' && x.length<=n ? x.trim():null;
const sourceCredit={text:'Streaming availability information provided by Streaming Availability API by Movie of the Night',url:'https://www.movieofthenight.com/about/api'};
function links(show:any,country:string) {
 const options=show?.streamingOptions?.[country]; if(!Array.isArray(options)) return [];
 return options.slice(0,50).flatMap((o:any)=>{
  if(typeof o?.service?.name!=='string'||o.service.name.length>100||!['subscription','free','rent','buy','addon'].includes(o.type)||typeof o.link!=='string'||o.link.length>2048) return [];
  try {const u=new URL(o.link); return u.protocol==='https:'&&!u.username&&!u.password&&!u.port ? [{service:o.service.name,type:o.type,url:u.href}]:[];}catch{return [];}
 });
}
function trailerAction(title:string,year:number|null) {
 // No scraped results or guessed video ID. User opens search in YouTube itself.
 return {label:'Find trailer on YouTube',url:'https://www.youtube.com/results?search_query='+encodeURIComponent(`${title} ${year||''} official trailer`.trim()),kind:'external-search'};
}
function movieData(show:any,id:string,country:string) {
 const title=small(show?.title,240); if(!title) return null;
 const year=Number.isInteger(show.releaseYear)&&show.releaseYear>=1880&&show.releaseYear<=2200?show.releaseYear:null;
 const list=(xs:any,limit:number)=>Array.isArray(xs)?xs.slice(0,limit).map((x:any)=>small(typeof x==='string'?x:x?.name,100)).filter(Boolean):[];
 const runtime=Number.isInteger(show.runtime)&&show.runtime>0&&show.runtime<1000?show.runtime:null;
 const rating=Number.isInteger(show.rating)&&show.rating>=0&&show.rating<=100?show.rating:null;
 return {id,country:country.toUpperCase(),source:'movie-of-the-night',title,releaseYear:year,runtimeMinutes:runtime,rating,
  genres:list(show.genres,12),directors:list(show.directors,12),cast:list(show.cast,30),streamingOptions:links(show,country),
  synopsis:null,poster:null,artworkStatus:'No licensed artwork selected',wikipediaUrl:null,trailer:trailerAction(title,year),attribution:sourceCredit};
}
const query=(params:Record<string,string>)=>'https://www.wikidata.org/w/api.php?'+new URLSearchParams({...params,format:'json',origin:'*'});
async function wikidata(id:string) {
 const property=id.startsWith('tt')?'P345':id.startsWith('movie/')?'P4947':null;
 if(!property) return null; // no independently verified TV identifier mapping
 const value=id.startsWith('movie/')?id.split('/')[1]:id;
 const url=query({action:'query',list:'search',srsearch:`haswbstatement:${property}=${value}`,srnamespace:'0',srlimit:'5'});
 const r=await fetch(url,{headers:{'User-Agent':'Mimo/0.1 (https://getmimo.netlify.app/; devendrsingh9772733496@gmail.com)'},signal:AbortSignal.timeout(3500)});
 if(!r.ok||Number(r.headers.get('content-length')||0)>65536) return null;
 const searchText=await r.text();if(searchText.length>65536)return null;const data=JSON.parse(searchText);const ids=(data?.query?.search||[]).map((x:any)=>x?.title).filter((x:any)=>typeof x==='string'&&/^Q[1-9][0-9]*$/.test(x)).slice(0,5);
 if(!ids.length) return null;
 const er=await fetch(query({action:'wbgetentities',ids:ids.join('|'),props:'labels|descriptions|claims|sitelinks',languages:'en',sitefilter:'enwiki'}),{headers:{'User-Agent':'Mimo/0.1 (https://getmimo.netlify.app/; devendrsingh9772733496@gmail.com)'},signal:AbortSignal.timeout(3500)});
 if(!er.ok||Number(er.headers.get('content-length')||0)>1048576)return null;
 const raw=await er.text();if(raw.length>1048576)return null;const entities=JSON.parse(raw)?.entities||{};
 const matches=ids.map((key:string)=>entities[key]).filter((e:any)=>e?.claims?.[property]?.some((c:any)=>c?.mainsnak?.datavalue?.value===value));
 if(matches.length!==1) return null; // ambiguous or no exact identifier; never guess a film
 const e=matches[0],title=small(e?.labels?.en?.value,240); if(!title)return null;
 const date=e?.claims?.P577?.find((c:any)=>c?.mainsnak?.datavalue?.value?.time)?.mainsnak?.datavalue?.value?.time;
 const year=typeof date==='string'&&/^\+[0-9]{4}/.test(date)?Number(date.slice(1,5)):null;
 const page=e?.sitelinks?.enwiki?.title;
 const wikipediaUrl=typeof page==='string'&&page.length<300?'https://en.wikipedia.org/wiki/'+encodeURIComponent(page.replaceAll(' ','_')):null;
 return {id:e.id,title,releaseYear:year&&year>=1880&&year<=2200?year:null,wikipediaUrl};
}
Deno.serve(async req=>{
 if(req.method!=='POST')return json({error:'POST only'},405);
 if(Number(req.headers.get('content-length')||0)>512)return json({error:'Request too large'},413);
 const auth=req.headers.get('authorization')||'';
 if(!/^Bearer [A-Za-z0-9._~-]+$/.test(auth))return json({error:'Unauthorized'},401);
 const base=Deno.env.get('SUPABASE_URL'),anon=Deno.env.get('SUPABASE_ANON_KEY'),service=Deno.env.get('SUPABASE_SERVICE_ROLE_KEY'),providerKey=Deno.env.get('MOVIE_OF_THE_NIGHT_API_KEY');
 if(!base||!anon||!service||!providerKey)return json({error:'Service unavailable'},503);
 let user:any;
 try {const r=await fetch(base+'/auth/v1/user',{headers:{authorization:auth,apikey:anon},signal:AbortSignal.timeout(4000)});if(!r.ok)return json({error:'Unauthorized'},401);user=await r.json();if(!validUuid(user?.id))return json({error:'Unauthorized'},401);}catch{return json({error:'Auth unavailable'},503);}
 let input:any;try{const body=await req.text();if(body.length>512)return json({error:'Request too large'},413);input=JSON.parse(body);}catch{return json({error:'Invalid JSON'},400);}
 if(!input||Object.keys(input).some(k=>!['id','country'].includes(k))||!validId(input.id)||!validCountry(input.country))return json({error:'Valid IMDb/TMDB ID and country required'},400);
 const id=input.id,country=input.country.toLowerCase();const h={'content-type':'application/json',apikey:service,authorization:'Bearer '+service};
 let reservation:any;
 try{const r=await fetch(base+'/rest/v1/rpc/mimo_movie_details_reserve',{method:'POST',headers:h,body:JSON.stringify({p_user:user.id,p_lookup:id,p_country:country.toUpperCase()}),signal:AbortSignal.timeout(4500)});if(!r.ok)throw Error();reservation=await r.json();}catch{return json({error:'Quota check unavailable'},503);}
 if(reservation?.state==='invalid')return json({error:'Invalid ID'},400);
 if(reservation?.state==='limited')return json({error:'Request limit reached'},429);
 if(reservation?.state==='cached')return json({...reservation.value,cached:true});
 if(!['reserved','fallback'].includes(reservation?.state))return json({error:'Quota check unavailable'},503);
 let result:any=null;
 if(reservation.state==='reserved'){
  try{
   const r=await fetch('https://api.movieofthenight.com/v4/shows/'+id+'?country='+country+'&series_granularity=show',{headers:{'X-API-Key':providerKey},redirect:'error',signal:AbortSignal.timeout(6000)});
   if(r.ok&&r.headers.get('content-type')?.includes('json')&&Number(r.headers.get('content-length')||0)<=131072){const text=await r.text();if(text.length<=131072){const show=JSON.parse(text);if(show?.imdbId===id||show?.tmdbId===id)result=movieData(show,id,country);}}
  }catch{ /* fall through to CC0 facts */ }
 }
 // Wikidata is only a small, identifier-exact fallback or cross-check. No poster or article prose.
 let wd:any=null;try{wd=await wikidata(id);}catch{ /* neutral omission */ }
 if(result){if(wd&&result.releaseYear&&wd.releaseYear&&Math.abs(result.releaseYear-wd.releaseYear)<=1)result.wikipediaUrl=wd.wikipediaUrl;}
 else if(wd)result={id,country:country.toUpperCase(),source:'wikidata',title:wd.title,releaseYear:wd.releaseYear,runtimeMinutes:null,rating:null,genres:[],directors:[],cast:[],streamingOptions:[],synopsis:null,poster:null,artworkStatus:'No licensed artwork selected',wikipediaUrl:wd.wikipediaUrl,trailer:trailerAction(wd.title,wd.releaseYear),attribution:{text:'Structured facts from Wikidata (CC0)',url:'https://www.wikidata.org/wiki/'+wd.id}};
 if(!result)return json({error:'Movie details unavailable'},404);
 if(result.source==='movie-of-the-night')try{await fetch(base+'/rest/v1/rpc/mimo_movie_details_cache_put',{method:'POST',headers:h,body:JSON.stringify({p_lookup:id,p_country:country.toUpperCase(),p_response:result}),signal:AbortSignal.timeout(3000)});}catch{/* bounded reserve on next attempt */}
 return json({...result,cached:false});
});
