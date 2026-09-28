-- Source-only draft. Apply after the two streaming migrations; shares their 900/month provider budget.
create table if not exists mimo_private.movie_details_cache (
 lookup_key text primary key,
 response jsonb not null,
 expires_at timestamptz not null
);
alter table mimo_private.movie_details_cache enable row level security;
revoke all on mimo_private.movie_details_cache from public, anon, authenticated;
grant select, insert, update, delete on mimo_private.movie_details_cache to service_role;

create or replace function public.mimo_movie_details_reserve(p_user uuid, p_lookup text, p_country text)
returns jsonb language plpgsql security definer set search_path = pg_catalog, mimo_private
as $$
declare
 v_cache jsonb;
 v_count integer;
 v_global integer;
 v_hour timestamptz := date_trunc('hour',clock_timestamp());
 v_month timestamptz := date_trunc('month',clock_timestamp());
begin
 if p_user is null or p_lookup !~ '^(tt[0-9]{7,12}|(movie|tv)/[0-9]{1,12})$' or p_country !~ '^[A-Z]{2}$' then return jsonb_build_object('state','invalid'); end if;
 perform pg_advisory_xact_lock(529716200);
 select used into v_count from mimo_private.streaming_budget where scope='movie-request-hour' and actor=p_user::text and window_start=v_hour;
 if coalesce(v_count,0)>=40 then return jsonb_build_object('state','limited'); end if;
 insert into mimo_private.streaming_budget(scope,actor,window_start,used) values ('movie-request-hour',p_user::text,v_hour,1)
 on conflict(scope,actor,window_start) do update set used=mimo_private.streaming_budget.used+1;
 select response into v_cache from mimo_private.movie_details_cache where lookup_key=p_country||':'||p_lookup and expires_at>clock_timestamp();
 if found then return jsonb_build_object('state','cached','value',v_cache); end if;
 select used into v_global from mimo_private.streaming_budget where scope='global-month' and actor='movieofthenight' and window_start=v_month;
 if coalesce(v_global,0)>=900 then return jsonb_build_object('state','fallback'); end if;
 select used into v_count from mimo_private.streaming_budget where scope='movie-provider-hour' and actor=p_user::text and window_start=v_hour;
 if coalesce(v_count,0)>=12 then return jsonb_build_object('state','fallback'); end if;
 insert into mimo_private.streaming_budget(scope,actor,window_start,used) values ('movie-provider-hour',p_user::text,v_hour,1)
 on conflict(scope,actor,window_start) do update set used=mimo_private.streaming_budget.used+1;
 insert into mimo_private.streaming_budget(scope,actor,window_start,used) values ('global-month','movieofthenight',v_month,1)
 on conflict(scope,actor,window_start) do update set used=mimo_private.streaming_budget.used+1;
 return jsonb_build_object('state','reserved');
end $$;
revoke all on function public.mimo_movie_details_reserve(uuid,text,text) from public, anon, authenticated;
grant execute on function public.mimo_movie_details_reserve(uuid,text,text) to service_role;

create or replace function public.mimo_movie_details_cache_put(p_lookup text,p_country text,p_response jsonb)
returns boolean language plpgsql security definer set search_path = pg_catalog, mimo_private
as $$
begin
 if p_lookup !~ '^(tt[0-9]{7,12}|(movie|tv)/[0-9]{1,12})$' or p_country !~ '^[A-Z]{2}$' or pg_column_size(p_response)>32768 then return false; end if;
 insert into mimo_private.movie_details_cache(lookup_key,response,expires_at)
 values(p_country||':'||p_lookup,p_response,clock_timestamp()+interval '24 hours')
 on conflict(lookup_key) do update set response=excluded.response,expires_at=excluded.expires_at;
 return true;
end $$;
revoke all on function public.mimo_movie_details_cache_put(text,text,jsonb) from public, anon, authenticated;
grant execute on function public.mimo_movie_details_cache_put(text,text,jsonb) to service_role;
