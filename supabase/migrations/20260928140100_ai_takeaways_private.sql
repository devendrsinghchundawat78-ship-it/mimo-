-- Draft only; reconcile production migration history before any push.
-- Only a trusted server-side importer can record reviewed public metadata.
create schema if not exists mimo_private;
revoke all on schema mimo_private from public, anon, authenticated;
grant usage on schema mimo_private to service_role;
create table if not exists mimo_private.ai_metadata (
  save_id uuid primary key references public.saves(id) on delete cascade,
  user_id uuid not null,
  title text not null check (length(title) between 1 and 500),
  description text not null default '' check (length(description) <= 3000),
  source_kind text not null check (source_kind = 'reviewed_public_metadata'),
  source_name text not null,
  source_revision text not null,
  ai_use_allowed boolean not null default false,
  updated_at timestamptz not null default now()
);
create table if not exists mimo_private.ai_takeaways (
  save_id uuid primary key references public.saves(id) on delete cascade,
  user_id uuid not null,
  source_revision text not null,
  takeaways jsonb not null,
  generated_at timestamptz not null default now()
);
create table if not exists mimo_private.ai_quota (
  scope text not null, actor text not null, window_start timestamptz not null,
  used integer not null default 0 check (used >= 0),
  primary key(scope,actor,window_start)
);
create index if not exists ai_metadata_user_idx on mimo_private.ai_metadata(user_id);
create index if not exists ai_takeaways_user_idx on mimo_private.ai_takeaways(user_id);
alter table mimo_private.ai_metadata enable row level security;
alter table mimo_private.ai_takeaways enable row level security;
alter table mimo_private.ai_quota enable row level security;
revoke all on mimo_private.ai_metadata,mimo_private.ai_takeaways,mimo_private.ai_quota from public,anon,authenticated;
grant select,insert,update,delete on mimo_private.ai_metadata,mimo_private.ai_takeaways,mimo_private.ai_quota to service_role;

create or replace function public.mimo_ai_reserve(p_user uuid,p_save uuid,p_force boolean default false)
returns jsonb language plpgsql security definer set search_path = pg_catalog,mimo_private
as $$
declare
  v_meta mimo_private.ai_metadata%rowtype;
  v_existing mimo_private.ai_takeaways%rowtype;
  v_used integer;
  v_hour timestamptz := date_trunc('hour',now());
  v_minute timestamptz := date_trunc('minute',now());
  v_day timestamptz := date_trunc('day',now() at time zone 'UTC') at time zone 'UTC';
begin
  -- Never trust a caller-supplied text payload, user ID claim, or category label.
  if not exists(select 1 from public.saves where id=p_save and user_id=p_user and status='active')
    then return jsonb_build_object('state','not_found'); end if;
  select * into v_meta from mimo_private.ai_metadata where save_id=p_save and user_id=p_user
    and ai_use_allowed and source_kind='reviewed_public_metadata' for update;
  if not found then return jsonb_build_object('state','ineligible'); end if;
  if not p_force then
    select * into v_existing from mimo_private.ai_takeaways where save_id=p_save and user_id=p_user
      and source_revision=v_meta.source_revision;
    if found then return jsonb_build_object('state','cached','takeaways',v_existing.takeaways); end if;
  end if;
  -- UPSERT WHERE serializes even the first use of a new window. Slots are spent on failures.
  insert into mimo_private.ai_quota(scope,actor,window_start,used) values('user_hour',p_user::text,v_hour,1)
    on conflict(scope,actor,window_start) do update set used=mimo_private.ai_quota.used+1
      where mimo_private.ai_quota.used<6 returning used into v_used;
  if v_used is null then return jsonb_build_object('state','limited'); end if;
  v_used := null;
  insert into mimo_private.ai_quota(scope,actor,window_start,used) values('global_day','all',v_day,1)
    on conflict(scope,actor,window_start) do update set used=mimo_private.ai_quota.used+1
      where mimo_private.ai_quota.used<600 returning used into v_used;
  if v_used is null then return jsonb_build_object('state','limited'); end if;
  v_used := null;
  insert into mimo_private.ai_quota(scope,actor,window_start,used) values('global_minute','all',v_minute,1)
    on conflict(scope,actor,window_start) do update set used=mimo_private.ai_quota.used+1
      where mimo_private.ai_quota.used<20 returning used into v_used;
  if v_used is null then return jsonb_build_object('state','limited'); end if;
  return jsonb_build_object('state','reserved','title',v_meta.title,
    'description',v_meta.description,'revision',v_meta.source_revision);
end;
$$;
revoke all on function public.mimo_ai_reserve(uuid,uuid,boolean) from public,anon,authenticated;
grant execute on function public.mimo_ai_reserve(uuid,uuid,boolean) to service_role;

create or replace function public.mimo_ai_store(p_user uuid,p_save uuid,p_revision text,p_takeaways jsonb)
returns boolean language plpgsql security definer set search_path = pg_catalog,mimo_private
as $$
begin
  if jsonb_typeof(p_takeaways)<>'object' or length(p_takeaways::text)>8000 then return false; end if;
  -- Lock save and metadata so a concurrent change cannot slip between validation and output write.
  perform 1 from public.saves where id=p_save and user_id=p_user and status='active' for update;
  if not found then return false; end if;
  perform 1 from mimo_private.ai_metadata where save_id=p_save and user_id=p_user
    and source_revision=p_revision and ai_use_allowed and source_kind='reviewed_public_metadata' for update;
  if not found then return false; end if;
  insert into mimo_private.ai_takeaways(save_id,user_id,source_revision,takeaways)
    values(p_save,p_user,p_revision,p_takeaways)
    on conflict(save_id) do update set user_id=excluded.user_id,source_revision=excluded.source_revision,
      takeaways=excluded.takeaways,generated_at=now();
  return true;
end;
$$;
revoke all on function public.mimo_ai_store(uuid,uuid,text,jsonb) from public,anon,authenticated;
grant execute on function public.mimo_ai_store(uuid,uuid,text,jsonb) to service_role;
