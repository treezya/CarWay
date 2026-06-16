create or replace function public.create_order_with_card(
  car_id uuid,
  customer_name text,
  customer_phone text,
  customer_email text,
  subtotal_rub bigint,
  fees_rub bigint,
  total_rub bigint,
  card_last4 text,
  card_holder text,
  card_expiry text
)
returns uuid
language plpgsql
security definer
set search_path = public
as $$
declare
  v_order_id uuid;
begin
  insert into public.orders (
    customer_name, customer_phone, customer_email, car_id, payment,
    subtotal_rub, fees_rub, total_rub, created_by
  )
  values (
    create_order_with_card.customer_name,
    create_order_with_card.customer_phone,
    create_order_with_card.customer_email,
    create_order_with_card.car_id,
    'card',
    create_order_with_card.subtotal_rub,
    create_order_with_card.fees_rub,
    create_order_with_card.total_rub,
    auth.uid()
  )
  returning id into v_order_id;
  insert into public.card_payments (
    order_id, card_last4, card_holder, card_expiry, status
  )
  values (
    v_order_id,
    right(coalesce(create_order_with_card.card_last4, ''), 4),
    create_order_with_card.card_holder,
    create_order_with_card.card_expiry,
    'created'
  );
  return v_order_id;
end;
$$;
revoke all on function public.create_order_with_card(uuid, text, text, text, bigint, bigint, bigint, text, text, text) from public;
grant execute on function public.create_order_with_card(uuid, text, text, text, bigint, bigint, bigint, text, text, text) to anon, authenticated;
create or replace function public.create_order_with_credit(
  car_id uuid,
  customer_name text,
  customer_phone text,
  customer_email text,
  subtotal_rub bigint,
  fees_rub bigint,
  total_rub bigint,
  borrower_full_name text,
  passport text,
  income_rub bigint,
  work_place text,
  initial_payment_rub bigint,
  months int
)
returns uuid
language plpgsql
security definer
set search_path = public
as $$
declare
  v_order_id uuid;
begin
  insert into public.orders (
    customer_name, customer_phone, customer_email, car_id, payment,
    subtotal_rub, fees_rub, total_rub, created_by
  )
  values (
    create_order_with_credit.customer_name,
    create_order_with_credit.customer_phone,
    create_order_with_credit.customer_email,
    create_order_with_credit.car_id,
    'credit',
    create_order_with_credit.subtotal_rub,
    create_order_with_credit.fees_rub,
    create_order_with_credit.total_rub,
    auth.uid()
  )
  returning id into v_order_id;
  insert into public.credit_applications (
    order_id, borrower_full_name, passport, income_rub, work_place,
    initial_payment_rub, months, status
  )
  values (
    v_order_id,
    create_order_with_credit.borrower_full_name,
    create_order_with_credit.passport,
    create_order_with_credit.income_rub,
    create_order_with_credit.work_place,
    create_order_with_credit.initial_payment_rub,
    create_order_with_credit.months,
    'submitted'
  );
  return v_order_id;
end;
$$;
revoke all on function public.create_order_with_credit(uuid, text, text, text, bigint, bigint, bigint, text, text, bigint, text, bigint, int) from public;
grant execute on function public.create_order_with_credit(uuid, text, text, text, bigint, bigint, bigint, text, text, bigint, text, bigint, int) to anon, authenticated;
