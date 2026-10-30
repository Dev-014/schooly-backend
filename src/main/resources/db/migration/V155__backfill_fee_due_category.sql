-- V155: Backfill fee_category_id in fee_due for collection plan installments and orphan dues

-- 1. Backfill fee_category_id from fee_structure_item for collection plan dues
UPDATE public.fee_due fd
SET fee_category_id = sub.fee_category_id
FROM (
    SELECT DISTINCT ON (fee_structure_id) fee_structure_id, fee_category_id
    FROM public.fee_structure_item
    WHERE fee_category_id IS NOT NULL
    ORDER BY fee_structure_id, is_part_of_collection_plan DESC, amount DESC
) sub
WHERE fd.fee_structure_id = sub.fee_structure_id
  AND fd.fee_category_id IS NULL;

-- 2. For any remaining dues with NULL fee_category_id, link to the school's Tuition Fee or first active category
UPDATE public.fee_due fd
SET fee_category_id = sub.cat_id
FROM (
    SELECT DISTINCT ON (school_id) school_id, id AS cat_id
    FROM public.fee_category
    ORDER BY school_id, CASE WHEN LOWER(name) LIKE '%tuition%' THEN 1 ELSE 2 END, id ASC
) sub
WHERE fd.school_id = sub.school_id
  AND fd.fee_category_id IS NULL;

-- 3. Create index for query performance
CREATE INDEX IF NOT EXISTS idx_fee_due_fee_category_id ON public.fee_due(fee_category_id);
