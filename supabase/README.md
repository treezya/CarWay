## Supabase (база данных) для AutoSalon

В папке `supabase/migrations/` лежат SQL-миграции со схемой:
- роли (`app_roles`)
- профили пользователей (`profiles`) связаны с `auth.users`
- каталог авто (`cars`)
- заказы (`orders`)
- оплаты картой (`card_payments`) — **без хранения CVV**
- кредитные заявки (`credit_applications`)
- RPC-функции для оформления заказа из desktop-клиента:
  - `create_order_with_card(...)`
  - `create_order_with_credit(...)`

### Как применить

Вариант 1: Supabase Dashboard → SQL Editor
- По очереди выполни:
  1. `supabase/migrations/20260401_000001_init.sql`
  2. `supabase/migrations/20260407_000002_rpc_checkout.sql`

Вариант 2: Supabase CLI
- Положи проект Supabase рядом и примени миграции через `supabase db push` (если используешь CLI)

### Storage для картинок (рекомендация)

Поле `cars.image_path` предназначено для хранения пути в Supabase Storage (например `cars/bmw-m5.webp`) или публичного URL.

## Подключение приложения к Supabase

Приложение читает каталог авто через Supabase REST (PostgREST) из таблицы `cars`.

### 1) Создай проект Supabase
- Supabase Dashboard → New project
- Дождись, пока база поднимется

### 2) Примени миграцию
- Dashboard → SQL Editor → New query
- Вставь содержимое `supabase/migrations/20260401_000001_init.sql`
- Run

### 3) Получи ключи
- Dashboard → Project Settings → API
- Скопируй:
  - **Project URL** → это `SUPABASE_URL`
  - **anon public key** → это `SUPABASE_ANON_KEY`

### 4) Укажи ключи в `supabase/.env.local`

1. Скопируй `supabase/.env.example` → `supabase/.env.local`
2. Вставь **Project URL** и **anon public key** из Dashboard → Settings → API

Или запусти с автозагрузкой:

```powershell
.\scripts\run_with_supabase.ps1
```

Альтернатива: переменные `SUPABASE_URL` и `SUPABASE_ANON_KEY` в конфигурации запуска IDE.

### 5) Заполни таблицу `cars`

Самый простой способ: Dashboard → Table Editor → `cars` → Insert row.

Обязательные поля:
- `name`, `brand`, `color`, `body_type`, `fuel_type`, `year`, `mileage_km`, `price_rub`
- `image_path` можно оставить пустым (тогда в UI будет пустая картинка), либо указать URL/путь

После этого экран каталога при старте автоматически загрузит машины из Supabase.

### 6) Полезные SQL-запросы для проверки

```sql
-- Список авто по цене
select id, name, brand, price_rub
from public.cars
order by price_rub asc;

-- Все заказы с названием авто
select o.id, o.created_at, c.name as car_name, o.customer_name, o.payment, o.total_rub
from public.orders o
join public.cars c on c.id = o.car_id
order by o.created_at desc;

-- Детали кредитных заявок
select ca.created_at, ca.borrower_full_name, ca.status, o.total_rub
from public.credit_applications ca
join public.orders o on o.id = ca.order_id
order by ca.created_at desc;
```

