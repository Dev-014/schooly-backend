-- Fix fee_due records where paid_amount + discount_amount equals or exceeds amount, marking them PAID
UPDATE public.fee_due
SET status = 'PAID',
    updated_at = CURRENT_TIMESTAMP
WHERE (COALESCE(paid_amount, 0) + COALESCE(discount_amount, 0)) >= amount
  AND status != 'PAID';

