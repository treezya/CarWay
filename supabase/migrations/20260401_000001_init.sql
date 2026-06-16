create extension if not exists "pgcrypto";
do $$
begin
  if not exists (select 1 from pg_type where typname = 'payment_method') then
    create type payment_method as enum ('card', 'credit');
  end if;
end$$;
create table if not exists public.app_roles (
  id uuid primary key default gen_random_uuid(),
  key text not null unique,
  name text not null,
  created_at timestamptz not null default now()
);
create table if not exists public.profiles (
  user_id uuid primary key references auth.users(id) on delete cascade,
  full_name text,
  phone text,
  email text,
  role_id uuid references public.app_roles(id),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);
create or replace function public.set_updated_at()
returns trigger language plpgsql as $$
begin
  new.updated_at = now();
  return new;
end$$;
drop trigger if exists trg_profiles_updated_at on public.profiles;
create trigger trg_profiles_updated_at
before update on public.profiles
for each row execute function public.set_updated_at();
create table if not exists public.cars (
  id uuid primary key default gen_random_uuid(),
  name text not null,
  brand text not null,
  color text not null,
  body_type text not null,
  fuel_type text not null,
  year int not null,
  mileage_km int not null,
  price_rub bigint not null,
  image_path text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);
drop trigger if exists trg_cars_updated_at on public.cars;
create trigger trg_cars_updated_at
before update on public.cars
for each row execute function public.set_updated_at();
create table if not exists public.orders (
  id uuid primary key default gen_random_uuid(),
  customer_name text not null,
  customer_phone text not null,
  customer_email text not null,
  car_id uuid not null references public.cars(id),
  payment payment_method not null,
  subtotal_rub bigint not null,
  fees_rub bigint not null,
  total_rub bigint not null,
  created_by uuid references auth.users(id),
  created_at timestamptz not null default now()
);
create table if not exists public.card_payments (
  id uuid primary key default gen_random_uuid(),
  order_id uuid not null unique references public.orders(id) on delete cascade,
  card_last4 text not null,
  card_holder text,
  card_expiry text,
  status text not null default 'created',
  created_at timestamptz not null default now()
);
create table if not exists public.credit_applications (
  id uuid primary key default gen_random_uuid(),
  order_id uuid not null unique references public.orders(id) on delete cascade,
  borrower_full_name text not null,
  passport text not null,
  income_rub bigint not null,
  work_place text not null,
  initial_payment_rub bigint not null,
  months int not null,
  status text not null default 'submitted',
  created_at timestamptz not null default now()
);
insert into public.app_roles (key, name)
values
  ('admin', 'Администратор'),
  ('manager', 'Менеджер'),
  ('viewer', 'Просмотр')
on conflict (key) do nothing;
alter table public.app_roles enable row level security;
alter table public.profiles enable row level security;
alter table public.cars enable row level security;
alter table public.orders enable row level security;
alter table public.card_payments enable row level security;
alter table public.credit_applications enable row level security;
drop policy if exists "profiles_read_own" on public.profiles;
create policy "profiles_read_own"
on public.profiles for select
using (auth.uid() = user_id);
drop policy if exists "profiles_update_own" on public.profiles;
create policy "profiles_update_own"
on public.profiles for update
using (auth.uid() = user_id);
drop policy if exists "cars_read_auth" on public.cars;
create policy "cars_read_auth"
on public.cars for select
to authenticated
using (true);
drop policy if exists "cars_read_anon" on public.cars;
create policy "cars_read_anon"
on public.cars for select
to anon
using (true);
create or replace function public.is_role(role_key text)
returns boolean
language sql
stable
as $$
  select exists (
    select 1
    from public.profiles p
    join public.app_roles r on r.id = p.role_id
    where p.user_id = auth.uid()
      and r.key = role_key
  );
$$;
drop policy if exists "cars_write_manager_admin" on public.cars;
create policy "cars_write_manager_admin"
on public.cars
for all
to authenticated
using (public.is_role('admin') or public.is_role('manager'))
with check (public.is_role('admin') or public.is_role('manager'));
drop policy if exists "orders_manager_admin" on public.orders;
create policy "orders_manager_admin"
on public.orders
for all
to authenticated
using (public.is_role('admin') or public.is_role('manager'))
with check (public.is_role('admin') or public.is_role('manager'));
drop policy if exists "card_payments_manager_admin" on public.card_payments;
create policy "card_payments_manager_admin"
on public.card_payments
for all
to authenticated
using (public.is_role('admin') or public.is_role('manager'))
with check (public.is_role('admin') or public.is_role('manager'));
drop policy if exists "credit_apps_manager_admin" on public.credit_applications;
create policy "credit_apps_manager_admin"
on public.credit_applications
for all
to authenticated
using (public.is_role('admin') or public.is_role('manager'))
with check (public.is_role('admin') or public.is_role('manager'));
