-- ============================================================================
-- Migration: V168__fix_evaluated_homework_submissions_status.sql
-- Description: Correct submission status for homework submissions that have
--              been evaluated/graded (marks_obtained or evaluated_at present)
--              but whose status remained as 'SUBMITTED' or 'PENDING'.
-- ============================================================================

UPDATE public.homework_submissions
SET status = 'GRADED',
    updated_at = CURRENT_TIMESTAMP
WHERE (marks_obtained IS NOT NULL OR evaluated_at IS NOT NULL)
  AND status IN ('SUBMITTED', 'PENDING');

