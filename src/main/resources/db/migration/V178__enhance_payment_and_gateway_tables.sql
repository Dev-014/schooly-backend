-- ============================================================================
-- Migration: V176__enhance_payment_and_gateway_tables.sql
-- Description: Enhances fee payment, school accounts, and subscriptions for 
--              Razorpay Route (Linked Accounts), offline receipt verification,
--              and audit logging.
-- ============================================================================

-- 1. Add Bank & Razorpay Route fields to school_account
ALTER TABLE public.school_account
    ADD COLUMN IF NOT EXISTS bank_account_number VARCHAR(50),
    ADD COLUMN IF NOT EXISTS ifsc_code VARCHAR(20),
    ADD COLUMN IF NOT EXISTS bank_name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS branch_name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS account_holder_name VARCHAR(150),
    ADD COLUMN IF NOT EXISTS razorpay_account_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS route_kyc_status VARCHAR(50) DEFAULT 'PENDING';

CREATE INDEX IF NOT EXISTS idx_school_account_razorpay ON public.school_account(razorpay_account_id);

-- 2. Add Gateway, Verification, and Receipt Attachment fields to fee_payment
ALTER TABLE public.fee_payment
    ADD COLUMN IF NOT EXISTS gateway_order_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS gateway_payment_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS gateway_signature VARCHAR(255),
    ADD COLUMN IF NOT EXISTS verification_status VARCHAR(50) DEFAULT 'APPROVED',
    ADD COLUMN IF NOT EXISTS verification_note TEXT,
    ADD COLUMN IF NOT EXISTS verified_by BIGINT REFERENCES public.users(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS verified_at TIMESTAMP WITHOUT TIME ZONE,
    ADD COLUMN IF NOT EXISTS failure_reason TEXT,
    ADD COLUMN IF NOT EXISTS currency VARCHAR(10) DEFAULT 'INR',
    ADD COLUMN IF NOT EXISTS receipt_file_id UUID REFERENCES public.files(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_fee_payment_gateway_order ON public.fee_payment(gateway_order_id);
CREATE INDEX IF NOT EXISTS idx_fee_payment_verification ON public.fee_payment(school_id, verification_status);

-- 3. Enhance school_subscriptions for tax and billing
ALTER TABLE public.school_subscriptions
    ADD COLUMN IF NOT EXISTS gateway_order_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS gateway_payment_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS gst_rate NUMERIC(5,2) DEFAULT 18.00,
    ADD COLUMN IF NOT EXISTS gst_amount NUMERIC(10,2) DEFAULT 0.00,
    ADD COLUMN IF NOT EXISTS base_amount NUMERIC(10,2) DEFAULT 0.00;

-- 4. Create Gateway Transaction Audit Log table
CREATE TABLE IF NOT EXISTS public.gateway_transaction_log (
    id BIGSERIAL PRIMARY KEY,
    domain VARCHAR(50) NOT NULL, -- 'STUDENT_FEE' or 'SCHOOL_SUBSCRIPTION'
    reference_id BIGINT NOT NULL,
    school_id BIGINT REFERENCES public.schools(id) ON DELETE CASCADE,
    gateway VARCHAR(50) NOT NULL DEFAULT 'RAZORPAY',
    event_type VARCHAR(100) NOT NULL,
    gateway_order_id VARCHAR(100),
    gateway_payment_id VARCHAR(100),
    amount NUMERIC(10,2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    raw_payload JSONB,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_gtl_order_id ON public.gateway_transaction_log(gateway_order_id);
CREATE INDEX IF NOT EXISTS idx_gtl_payment_id ON public.gateway_transaction_log(gateway_payment_id);
CREATE INDEX IF NOT EXISTS idx_gtl_school_domain ON public.gateway_transaction_log(school_id, domain);
