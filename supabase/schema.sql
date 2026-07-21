-- Fridge Tracker schema
-- Run this in the Supabase SQL editor for your project.

-- ─────────────────────────────────────────────
-- Inventory items currently in the fridge/pantry
-- ─────────────────────────────────────────────
create table if not exists inventory_items (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  name text not null,
  quantity numeric not null default 1,
  unit text,                          -- e.g. 'g', 'ml', 'count', null for "1 item"
  category text,                      -- e.g. 'produce', 'dairy', 'pantry', 'meat'
  photo_url text,                     -- matched from item_photos library, if any
  date_added timestamptz not null default now(),
  estimated_expiry date,              -- nullable: LLM estimate at add-time
  status text not null default 'fresh' check (status in ('fresh', 'expiring_soon', 'expired')),
  source text default 'manual' check (source in ('manual', 'receipt')),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index if not exists idx_inventory_items_user on inventory_items(user_id);
create index if not exists idx_inventory_items_expiry on inventory_items(estimated_expiry);

-- ─────────────────────────────────────────────
-- Personal photo library — keyword-labeled photos you upload,
-- matched against new inventory items (AI-assisted fuzzy match).
-- ─────────────────────────────────────────────
create table if not exists item_photos (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  keyword text not null,              -- e.g. "milk", "spinach"
  image_url text not null,
  created_at timestamptz not null default now()
);

create index if not exists idx_item_photos_user on item_photos(user_id);

-- ─────────────────────────────────────────────
-- Receipts — raw upload + extraction audit trail
-- ─────────────────────────────────────────────
create table if not exists receipts (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  image_url text,                     -- storage path if you keep the photo
  raw_extraction jsonb,               -- what the vision model returned
  status text not null default 'pending' check (status in ('pending', 'reviewed', 'discarded')),
  created_at timestamptz not null default now()
);

-- ─────────────────────────────────────────────
-- Saved / AI-generated recipes
-- ─────────────────────────────────────────────
create table if not exists recipes (
  id uuid primary key default gen_random_uuid(),
  user_id uuid references auth.users(id) on delete cascade,  -- null = shared/public recipe
  name text not null,
  ingredients jsonb not null,         -- [{ "name": "chicken breast", "quantity": 2, "unit": "count" }, ...]
  steps jsonb not null,               -- ["Preheat oven to 200C", ...]
  servings integer default 2,
  source text default 'ai' check (source in ('ai', 'manual')),
  created_at timestamptz not null default now()
);

-- ─────────────────────────────────────────────
-- Cook log — records when a recipe was made & inventory deducted
-- ─────────────────────────────────────────────
create table if not exists cook_log (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  recipe_id uuid references recipes(id) on delete set null,
  recipe_name text not null,          -- denormalised snapshot in case recipe is later deleted
  deducted jsonb not null,            -- what was actually removed from inventory
  cooked_at timestamptz not null default now()
);

-- ─────────────────────────────────────────────
-- Row Level Security — each user only sees their own data
-- ─────────────────────────────────────────────
alter table inventory_items enable row level security;
alter table receipts enable row level security;
alter table recipes enable row level security;
alter table cook_log enable row level security;
alter table item_photos enable row level security;

drop policy if exists "Users manage their own inventory" on inventory_items;
create policy "Users manage their own inventory"
  on inventory_items for all
  using (auth.uid() = user_id)
  with check (auth.uid() = user_id);

drop policy if exists "Users manage their own receipts" on receipts;
create policy "Users manage their own receipts"
  on receipts for all
  using (auth.uid() = user_id)
  with check (auth.uid() = user_id);

drop policy if exists "Users manage their own recipes, shared recipes are readable" on recipes;
create policy "Users manage their own recipes, shared recipes are readable"
  on recipes for select
  using (auth.uid() = user_id or user_id is null);

drop policy if exists "Users insert their own recipes" on recipes;
create policy "Users insert their own recipes"
  on recipes for insert
  with check (auth.uid() = user_id);

drop policy if exists "Users manage their own cook log" on cook_log;
create policy "Users manage their own cook log"
  on cook_log for all
  using (auth.uid() = user_id)
  with check (auth.uid() = user_id);

drop policy if exists "Users manage their own photo library" on item_photos;
create policy "Users manage their own photo library"
  on item_photos for all
  using (auth.uid() = user_id)
  with check (auth.uid() = user_id);

-- ─────────────────────────────────────────────
-- Keep updated_at fresh on inventory edits
-- ─────────────────────────────────────────────
create or replace function set_updated_at()
returns trigger as $$
begin
  new.updated_at = now();
  return new;
end;
$$ language plpgsql;

drop trigger if exists inventory_items_set_updated_at on inventory_items;
create trigger inventory_items_set_updated_at
  before update on inventory_items
  for each row execute function set_updated_at();

-- ─────────────────────────────────────────────
-- Storage buckets — run once. If a bucket already exists this errors
-- harmlessly; ignore "already exists" and continue.
-- ─────────────────────────────────────────────
insert into storage.buckets (id, name, public)
values ('item-photos', 'item-photos', true)
on conflict (id) do nothing;

insert into storage.buckets (id, name, public)
values ('receipts', 'receipts', false)
on conflict (id) do nothing;

-- Each user can manage files inside their own folder (path prefix = their user id)
drop policy if exists "Users manage their own item photos" on storage.objects;
create policy "Users manage their own item photos"
  on storage.objects for all
  using (bucket_id = 'item-photos' and auth.uid()::text = (storage.foldername(name))[1])
  with check (bucket_id = 'item-photos' and auth.uid()::text = (storage.foldername(name))[1]);

drop policy if exists "Users manage their own receipt images" on storage.objects;
create policy "Users manage their own receipt images"
  on storage.objects for all
  using (bucket_id = 'receipts' and auth.uid()::text = (storage.foldername(name))[1])
  with check (bucket_id = 'receipts' and auth.uid()::text = (storage.foldername(name))[1]);
