create or replace function public.mimo_streaming_reserve(p_user uuid, p_lookup text, p_country text)
returns jsonb language plpgsql security definer set search_path = pg_catalog, mimo_private
as $$
declare
  v_cache jsonb;
  v_request_count integer;
  v_user_count integer;
  v_global_count integer;
  v_hour timestamptz := date_trunc('hour', clock_timestamp());
  v_month timestamptz := date_trunc('month', clock_timestamp());
begin
  if p_user is null or p_lookup !~ '^(tt[0-9]{7,12}|(movie|tv)/[0-9]{1,12})$' or p_country !~ '^[A-Z]{2}$' then
    return jsonb_build_object('state','invalid');
  end if;
  perform pg_advisory_xact_lock(529716200);
  select used into v_request_count from mimo_private.streaming_budget
   where scope='request-hour' and actor=p_user::text and window_start=v_hour;
  if coalesce(v_request_count,0) >= 60 then return jsonb_build_object('state','limited','retryAfter',3600); end if;
  insert into mimo_private.streaming_budget(scope,actor,window_start,used)
   values ('request-hour',p_user::text,v_hour,1)
   on conflict(scope,actor,window_start) do update set used=mimo_private.streaming_budget.used+1;
  select response into v_cache from mimo_private.streaming_cache
   where lookup_key = p_country || ':' || p_lookup and expires_at > clock_timestamp();
  if found then return jsonb_build_object('state','cached','value',v_cache); end if;
  select used into v_user_count from mimo_private.streaming_budget
   where scope='user-hour' and actor=p_user::text and window_start=v_hour;
  select used into v_global_count from mimo_private.streaming_budget
   where scope='global-month' and actor='movieofthenight' and window_start=v_month;
  if coalesce(v_user_count,0) >= 20 or coalesce(v_global_count,0) >= 900 then
    return jsonb_build_object('state','limited','retryAfter',case when coalesce(v_global_count,0)>=900 then 86400 else 3600 end);
  end if;
  insert into mimo_private.streaming_budget(scope,actor,window_start,used)
   values ('user-hour',p_user::text,v_hour,1)
   on conflict(scope,actor,window_start) do update set used=mimo_private.streaming_budget.used+1;
  insert into mimo_private.streaming_budget(scope,actor,window_start,used)
   values ('global-month','movieofthenight',v_month,1)
   on conflict(scope,actor,window_start) do update set used=mimo_private.streaming_budget.used+1;
  return jsonb_build_object('state','reserved');
end $$;
revoke all on function public.mimo_streaming_reserve(uuid,text,text) from public, anon, authenticated;
grant execute on function public.mimo_streaming_reserve(uuid,text,text) to service_role;
