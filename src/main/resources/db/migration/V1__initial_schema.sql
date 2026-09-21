-- ============================================================================
-- V1__initial_schema.sql
-- Schooly ERP - Consolidated Database Architecture Baseline
-- Squashed from migrations V1 through V102
-- ============================================================================

--
-- PostgreSQL database dump
--



SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET search_path = public;
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: public; Type: SCHEMA; Schema: -; Owner: -
--

-- *not* creating schema, since initdb creates it


--
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: -
--

--
-- Name: communication_audience_type; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.communication_audience_type AS ENUM (
    'ALL_SCHOOLS',
    'SELECTED_SCHOOLS',
    'SCHOOLS_BY_PLAN',
    'SCHOOLS_BY_CITY',
    'SCHOOLS_BY_BOARD',
    'TRIAL_SCHOOLS',
    'EXPIRED_SUBSCRIPTION',
    'RENEWAL_DUE',
    'INACTIVE_SCHOOLS'
);


--
-- Name: communication_channel; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.communication_channel AS ENUM (
    'PORTAL',
    'MOBILE_APP',
    'EMAIL',
    'SMS',
    'WHATSAPP'
);


--
-- Name: communication_delivery_status; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.communication_delivery_status AS ENUM (
    'PENDING',
    'DELIVERED',
    'READ',
    'FAILED'
);


--
-- Name: communication_importance; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.communication_importance AS ENUM (
    'INFORMATION',
    'ACTION_REQUIRED',
    'IMPORTANT',
    'CRITICAL'
);


--
-- Name: communication_message_type; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.communication_message_type AS ENUM (
    'ANNOUNCEMENT',
    'SOFTWARE_UPDATE',
    'MAINTENANCE_NOTICE',
    'PAYMENT_REMINDER',
    'SUBSCRIPTION_RENEWAL',
    'TRAINING_INVITATION',
    'NEW_FEATURE',
    'HOLIDAY_NOTICE',
    'EMERGENCY_ALERT',
    'GENERAL_MESSAGE'
);


--
-- Name: communication_status; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.communication_status AS ENUM (
    'DRAFT',
    'SCHEDULED',
    'SENT',
    'CANCELLED'
);


--
-- Name: crm_demo_mode; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.crm_demo_mode AS ENUM (
    'ONLINE',
    'OFFLINE'
);


--
-- Name: crm_demo_status; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.crm_demo_status AS ENUM (
    'SCHEDULED',
    'COMPLETED',
    'CANCELED',
    'NO_SHOW'
);


--
-- Name: crm_follow_up_action; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.crm_follow_up_action AS ENUM (
    'CALL',
    'WHATSAPP',
    'EMAIL',
    'MEETING',
    'VISIT'
);


--
-- Name: crm_follow_up_status; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.crm_follow_up_status AS ENUM (
    'PENDING',
    'COMPLETED',
    'MISSED'
);


--
-- Name: crm_lead_source; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.crm_lead_source AS ENUM (
    'WEBSITE',
    'FACEBOOK',
    'GOOGLE',
    'REFERENCE',
    'WALKIN',
    'COLD_CALL',
    'CAMPAIGN',
    'OTHER'
);


--
-- Name: crm_pipeline_stage; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.crm_pipeline_stage AS ENUM (
    'NEW',
    'CONTACTED',
    'QUALIFIED',
    'DEMO_SCHEDULED',
    'DEMO_COMPLETED',
    'QUOTATION_SENT',
    'NEGOTIATION',
    'WON',
    'LOST'
);


--
-- Name: crm_quotation_status; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.crm_quotation_status AS ENUM (
    'DRAFT',
    'SENT',
    'ACCEPTED',
    'REJECTED',
    'EXPIRED'
);


--
-- Name: user_role; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.user_role AS ENUM (
    'SUPER_ADMIN',
    'ADMIN',
    'TEACHER',
    'STUDENT',
    'PARENT',
    'STAFF'
);


--
-- Name: generate_family_code(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.generate_family_code() RETURNS text
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN 'FAM-' || TO_CHAR(CURRENT_TIMESTAMP, 'YYYYMMDD') || '-' || LPAD(nextval('family_code_seq')::TEXT, 4, '0');
END;
$$;


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: academic_years; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.academic_years (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    name character varying(100) NOT NULL,
    start_date date,
    end_date date,
    status character varying(50) DEFAULT 'ACTIVE'::character varying,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    display_name character varying(100) NOT NULL
);


--
-- Name: academic_years_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.academic_years_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: academic_years_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.academic_years_id_seq OWNED BY public.academic_years.id;


--
-- Name: account_requests; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.account_requests (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    user_id bigint,
    request_type character varying(100) NOT NULL,
    description text,
    status character varying(50) DEFAULT 'PENDING'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    resolved_at timestamp without time zone,
    resolution_notes text,
    requester_name character varying(255),
    requester_email character varying(255),
    requester_phone character varying(50),
    requested_role character varying(50),
    reject_reason text
);


--
-- Name: account_requests_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.account_requests_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: account_requests_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.account_requests_id_seq OWNED BY public.account_requests.id;


--
-- Name: admission_enquiries; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.admission_enquiries (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    enquiry_number character varying(50),
    student_name character varying(255) NOT NULL,
    parent_name character varying(255),
    phone character varying(50),
    email character varying(255),
    enquiry_date date NOT NULL,
    next_follow_up_date date,
    source character varying(100),
    status character varying(50) DEFAULT 'ACTIVE'::character varying,
    class_id bigint,
    number_of_children integer DEFAULT 1,
    assigned_to bigint,
    detailed_notes text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: admission_enquiries_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.admission_enquiries_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: admission_enquiries_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.admission_enquiries_id_seq OWNED BY public.admission_enquiries.id;


--
-- Name: admission_enquiry_follow_ups; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.admission_enquiry_follow_ups (
    id bigint NOT NULL,
    enquiry_id bigint NOT NULL,
    action_type character varying(100) NOT NULL,
    notes text,
    follow_up_date timestamp without time zone NOT NULL,
    recorded_by bigint,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: admission_enquiry_follow_ups_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.admission_enquiry_follow_ups_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: admission_enquiry_follow_ups_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.admission_enquiry_follow_ups_id_seq OWNED BY public.admission_enquiry_follow_ups.id;


--
-- Name: audit_logs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.audit_logs (
    id bigint NOT NULL,
    actor_id bigint,
    actor_name character varying(255),
    action character varying(255) NOT NULL,
    resource_type character varying(255),
    resource_id bigint,
    target_school_id bigint,
    target_school_name character varying(255),
    changes_json text,
    ip_address character varying(255),
    user_agent character varying(255),
    status character varying(50) DEFAULT 'SUCCESS'::character varying,
    "timestamp" timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: audit_logs_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.audit_logs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: audit_logs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.audit_logs_id_seq OWNED BY public.audit_logs.id;


--
-- Name: auth_sessions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.auth_sessions (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    school_id bigint,
    access_token text NOT NULL,
    refresh_token text NOT NULL,
    device_info jsonb,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: auth_sessions_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.auth_sessions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: auth_sessions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.auth_sessions_id_seq OWNED BY public.auth_sessions.id;


--
-- Name: class; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.class (
    id bigint NOT NULL,
    name character varying(255) NOT NULL,
    school_id bigint NOT NULL,
    grade integer,
    level character varying(50)
);


--
-- Name: class_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.class_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: class_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.class_id_seq OWNED BY public.class.id;


--
-- Name: class_subject_assignments; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.class_subject_assignments (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    class_id bigint NOT NULL,
    section_id bigint,
    subject_id bigint NOT NULL,
    academic_year_id bigint NOT NULL,
    subject_type character varying(20) DEFAULT 'CORE'::character varying NOT NULL,
    status character varying(20) DEFAULT 'ACTIVE'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: class_subject_assignments_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.class_subject_assignments_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: class_subject_assignments_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.class_subject_assignments_id_seq OWNED BY public.class_subject_assignments.id;


--
-- Name: class_teacher_assignments; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.class_teacher_assignments (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    staff_id bigint NOT NULL,
    class_id bigint NOT NULL,
    section_id bigint,
    academic_year_id bigint NOT NULL,
    status character varying(50) DEFAULT 'ACTIVE'::character varying,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: class_teacher_assignments_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.class_teacher_assignments_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: class_teacher_assignments_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.class_teacher_assignments_id_seq OWNED BY public.class_teacher_assignments.id;


--
-- Name: collection_plan; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.collection_plan (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    name character varying(255) NOT NULL,
    description text,
    is_active boolean DEFAULT true,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: collection_plan_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.collection_plan_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: collection_plan_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.collection_plan_id_seq OWNED BY public.collection_plan.id;


--
-- Name: collection_plan_item; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.collection_plan_item (
    id bigint NOT NULL,
    collection_plan_id bigint NOT NULL,
    label character varying(255) NOT NULL,
    due_date date,
    amount_type character varying(50) NOT NULL,
    amount_value numeric(10,2),
    sequence_order integer DEFAULT 0 NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: collection_plan_item_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.collection_plan_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: collection_plan_item_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.collection_plan_item_id_seq OWNED BY public.collection_plan_item.id;


--
-- Name: communication_announcement_schools; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.communication_announcement_schools (
    announcement_id bigint NOT NULL,
    school_id bigint NOT NULL
);


--
-- Name: communication_announcements; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.communication_announcements (
    id bigint NOT NULL,
    subject character varying(255) NOT NULL,
    message text NOT NULL,
    message_type public.communication_message_type NOT NULL,
    importance public.communication_importance DEFAULT 'INFORMATION'::public.communication_importance NOT NULL,
    status public.communication_status DEFAULT 'DRAFT'::public.communication_status NOT NULL,
    audience_type public.communication_audience_type NOT NULL,
    audience_criteria jsonb,
    scheduled_at timestamp without time zone,
    created_by_user_id bigint,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: communication_announcements_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.communication_announcements_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: communication_announcements_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.communication_announcements_id_seq OWNED BY public.communication_announcements.id;


--
-- Name: communication_deliveries; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.communication_deliveries (
    id bigint NOT NULL,
    announcement_id bigint NOT NULL,
    school_id bigint NOT NULL,
    delivery_channel public.communication_channel DEFAULT 'PORTAL'::public.communication_channel NOT NULL,
    status public.communication_delivery_status DEFAULT 'PENDING'::public.communication_delivery_status NOT NULL,
    delivered_at timestamp without time zone,
    read_at timestamp without time zone,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: communication_deliveries_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.communication_deliveries_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: communication_deliveries_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.communication_deliveries_id_seq OWNED BY public.communication_deliveries.id;


--
-- Name: communication_templates; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.communication_templates (
    id bigint NOT NULL,
    template_name character varying(255) NOT NULL,
    category public.communication_message_type NOT NULL,
    message text NOT NULL,
    is_active boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: communication_templates_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.communication_templates_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: communication_templates_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.communication_templates_id_seq OWNED BY public.communication_templates.id;


--
-- Name: crm_activity_logs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.crm_activity_logs (
    id bigint NOT NULL,
    lead_id bigint NOT NULL,
    actor_id bigint,
    activity_type character varying(100) NOT NULL,
    description text NOT NULL,
    metadata jsonb,
    created_at timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: crm_activity_logs_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.crm_activity_logs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: crm_activity_logs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.crm_activity_logs_id_seq OWNED BY public.crm_activity_logs.id;


--
-- Name: crm_demos; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.crm_demos (
    id bigint NOT NULL,
    lead_id bigint NOT NULL,
    demo_date timestamp without time zone NOT NULL,
    mode public.crm_demo_mode NOT NULL,
    demo_by_id bigint,
    status public.crm_demo_status DEFAULT 'SCHEDULED'::public.crm_demo_status,
    feedback text,
    recording_url character varying(500),
    meeting_notes text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: crm_demos_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.crm_demos_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: crm_demos_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.crm_demos_id_seq OWNED BY public.crm_demos.id;


--
-- Name: crm_follow_ups; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.crm_follow_ups (
    id bigint NOT NULL,
    lead_id bigint NOT NULL,
    action_type public.crm_follow_up_action NOT NULL,
    scheduled_date timestamp without time zone NOT NULL,
    remarks text,
    executive_id bigint,
    status public.crm_follow_up_status DEFAULT 'PENDING'::public.crm_follow_up_status,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: crm_follow_ups_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.crm_follow_ups_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: crm_follow_ups_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.crm_follow_ups_id_seq OWNED BY public.crm_follow_ups.id;


--
-- Name: crm_leads; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.crm_leads (
    id bigint NOT NULL,
    school_name character varying(255) NOT NULL,
    principal_name character varying(255),
    city character varying(100),
    board character varying(50),
    mobile character varying(20) NOT NULL,
    alternative_mobile character varying(20),
    email character varying(255),
    address text,
    state character varying(100),
    pin_code character varying(20),
    approx_student_strength integer,
    teachers integer,
    branches integer,
    current_erp character varying(255),
    website character varying(255),
    existing_problems text,
    lead_source public.crm_lead_source DEFAULT 'OTHER'::public.crm_lead_source,
    assigned_employee_id bigint,
    priority character varying(50) DEFAULT 'MEDIUM'::character varying,
    expected_closing_date timestamp without time zone,
    lead_rating integer DEFAULT 1,
    notes text,
    pipeline_stage public.crm_pipeline_stage DEFAULT 'NEW'::public.crm_pipeline_stage,
    status character varying(50) DEFAULT 'ACTIVE'::character varying,
    converted_school_id bigint,
    lost_reason character varying(255),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: crm_leads_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.crm_leads_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: crm_leads_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.crm_leads_id_seq OWNED BY public.crm_leads.id;


--
-- Name: crm_quotations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.crm_quotations (
    id bigint NOT NULL,
    quotation_number character varying(100) NOT NULL,
    lead_id bigint NOT NULL,
    plan_name character varying(255) NOT NULL,
    amount numeric(12,2) NOT NULL,
    discount numeric(12,2) DEFAULT 0.00,
    gst numeric(12,2) DEFAULT 0.00,
    total numeric(12,2) NOT NULL,
    expiry_date timestamp without time zone,
    status public.crm_quotation_status DEFAULT 'DRAFT'::public.crm_quotation_status,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: crm_quotations_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.crm_quotations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: crm_quotations_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.crm_quotations_id_seq OWNED BY public.crm_quotations.id;


--
-- Name: data_import_errors; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.data_import_errors (
    error_id bigint NOT NULL,
    job_id bigint NOT NULL,
    row_index character varying(50) NOT NULL,
    category character varying(100) NOT NULL,
    field_name character varying(100) NOT NULL,
    error_message character varying(500) NOT NULL,
    current_value character varying(500),
    resolved boolean DEFAULT false
);


--
-- Name: data_import_errors_error_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.data_import_errors_error_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: data_import_errors_error_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.data_import_errors_error_id_seq OWNED BY public.data_import_errors.error_id;


--
-- Name: data_import_jobs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.data_import_jobs (
    job_id bigint NOT NULL,
    school_id bigint NOT NULL,
    category character varying(100) NOT NULL,
    file_name character varying(255) NOT NULL,
    status character varying(50) DEFAULT 'IN_PROGRESS'::character varying NOT NULL,
    total_records integer DEFAULT 0,
    successful_records integer DEFAULT 0,
    failed_records integer DEFAULT 0,
    field_mappings text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: data_import_jobs_job_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.data_import_jobs_job_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: data_import_jobs_job_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.data_import_jobs_job_id_seq OWNED BY public.data_import_jobs.job_id;


--
-- Name: departments; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.departments (
    id bigint NOT NULL,
    name character varying(100) NOT NULL,
    head_employee_id bigint,
    description text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: departments_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.departments_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: departments_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.departments_id_seq OWNED BY public.departments.id;


--
-- Name: employee_assets; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.employee_assets (
    id bigint NOT NULL,
    employee_id bigint NOT NULL,
    asset_name character varying(100) NOT NULL,
    asset_type character varying(50) NOT NULL,
    serial_number character varying(100),
    assigned_date timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    return_date timestamp without time zone,
    status character varying(50) DEFAULT 'ASSIGNED'::character varying NOT NULL,
    notes text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: employee_assets_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.employee_assets_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: employee_assets_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.employee_assets_id_seq OWNED BY public.employee_assets.id;


--
-- Name: employee_attendance; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.employee_attendance (
    id bigint NOT NULL,
    employee_id bigint NOT NULL,
    date date NOT NULL,
    status character varying(50) NOT NULL,
    check_in_time time without time zone,
    check_out_time time without time zone,
    notes text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: employee_attendance_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.employee_attendance_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: employee_attendance_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.employee_attendance_id_seq OWNED BY public.employee_attendance.id;


--
-- Name: employee_audit_logs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.employee_audit_logs (
    id bigint NOT NULL,
    employee_id bigint,
    action character varying(100) NOT NULL,
    entity_type character varying(100) NOT NULL,
    entity_id bigint NOT NULL,
    details text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: employee_audit_logs_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.employee_audit_logs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: employee_audit_logs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.employee_audit_logs_id_seq OWNED BY public.employee_audit_logs.id;


--
-- Name: employee_documents; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.employee_documents (
    id bigint NOT NULL,
    employee_id bigint NOT NULL,
    document_type character varying(100) NOT NULL,
    file_name character varying(255) NOT NULL,
    file_url character varying(500) NOT NULL,
    uploaded_by bigint,
    status character varying(50) DEFAULT 'PENDING'::character varying,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: employee_documents_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.employee_documents_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: employee_documents_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.employee_documents_id_seq OWNED BY public.employee_documents.id;


--
-- Name: employee_leaves; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.employee_leaves (
    id bigint NOT NULL,
    employee_id bigint NOT NULL,
    leave_type character varying(100) NOT NULL,
    start_date date NOT NULL,
    end_date date NOT NULL,
    reason text,
    status character varying(50) DEFAULT 'PENDING'::character varying NOT NULL,
    approved_by bigint,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: employee_leaves_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.employee_leaves_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: employee_leaves_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.employee_leaves_id_seq OWNED BY public.employee_leaves.id;


--
-- Name: employee_lifecycle; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.employee_lifecycle (
    id bigint NOT NULL,
    employee_id bigint NOT NULL,
    event_type character varying(50) NOT NULL,
    event_date timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    description text,
    created_by bigint,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: employee_lifecycle_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.employee_lifecycle_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: employee_lifecycle_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.employee_lifecycle_id_seq OWNED BY public.employee_lifecycle.id;


--
-- Name: employee_notes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.employee_notes (
    id bigint NOT NULL,
    employee_id bigint NOT NULL,
    note_content text NOT NULL,
    author_id bigint,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: employee_notes_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.employee_notes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: employee_notes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.employee_notes_id_seq OWNED BY public.employee_notes.id;


--
-- Name: employee_payroll; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.employee_payroll (
    id bigint NOT NULL,
    employee_id bigint NOT NULL,
    month character varying(20) NOT NULL,
    year integer NOT NULL,
    base_salary numeric(10,2) NOT NULL,
    allowances numeric(10,2) DEFAULT 0,
    deductions numeric(10,2) DEFAULT 0,
    net_salary numeric(10,2) NOT NULL,
    status character varying(50) DEFAULT 'PENDING'::character varying NOT NULL,
    payment_date date,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: employee_payroll_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.employee_payroll_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: employee_payroll_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.employee_payroll_id_seq OWNED BY public.employee_payroll.id;


--
-- Name: employee_performance; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.employee_performance (
    id bigint NOT NULL,
    employee_id bigint NOT NULL,
    review_cycle character varying(100) NOT NULL,
    rating integer NOT NULL,
    reviewer_id bigint,
    comments text,
    goals text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT employee_performance_rating_check CHECK (((rating >= 1) AND (rating <= 5)))
);


--
-- Name: employee_performance_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.employee_performance_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: employee_performance_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.employee_performance_id_seq OWNED BY public.employee_performance.id;


--
-- Name: employee_timeline; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.employee_timeline (
    id bigint NOT NULL,
    employee_id bigint NOT NULL,
    title character varying(100) NOT NULL,
    description text,
    date timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: employee_timeline_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.employee_timeline_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: employee_timeline_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.employee_timeline_id_seq OWNED BY public.employee_timeline.id;


--
-- Name: exam_admit_cards; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.exam_admit_cards (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    exam_setup_id bigint NOT NULL,
    student_id bigint NOT NULL,
    class_id bigint NOT NULL,
    section_id bigint,
    roll_number character varying(50),
    card_number character varying(100),
    status character varying(30) DEFAULT 'PENDING'::character varying NOT NULL,
    generated_at timestamp without time zone,
    released_at timestamp without time zone,
    template_name character varying(100) DEFAULT 'STANDARD'::character varying,
    notes text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: exam_admit_cards_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.exam_admit_cards_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: exam_admit_cards_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.exam_admit_cards_id_seq OWNED BY public.exam_admit_cards.id;


--
-- Name: exam_attendances; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.exam_attendances (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    term_id bigint NOT NULL,
    exam_setup_id bigint,
    exam_schedule_id bigint,
    class_id bigint NOT NULL,
    section_id bigint,
    student_id bigint NOT NULL,
    room_number character varying(50) DEFAULT 'Room 402B'::character varying,
    seat_assignment character varying(50) DEFAULT 'Row 1, Seat 1'::character varying NOT NULL,
    attendance_status character varying(20) DEFAULT 'PRESENT'::character varying NOT NULL,
    session_status character varying(30) DEFAULT 'ACTIVE'::character varying NOT NULL,
    remarks text,
    recorded_by bigint,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: exam_attendances_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.exam_attendances_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: exam_attendances_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.exam_attendances_id_seq OWNED BY public.exam_attendances.id;


--
-- Name: exam_co_curricular_grades; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.exam_co_curricular_grades (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    term_id bigint NOT NULL,
    class_id bigint NOT NULL,
    section_id bigint,
    student_id bigint NOT NULL,
    physical_education_grade character varying(10) DEFAULT 'PENDING'::character varying,
    visual_arts_grade character varying(10) DEFAULT 'PENDING'::character varying,
    performing_arts_grade character varying(10) DEFAULT 'PENDING'::character varying,
    health_wellness_grade character varying(10) DEFAULT 'PENDING'::character varying,
    status character varying(20) DEFAULT 'PENDING'::character varying NOT NULL,
    entered_by bigint,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: exam_co_curricular_grades_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.exam_co_curricular_grades_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: exam_co_curricular_grades_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.exam_co_curricular_grades_id_seq OWNED BY public.exam_co_curricular_grades.id;


--
-- Name: exam_divisions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.exam_divisions (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    name character varying(100) NOT NULL,
    percent_from numeric(5,2) NOT NULL,
    percent_to numeric(5,2) NOT NULL,
    description text,
    color_tag character varying(20) DEFAULT 'BLUE'::character varying,
    status character varying(20) DEFAULT 'ACTIVE'::character varying NOT NULL,
    order_no integer DEFAULT 1,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: exam_divisions_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.exam_divisions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: exam_divisions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.exam_divisions_id_seq OWNED BY public.exam_divisions.id;


--
-- Name: exam_grade_scales; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.exam_grade_scales (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    name character varying(100) NOT NULL,
    grade_point numeric(4,2) DEFAULT 0.0,
    target_class character varying(100) DEFAULT 'All Classes'::character varying,
    percent_from numeric(5,2) NOT NULL,
    percent_to numeric(5,2) NOT NULL,
    description text,
    status character varying(20) DEFAULT 'ACTIVE'::character varying NOT NULL,
    order_no integer DEFAULT 1,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: exam_grade_scales_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.exam_grade_scales_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: exam_grade_scales_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.exam_grade_scales_id_seq OWNED BY public.exam_grade_scales.id;


--
-- Name: exam_marks; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.exam_marks (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    exam_setup_id bigint NOT NULL,
    class_id bigint NOT NULL,
    section_id bigint,
    subject_id bigint NOT NULL,
    student_id bigint NOT NULL,
    marks_obtained numeric(6,2),
    max_marks numeric(6,2) DEFAULT 100.00 NOT NULL,
    attendance_status character varying(20) DEFAULT 'PRESENT'::character varying NOT NULL,
    remarks text,
    entered_by bigint,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: exam_marks_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.exam_marks_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: exam_marks_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.exam_marks_id_seq OWNED BY public.exam_marks.id;


--
-- Name: exam_report_card_batches; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.exam_report_card_batches (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    batch_name character varying(150) NOT NULL,
    term_id bigint,
    exam_setup_id bigint,
    class_id bigint,
    section_id bigint,
    generation_mode character varying(20) DEFAULT 'TERM_WISE'::character varying NOT NULL,
    template_name character varying(100) DEFAULT 'Classic CBSE Standard'::character varying,
    total_count integer DEFAULT 0 NOT NULL,
    processed_count integer DEFAULT 0 NOT NULL,
    progress_percent numeric(5,2) DEFAULT 0.00 NOT NULL,
    status character varying(30) DEFAULT 'PROCESSING'::character varying NOT NULL,
    completed_at timestamp without time zone,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: exam_report_card_batches_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.exam_report_card_batches_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: exam_report_card_batches_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.exam_report_card_batches_id_seq OWNED BY public.exam_report_card_batches.id;


--
-- Name: exam_report_cards; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.exam_report_cards (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    term_id bigint NOT NULL,
    exam_setup_id bigint,
    class_id bigint NOT NULL,
    section_id bigint,
    student_id bigint NOT NULL,
    generation_mode character varying(20) DEFAULT 'TERM_WISE'::character varying NOT NULL,
    template_name character varying(100) DEFAULT 'Classic CBSE Standard'::character varying,
    total_marks numeric(6,2),
    max_marks numeric(6,2) DEFAULT 100.00,
    percentage numeric(5,2),
    grade character varying(10),
    division character varying(50),
    status character varying(30) DEFAULT 'GENERATED'::character varying NOT NULL,
    batch_id bigint,
    file_url text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: exam_report_cards_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.exam_report_cards_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: exam_report_cards_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.exam_report_cards_id_seq OWNED BY public.exam_report_cards.id;


--
-- Name: exam_schedules; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.exam_schedules (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    exam_setup_id bigint NOT NULL,
    class_id bigint NOT NULL,
    section_id bigint,
    subject_id bigint NOT NULL,
    exam_date date NOT NULL,
    start_time time without time zone NOT NULL,
    end_time time without time zone NOT NULL,
    room_number character varying(100),
    full_marks numeric(6,2) DEFAULT 100.00 NOT NULL,
    passing_marks numeric(6,2) DEFAULT 35.00 NOT NULL,
    instructions text,
    status character varying(30) DEFAULT 'SCHEDULED'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: exam_schedules_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.exam_schedules_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: exam_schedules_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.exam_schedules_id_seq OWNED BY public.exam_schedules.id;


--
-- Name: exam_setups; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.exam_setups (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    term_id bigint NOT NULL,
    order_no integer DEFAULT 1 NOT NULL,
    name character varying(150) NOT NULL,
    group_name character varying(100),
    best_of_count integer,
    weightage_active boolean DEFAULT false,
    weightage_percent numeric(5,2) DEFAULT 0.00,
    evaluation_type character varying(50) DEFAULT 'STANDARD'::character varying,
    internal_note text,
    status character varying(30) DEFAULT 'ACTIVE'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: exam_setups_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.exam_setups_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: exam_setups_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.exam_setups_id_seq OWNED BY public.exam_setups.id;


--
-- Name: exam_teacher_remarks; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.exam_teacher_remarks (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    term_id bigint NOT NULL,
    class_id bigint NOT NULL,
    section_id bigint,
    student_id bigint NOT NULL,
    previous_grade character varying(10) DEFAULT 'B'::character varying,
    teacher_remarks text,
    status character varying(20) DEFAULT 'PENDING'::character varying NOT NULL,
    entered_by bigint,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: exam_teacher_remarks_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.exam_teacher_remarks_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: exam_teacher_remarks_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.exam_teacher_remarks_id_seq OWNED BY public.exam_teacher_remarks.id;


--
-- Name: exam_terms; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.exam_terms (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    academic_year_id bigint,
    name character varying(150) NOT NULL,
    description character varying(255),
    start_date date NOT NULL,
    end_date date NOT NULL,
    result_publish_date date,
    status character varying(30) DEFAULT 'ACTIVE'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: exam_terms_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.exam_terms_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: exam_terms_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.exam_terms_id_seq OWNED BY public.exam_terms.id;


--
-- Name: families; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.families (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    family_code character varying(50) NOT NULL,
    status character varying(20) DEFAULT 'ACTIVE'::character varying,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: families_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.families_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: families_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.families_id_seq OWNED BY public.families.id;


--
-- Name: family_code_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.family_code_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: fee_category; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.fee_category (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    name character varying(255) NOT NULL,
    description text,
    is_active boolean DEFAULT true,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: fee_category_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.fee_category_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: fee_category_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.fee_category_id_seq OWNED BY public.fee_category.id;


--
-- Name: fee_due; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.fee_due (
    id bigint NOT NULL,
    student_id bigint NOT NULL,
    school_id bigint NOT NULL,
    fee_category_id bigint,
    fee_structure_id bigint,
    title character varying(255) NOT NULL,
    amount numeric(10,2) NOT NULL,
    paid_amount numeric(10,2) DEFAULT 0,
    due_date date NOT NULL,
    status character varying(50) DEFAULT 'UNPAID'::character varying,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    discount_amount numeric(10,2) DEFAULT 0,
    term_name character varying(100),
    is_ad_hoc boolean DEFAULT false,
    collection_plan_item_id bigint
);


--
-- Name: fee_due_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.fee_due_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: fee_due_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.fee_due_id_seq OWNED BY public.fee_due.id;


--
-- Name: fee_generation_batch; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.fee_generation_batch (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    academic_year_id bigint NOT NULL,
    class_id bigint NOT NULL,
    fee_structure_id bigint NOT NULL,
    collection_plan_id bigint,
    number_of_students integer NOT NULL,
    total_amount_generated numeric(10,2) NOT NULL,
    generated_by character varying(255) NOT NULL,
    generated_at timestamp without time zone NOT NULL
);


--
-- Name: fee_generation_batch_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.fee_generation_batch_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: fee_generation_batch_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.fee_generation_batch_id_seq OWNED BY public.fee_generation_batch.id;


--
-- Name: fee_invoice; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.fee_invoice (
    id bigint NOT NULL,
    student_id bigint NOT NULL,
    school_id bigint NOT NULL,
    academic_year_id bigint,
    due_date date NOT NULL,
    total_amount numeric(19,2) NOT NULL,
    paid_amount numeric(19,2),
    status character varying(255) NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    fee_structure_id bigint
);


--
-- Name: fee_invoice_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.fee_invoice_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: fee_invoice_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.fee_invoice_id_seq OWNED BY public.fee_invoice.id;


--
-- Name: fee_payment; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.fee_payment (
    id bigint CONSTRAINT payment_id_not_null NOT NULL,
    school_id bigint CONSTRAINT payment_school_id_not_null NOT NULL,
    amount numeric(19,2) CONSTRAINT payment_amount_not_null NOT NULL,
    payment_mode character varying(255) CONSTRAINT payment_payment_mode_not_null NOT NULL,
    transaction_id character varying(255),
    status character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    student_id bigint,
    receipt_number character varying(255),
    payment_date date,
    unallocated_amount numeric(10,2) DEFAULT 0,
    account_id bigint
);


--
-- Name: fee_payment_allocation; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.fee_payment_allocation (
    id bigint NOT NULL,
    fee_payment_id bigint NOT NULL,
    fee_due_id bigint NOT NULL,
    allocated_amount numeric(10,2) NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: fee_payment_allocation_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.fee_payment_allocation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: fee_payment_allocation_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.fee_payment_allocation_id_seq OWNED BY public.fee_payment_allocation.id;


--
-- Name: fee_reminders; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.fee_reminders (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    staff_id bigint,
    student_id bigint NOT NULL,
    fee_amount numeric(10,2),
    method character varying(50) NOT NULL,
    status character varying(50) NOT NULL,
    sent_at timestamp without time zone,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: fee_reminders_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.fee_reminders_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: fee_reminders_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.fee_reminders_id_seq OWNED BY public.fee_reminders.id;


--
-- Name: fee_structure; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.fee_structure (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    academic_year_id bigint,
    name character varying(255) NOT NULL,
    description text,
    is_active boolean DEFAULT true,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    class_id bigint,
    collection_plan_id bigint
);


--
-- Name: fee_structure_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.fee_structure_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: fee_structure_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.fee_structure_id_seq OWNED BY public.fee_structure.id;


--
-- Name: fee_structure_item; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.fee_structure_item (
    id bigint NOT NULL,
    fee_structure_id bigint NOT NULL,
    fee_category_id bigint NOT NULL,
    amount numeric(10,2) NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    is_part_of_collection_plan boolean DEFAULT true
);


--
-- Name: fee_structure_item_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.fee_structure_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: fee_structure_item_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.fee_structure_item_id_seq OWNED BY public.fee_structure_item.id;


--
-- Name: gate_passes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.gate_passes (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    pass_number character varying(50),
    person_name character varying(255) NOT NULL,
    role character varying(50) NOT NULL,
    student_id bigint,
    staff_id bigint,
    class_or_department character varying(100),
    reason_for_exit text NOT NULL,
    pass_date date NOT NULL,
    exit_time time without time zone NOT NULL,
    expected_return_time time without time zone,
    approved_by character varying(255) NOT NULL,
    approved_by_staff_id bigint,
    status character varying(50) DEFAULT 'APPROVED'::character varying,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: gate_passes_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.gate_passes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: gate_passes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.gate_passes_id_seq OWNED BY public.gate_passes.id;


--
-- Name: id_card_generations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.id_card_generations (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    student_id bigint NOT NULL,
    status character varying(50) DEFAULT 'GENERATED'::character varying NOT NULL,
    generated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: id_card_generations_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.id_card_generations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: id_card_generations_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.id_card_generations_id_seq OWNED BY public.id_card_generations.id;


--
-- Name: impersonation_sessions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.impersonation_sessions (
    id bigint NOT NULL,
    original_user_id bigint NOT NULL,
    impersonated_user_id bigint NOT NULL,
    session_id character varying(255) NOT NULL,
    started_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ended_at timestamp without time zone,
    status character varying(50) DEFAULT 'ACTIVE'::character varying NOT NULL,
    ip_address character varying(255),
    device_info jsonb
);


--
-- Name: impersonation_sessions_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.impersonation_sessions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: impersonation_sessions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.impersonation_sessions_id_seq OWNED BY public.impersonation_sessions.id;


--
-- Name: onboarding_drafts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.onboarding_drafts (
    school_id bigint NOT NULL,
    status character varying(50) DEFAULT 'DRAFT'::character varying NOT NULL,
    current_step integer DEFAULT 1 NOT NULL,
    step1_data text,
    step2_data text,
    step3_data text,
    step4_data text,
    step5_data text,
    step6_data text,
    step7_data text,
    step8_data text,
    step9_data text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    step10_data text,
    step11_data text
);


--
-- Name: onboarding_drafts_school_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.onboarding_drafts_school_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: onboarding_drafts_school_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.onboarding_drafts_school_id_seq OWNED BY public.onboarding_drafts.school_id;


--
-- Name: online_admissions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.online_admissions (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    application_id character varying(50) NOT NULL,
    student_name character varying(255) NOT NULL,
    class_id bigint,
    father_name character varying(255),
    date_of_birth date,
    gender character varying(20),
    category_id bigint,
    mobile_number character varying(20),
    email character varying(255),
    address text,
    previous_school character varying(255),
    transaction_status character varying(50) DEFAULT 'UNPAID'::character varying,
    status character varying(50) DEFAULT 'PENDING'::character varying,
    applied_date timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: online_admissions_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.online_admissions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: online_admissions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.online_admissions_id_seq OWNED BY public.online_admissions.id;


--
-- Name: parcel_dispatches; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.parcel_dispatches (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    receiver_name character varying(255) NOT NULL,
    receiver_institution character varying(255),
    delivery_address text NOT NULL,
    phone_number character varying(50),
    dispatch_date date NOT NULL,
    item_details text NOT NULL,
    courier_name character varying(100) NOT NULL,
    tracking_number character varying(100),
    status character varying(50) DEFAULT 'IN_TRANSIT'::character varying,
    delivered_date date,
    notes text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: parcel_dispatches_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.parcel_dispatches_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: parcel_dispatches_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.parcel_dispatches_id_seq OWNED BY public.parcel_dispatches.id;


--
-- Name: parcel_receives; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.parcel_receives (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    sender_name character varying(255) NOT NULL,
    contact_number character varying(50),
    recipient_name character varying(255),
    recipient_type character varying(50),
    item_details text NOT NULL,
    date_received date NOT NULL,
    received_by character varying(255) NOT NULL,
    received_by_staff_id bigint,
    status character varying(50) DEFAULT 'RECEIVED'::character varying,
    collected_at timestamp without time zone,
    collected_by character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: parcel_receives_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.parcel_receives_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: parcel_receives_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.parcel_receives_id_seq OWNED BY public.parcel_receives.id;


--
-- Name: password_reset_tokens; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.password_reset_tokens (
    id bigint NOT NULL,
    token character varying(255) NOT NULL,
    user_id bigint NOT NULL,
    expiry_date timestamp without time zone NOT NULL,
    status character varying(50) DEFAULT 'PENDING'::character varying NOT NULL,
    request_ip character varying(255),
    device_info character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    used_at timestamp without time zone
);


--
-- Name: password_reset_tokens_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.password_reset_tokens_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: password_reset_tokens_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.password_reset_tokens_id_seq OWNED BY public.password_reset_tokens.id;


--
-- Name: password_resets; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.password_resets (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    token uuid NOT NULL,
    status character varying(50) DEFAULT 'PENDING'::character varying NOT NULL,
    expires_at timestamp without time zone NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: password_resets_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.password_resets_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: password_resets_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.password_resets_id_seq OWNED BY public.password_resets.id;


--
-- Name: payment_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.payment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: payment_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.payment_id_seq OWNED BY public.fee_payment.id;


--
-- Name: permission_definitions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.permission_definitions (
    id character varying(100) NOT NULL,
    permission_key character varying(100) NOT NULL,
    module_key character varying(50) NOT NULL,
    resource_key character varying(50) NOT NULL,
    action_key character varying(50) NOT NULL,
    name character varying(255) NOT NULL,
    description text,
    supported_scope_types jsonb,
    requires_assignment boolean DEFAULT false,
    is_sensitive boolean DEFAULT false,
    is_system_permission boolean DEFAULT false,
    is_active boolean DEFAULT true,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: plan_modules; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.plan_modules (
    plan_id bigint NOT NULL,
    module_id bigint NOT NULL
);


--
-- Name: platform_modules; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.platform_modules (
    id bigint NOT NULL,
    code character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    description text,
    is_default boolean DEFAULT false,
    status character varying(50) DEFAULT 'ACTIVE'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    category character varying(100) DEFAULT 'CORE'::character varying,
    add_on_price numeric(10,2) DEFAULT 0.00,
    target_roles character varying(255) DEFAULT 'ADMIN'::character varying,
    sub_modules text
);


--
-- Name: platform_modules_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.platform_modules_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: platform_modules_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.platform_modules_id_seq OWNED BY public.platform_modules.id;


--
-- Name: recruitment_candidates; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.recruitment_candidates (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    name character varying(255) NOT NULL,
    phone character varying(50),
    email character varying(255),
    date_of_birth date,
    applying_for character varying(255),
    expected_salary numeric(19,2),
    marital_status character varying(50),
    work_experience text,
    interview_date timestamp without time zone,
    submission_date timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    status character varying(50) DEFAULT 'NEW_APPLICATION'::character varying,
    description text,
    document_url character varying(500),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: recruitment_candidates_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.recruitment_candidates_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: recruitment_candidates_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.recruitment_candidates_id_seq OWNED BY public.recruitment_candidates.id;


--
-- Name: role_permissions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.role_permissions (
    id bigint NOT NULL,
    school_id bigint,
    role_id character varying(50) NOT NULL,
    permission_id character varying(100) NOT NULL,
    scope_type character varying(50) NOT NULL,
    scope_config jsonb
);


--
-- Name: role_permissions_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.role_permissions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: role_permissions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.role_permissions_id_seq OWNED BY public.role_permissions.id;


--
-- Name: roles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.roles (
    id character varying(50) NOT NULL,
    school_id bigint,
    name character varying(100) NOT NULL,
    description text,
    is_system_role boolean DEFAULT false,
    is_active boolean DEFAULT true,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    archetype character varying(50) NOT NULL
);


--
-- Name: school_account; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.school_account (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    account_name character varying(255) NOT NULL,
    account_type character varying(50) NOT NULL,
    is_active boolean DEFAULT true,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: school_account_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.school_account_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: school_account_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.school_account_id_seq OWNED BY public.school_account.id;


--
-- Name: school_config_overrides; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.school_config_overrides (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    student_limit integer,
    enable_beta_features boolean DEFAULT false,
    custom_config text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: school_config_overrides_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.school_config_overrides_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: school_config_overrides_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.school_config_overrides_id_seq OWNED BY public.school_config_overrides.id;


--
-- Name: school_departments; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.school_departments (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    name character varying(255) NOT NULL,
    description text,
    status character varying(50) DEFAULT 'ACTIVE'::character varying,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: school_departments_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.school_departments_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: school_departments_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.school_departments_id_seq OWNED BY public.school_departments.id;


--
-- Name: school_designations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.school_designations (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    department_id bigint,
    name character varying(255) NOT NULL,
    description text,
    status character varying(50) DEFAULT 'ACTIVE'::character varying,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: school_designations_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.school_designations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: school_designations_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.school_designations_id_seq OWNED BY public.school_designations.id;


--
-- Name: schools; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.schools (
    id bigint CONSTRAINT school_id_not_null NOT NULL,
    name character varying(255) CONSTRAINT school_name_not_null NOT NULL,
    code character varying(255) CONSTRAINT school_code_not_null NOT NULL,
    contact_email character varying(255),
    contact_phone character varying(255),
    address character varying(255),
    status character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    subdomain character varying(255),
    metadata jsonb DEFAULT '{}'::jsonb,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    plan_id bigint,
    custom_price numeric(10,2),
    subscription_start date,
    renewal_date date,
    payment_status character varying(50) DEFAULT 'PAID'::character varying,
    onboarding_status character varying(50) DEFAULT 'LIVE'::character varying,
    city character varying(255),
    state character varying(255),
    pincode character varying(50)
);


--
-- Name: school_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.school_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: school_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.school_id_seq OWNED BY public.schools.id;


--
-- Name: school_leave_types; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.school_leave_types (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    name character varying(255) NOT NULL,
    days_allowed integer DEFAULT 0,
    is_paid boolean DEFAULT true,
    applicable_roles character varying(500),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: school_leave_types_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.school_leave_types_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: school_leave_types_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.school_leave_types_id_seq OWNED BY public.school_leave_types.id;


--
-- Name: school_module_access; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.school_module_access (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    module_id bigint NOT NULL,
    enabled boolean DEFAULT true,
    enabled_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: school_module_access_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.school_module_access_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: school_module_access_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.school_module_access_id_seq OWNED BY public.school_module_access.id;


--
-- Name: school_subscription_installments; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.school_subscription_installments (
    id bigint NOT NULL,
    subscription_id bigint NOT NULL,
    installment_number integer NOT NULL,
    amount numeric(10,2) NOT NULL,
    due_date date NOT NULL,
    status character varying(50) DEFAULT 'PENDING'::character varying NOT NULL,
    paid_date date,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: school_subscription_installments_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.school_subscription_installments_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: school_subscription_installments_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.school_subscription_installments_id_seq OWNED BY public.school_subscription_installments.id;


--
-- Name: school_subscriptions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.school_subscriptions (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    plan_id bigint NOT NULL,
    billing_period character varying(50) DEFAULT 'YEARLY'::character varying NOT NULL,
    total_students integer DEFAULT 0 NOT NULL,
    total_amount numeric(10,2) DEFAULT 0.00 NOT NULL,
    amount_paid numeric(10,2) DEFAULT 0.00 NOT NULL,
    remaining_amount numeric(10,2) DEFAULT 0.00 NOT NULL,
    status character varying(50) DEFAULT 'ACTIVE'::character varying NOT NULL,
    start_date date,
    end_date date,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: school_subscriptions_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.school_subscriptions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: school_subscriptions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.school_subscriptions_id_seq OWNED BY public.school_subscriptions.id;


--
-- Name: sections; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sections (
    id bigint NOT NULL,
    class_id bigint NOT NULL,
    school_id bigint NOT NULL,
    name character varying(50) NOT NULL,
    room_number character varying(50),
    capacity integer DEFAULT 40
);


--
-- Name: sections_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.sections_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sections_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.sections_id_seq OWNED BY public.sections.id;


--
-- Name: staff; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.staff (
    id bigint NOT NULL,
    user_id bigint,
    school_id bigint NOT NULL,
    department_id bigint,
    designation_id bigint,
    joining_date date,
    salary numeric(19,2),
    status character varying(255),
    metadata jsonb DEFAULT '{}'::jsonb,
    first_name character varying(100),
    last_name character varying(100),
    department character varying(100),
    designation character varying(100),
    photo_url character varying(255),
    phone character varying(20),
    email character varying(100),
    biometric_id character varying(100),
    date_of_birth date,
    gender character varying(20),
    marital_status character varying(50),
    father_name character varying(255),
    mother_name character varying(255),
    emergency_contact character varying(100),
    current_address text,
    permanent_address text,
    qualification character varying(255),
    work_experience text,
    contract_type character varying(100),
    work_shift character varying(100),
    location character varying(255),
    notes text
);


--
-- Name: staff_advances; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.staff_advances (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    staff_id bigint NOT NULL,
    advance_date date NOT NULL,
    amount numeric(19,2) NOT NULL,
    reason text,
    repayment_method character varying(100),
    status character varying(50) DEFAULT 'PENDING'::character varying,
    recovered_amount numeric(19,2) DEFAULT 0,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: staff_advances_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.staff_advances_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: staff_advances_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.staff_advances_id_seq OWNED BY public.staff_advances.id;


--
-- Name: staff_attendance; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.staff_attendance (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    staff_id bigint NOT NULL,
    attendance_date date NOT NULL,
    status character varying(50) NOT NULL,
    check_in_time time without time zone,
    check_out_time time without time zone,
    working_hours numeric(5,2),
    notes text,
    is_correction_requested boolean DEFAULT false,
    correction_status character varying(50),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: staff_attendance_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.staff_attendance_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: staff_attendance_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.staff_attendance_id_seq OWNED BY public.staff_attendance.id;


--
-- Name: staff_bank_accounts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.staff_bank_accounts (
    id bigint NOT NULL,
    staff_id bigint NOT NULL,
    account_holder_name character varying(255),
    account_number character varying(100),
    bank_name character varying(255),
    ifsc_code character varying(50),
    branch_name character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: staff_bank_accounts_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.staff_bank_accounts_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: staff_bank_accounts_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.staff_bank_accounts_id_seq OWNED BY public.staff_bank_accounts.id;


--
-- Name: staff_documents; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.staff_documents (
    id bigint NOT NULL,
    staff_id bigint NOT NULL,
    document_type character varying(100) NOT NULL,
    file_name character varying(255) NOT NULL,
    file_url character varying(500) NOT NULL,
    uploaded_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: staff_documents_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.staff_documents_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: staff_documents_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.staff_documents_id_seq OWNED BY public.staff_documents.id;


--
-- Name: staff_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.staff_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: staff_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.staff_id_seq OWNED BY public.staff.id;


--
-- Name: staff_leave_balances; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.staff_leave_balances (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    staff_id bigint NOT NULL,
    leave_type_id bigint NOT NULL,
    total_leaves numeric(5,2) DEFAULT 0,
    used_leaves numeric(5,2) DEFAULT 0,
    remaining_leaves numeric(5,2) DEFAULT 0,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: staff_leave_balances_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.staff_leave_balances_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: staff_leave_balances_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.staff_leave_balances_id_seq OWNED BY public.staff_leave_balances.id;


--
-- Name: staff_leaves; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.staff_leaves (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    staff_id bigint NOT NULL,
    leave_type_id bigint NOT NULL,
    start_date date NOT NULL,
    end_date date NOT NULL,
    number_of_days numeric(5,2) NOT NULL,
    reason text,
    status character varying(50) DEFAULT 'PENDING'::character varying,
    approved_by bigint,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: staff_leaves_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.staff_leaves_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: staff_leaves_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.staff_leaves_id_seq OWNED BY public.staff_leaves.id;


--
-- Name: staff_payroll_details; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.staff_payroll_details (
    id bigint NOT NULL,
    staff_id bigint NOT NULL,
    basic_salary numeric(19,2) DEFAULT 0,
    hra numeric(19,2) DEFAULT 0,
    ta numeric(19,2) DEFAULT 0,
    da numeric(19,2) DEFAULT 0,
    special_allowance numeric(19,2) DEFAULT 0,
    pf numeric(19,2) DEFAULT 0,
    epf_number character varying(100),
    tds numeric(19,2) DEFAULT 0,
    esic numeric(19,2) DEFAULT 0,
    other_deductions numeric(19,2) DEFAULT 0,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: staff_payroll_details_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.staff_payroll_details_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: staff_payroll_details_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.staff_payroll_details_id_seq OWNED BY public.staff_payroll_details.id;


--
-- Name: staff_payrolls; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.staff_payrolls (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    staff_id bigint NOT NULL,
    payroll_month character varying(20) NOT NULL,
    payroll_year integer NOT NULL,
    basic_salary numeric(19,2) NOT NULL,
    total_earnings numeric(19,2) NOT NULL,
    total_deductions numeric(19,2) NOT NULL,
    net_payable_salary numeric(19,2) NOT NULL,
    status character varying(50) DEFAULT 'PENDING'::character varying,
    payment_date date,
    details_json jsonb,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: staff_payrolls_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.staff_payrolls_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: staff_payrolls_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.staff_payrolls_id_seq OWNED BY public.staff_payrolls.id;


--
-- Name: staff_tasks; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.staff_tasks (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    staff_id bigint NOT NULL,
    title character varying(255) NOT NULL,
    description text,
    start_date date,
    due_date date,
    priority character varying(50) DEFAULT 'MEDIUM'::character varying,
    status character varying(50) DEFAULT 'PENDING'::character varying,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: staff_tasks_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.staff_tasks_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: staff_tasks_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.staff_tasks_id_seq OWNED BY public.staff_tasks.id;


--
-- Name: student; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.student (
    id bigint NOT NULL,
    user_id bigint,
    admission_no character varying(255) NOT NULL,
    roll_number character varying(255),
    class_id bigint NOT NULL,
    section_id bigint,
    academic_year_id bigint,
    school_id bigint NOT NULL,
    status character varying(255) NOT NULL,
    admission_date date,
    name character varying(255),
    metadata jsonb DEFAULT '{}'::jsonb,
    first_name character varying(100),
    last_name character varying(100),
    gender character varying(20),
    date_of_birth date,
    blood_group character varying(10),
    religion character varying(50),
    nationality character varying(50),
    previous_school character varying(200),
    address text,
    photo_url character varying(255),
    guardian_name character varying(100),
    guardian_relation character varying(50),
    guardian_phone character varying(20),
    guardian_email character varying(100),
    guardian_occupation character varying(100),
    category_id bigint,
    house_id bigint,
    family_id bigint
);


--
-- Name: student_attendance; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.student_attendance (
    id bigint NOT NULL,
    student_id bigint NOT NULL,
    school_id bigint NOT NULL,
    attendance_date date NOT NULL,
    status character varying(255) NOT NULL
);


--
-- Name: student_attendance_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.student_attendance_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: student_attendance_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.student_attendance_id_seq OWNED BY public.student_attendance.id;


--
-- Name: student_categories; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.student_categories (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    name character varying(100) NOT NULL,
    description character varying(255)
);


--
-- Name: student_categories_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.student_categories_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: student_categories_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.student_categories_id_seq OWNED BY public.student_categories.id;


--
-- Name: student_certificates; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.student_certificates (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    student_id bigint NOT NULL,
    certificate_type character varying(100) NOT NULL,
    issue_date date,
    status character varying(50) DEFAULT 'PENDING'::character varying NOT NULL,
    remarks text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone
);


--
-- Name: student_certificates_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.student_certificates_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: student_certificates_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.student_certificates_id_seq OWNED BY public.student_certificates.id;


--
-- Name: student_documents; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.student_documents (
    id bigint NOT NULL,
    student_id bigint NOT NULL,
    school_id bigint NOT NULL,
    document_name character varying(255) NOT NULL,
    document_type character varying(100),
    file_url character varying(500),
    uploaded_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: student_documents_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.student_documents_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: student_documents_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.student_documents_id_seq OWNED BY public.student_documents.id;


--
-- Name: student_fee_structure; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.student_fee_structure (
    id bigint NOT NULL,
    student_id bigint NOT NULL,
    fee_structure_id bigint NOT NULL,
    academic_year_id bigint,
    assigned_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    collection_plan_id bigint
);


--
-- Name: student_fee_structure_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.student_fee_structure_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: student_fee_structure_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.student_fee_structure_id_seq OWNED BY public.student_fee_structure.id;


--
-- Name: student_houses; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.student_houses (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    name character varying(100) NOT NULL,
    color_code character varying(100),
    description character varying(255)
);


--
-- Name: student_houses_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.student_houses_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: student_houses_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.student_houses_id_seq OWNED BY public.student_houses.id;


--
-- Name: student_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.student_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: student_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.student_id_seq OWNED BY public.student.id;


--
-- Name: student_leaves; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.student_leaves (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    student_id bigint NOT NULL,
    apply_date date NOT NULL,
    from_date date NOT NULL,
    to_date date NOT NULL,
    days integer,
    reason text,
    status character varying(50) DEFAULT 'PENDING'::character varying NOT NULL,
    reply text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone
);


--
-- Name: student_leaves_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.student_leaves_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: student_leaves_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.student_leaves_id_seq OWNED BY public.student_leaves.id;


--
-- Name: student_parents; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.student_parents (
    student_id bigint NOT NULL,
    parent_user_id bigint NOT NULL,
    relation character varying(100),
    is_primary boolean DEFAULT false NOT NULL
);


--
-- Name: student_referrals; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.student_referrals (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    referral_by character varying(255) NOT NULL,
    student_name character varying(255) NOT NULL,
    email character varying(255),
    mobile character varying(50) NOT NULL,
    note text,
    status character varying(50) DEFAULT 'PENDING'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone
);


--
-- Name: student_referrals_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.student_referrals_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: student_referrals_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.student_referrals_id_seq OWNED BY public.student_referrals.id;


--
-- Name: student_siblings; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.student_siblings (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    primary_student_id bigint NOT NULL,
    sibling_student_id bigint NOT NULL,
    relationship character varying(255) NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: student_siblings_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.student_siblings_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: student_siblings_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.student_siblings_id_seq OWNED BY public.student_siblings.id;


--
-- Name: student_subject_enrollments; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.student_subject_enrollments (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    student_id bigint NOT NULL,
    subject_id bigint NOT NULL,
    academic_year_id bigint NOT NULL,
    enrollment_type character varying(20) DEFAULT 'CORE'::character varying NOT NULL,
    status character varying(20) DEFAULT 'CONFIRMED'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: student_subject_enrollments_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.student_subject_enrollments_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: student_subject_enrollments_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.student_subject_enrollments_id_seq OWNED BY public.student_subject_enrollments.id;


--
-- Name: subject_attendance; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.subject_attendance (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    student_id bigint NOT NULL,
    class_id bigint NOT NULL,
    section_id bigint,
    subject_id bigint NOT NULL,
    timetable_entry_id bigint,
    attendance_date date NOT NULL,
    status character varying(20) NOT NULL,
    remarks text,
    marked_by bigint,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: subject_attendance_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.subject_attendance_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: subject_attendance_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.subject_attendance_id_seq OWNED BY public.subject_attendance.id;


--
-- Name: subjects; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.subjects (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    code character varying(50),
    name character varying(100) NOT NULL,
    type character varying(50) DEFAULT 'THEORY'::character varying,
    credits integer DEFAULT 3,
    grade_level character varying(50),
    status character varying(20) DEFAULT 'ACTIVE'::character varying NOT NULL
);


--
-- Name: subjects_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.subjects_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: subjects_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.subjects_id_seq OWNED BY public.subjects.id;


--
-- Name: subscription_plans; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.subscription_plans (
    id bigint NOT NULL,
    name character varying(255) NOT NULL,
    monthly_price numeric(10,2),
    max_students integer,
    storage_gb integer,
    status character varying(50) DEFAULT 'ACTIVE'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    code character varying(100),
    annual_price numeric(10,2),
    description text,
    features text,
    billing_model character varying(50) DEFAULT 'PER_STUDENT'::character varying,
    price_per_student numeric(10,2)
);


--
-- Name: subscription_plans_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.subscription_plans_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: subscription_plans_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.subscription_plans_id_seq OWNED BY public.subscription_plans.id;


--
-- Name: super_admin_employees; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.super_admin_employees (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    department character varying(255),
    designation character varying(255),
    employee_code character varying(100),
    joined_at date,
    salary_band character varying(100),
    leave_balance integer DEFAULT 0,
    metadata jsonb,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: super_admin_employees_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.super_admin_employees_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: super_admin_employees_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.super_admin_employees_id_seq OWNED BY public.super_admin_employees.id;


--
-- Name: support_tickets; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.support_tickets (
    id bigint NOT NULL,
    ticket_code character varying(50) NOT NULL,
    school_id bigint NOT NULL,
    creator_user_id bigint NOT NULL,
    portal_source character varying(50) NOT NULL,
    category_id bigint,
    priority character varying(50) DEFAULT 'LOW'::character varying,
    subject character varying(255) NOT NULL,
    description text,
    attachment_url character varying(500),
    status character varying(50) DEFAULT 'NEW'::character varying,
    assigned_employee_id bigint,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    closed_at timestamp without time zone
);


--
-- Name: support_tickets_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.support_tickets_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: support_tickets_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.support_tickets_id_seq OWNED BY public.support_tickets.id;


--
-- Name: tenant_entitlement_overrides; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tenant_entitlement_overrides (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    module_code character varying(100) NOT NULL,
    override_type character varying(50) NOT NULL,
    expires_at timestamp without time zone,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: tenant_entitlement_overrides_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tenant_entitlement_overrides_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tenant_entitlement_overrides_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tenant_entitlement_overrides_id_seq OWNED BY public.tenant_entitlement_overrides.id;


--
-- Name: ticket_categories; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ticket_categories (
    id bigint NOT NULL,
    name character varying(255) NOT NULL,
    department character varying(100),
    status character varying(50) DEFAULT 'ACTIVE'::character varying,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: ticket_categories_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.ticket_categories_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: ticket_categories_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.ticket_categories_id_seq OWNED BY public.ticket_categories.id;


--
-- Name: ticket_histories; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ticket_histories (
    id bigint NOT NULL,
    ticket_id bigint NOT NULL,
    old_status character varying(50),
    new_status character varying(50),
    employee_id bigint,
    remark text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: ticket_histories_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.ticket_histories_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: ticket_histories_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.ticket_histories_id_seq OWNED BY public.ticket_histories.id;


--
-- Name: timetable_entries; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.timetable_entries (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    class_id bigint NOT NULL,
    section_id bigint,
    academic_year_id bigint NOT NULL,
    day_of_week character varying(20) NOT NULL,
    period_id bigint NOT NULL,
    subject_id bigint,
    teacher_id bigint,
    room_number character varying(50),
    status character varying(20) DEFAULT 'ACTIVE'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: timetable_entries_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.timetable_entries_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: timetable_entries_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.timetable_entries_id_seq OWNED BY public.timetable_entries.id;


--
-- Name: timetable_periods; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.timetable_periods (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    period_number integer NOT NULL,
    name character varying(50) NOT NULL,
    start_time character varying(20),
    end_time character varying(20),
    is_break boolean DEFAULT false
);


--
-- Name: timetable_periods_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.timetable_periods_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: timetable_periods_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.timetable_periods_id_seq OWNED BY public.timetable_periods.id;


--
-- Name: user_activity_logs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_activity_logs (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    school_id bigint,
    role character varying(50) NOT NULL,
    module character varying(100) NOT NULL,
    action character varying(255) NOT NULL,
    ip_address character varying(255),
    "timestamp" timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    status character varying(50) DEFAULT 'SUCCESS'::character varying,
    browser character varying(255),
    device character varying(255)
);


--
-- Name: user_activity_logs_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.user_activity_logs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: user_activity_logs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.user_activity_logs_id_seq OWNED BY public.user_activity_logs.id;


--
-- Name: user_assignments; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_assignments (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    user_id bigint NOT NULL,
    academic_session_id bigint NOT NULL,
    assignment_type character varying(50) NOT NULL,
    class_id bigint,
    section_id bigint,
    subject_id bigint,
    student_id bigint,
    effective_from timestamp without time zone,
    effective_to timestamp without time zone,
    is_active boolean DEFAULT true,
    department_id bigint
);


--
-- Name: user_assignments_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.user_assignments_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: user_assignments_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.user_assignments_id_seq OWNED BY public.user_assignments.id;


--
-- Name: user_login_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_login_history (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    school_id bigint,
    device character varying(255),
    browser character varying(255),
    ip_address character varying(255),
    status character varying(50) NOT NULL,
    login_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    logout_time timestamp without time zone
);


--
-- Name: user_login_history_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.user_login_history_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: user_login_history_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.user_login_history_id_seq OWNED BY public.user_login_history.id;


--
-- Name: user_requests; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_requests (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    school_id bigint,
    request_type character varying(100) NOT NULL,
    status character varying(50) DEFAULT 'PENDING'::character varying NOT NULL,
    notes text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: user_requests_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.user_requests_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: user_requests_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.user_requests_id_seq OWNED BY public.user_requests.id;


--
-- Name: user_role_mappings; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_role_mappings (
    id bigint NOT NULL,
    school_id bigint,
    user_id bigint NOT NULL,
    role_id character varying(50) NOT NULL,
    academic_session_id bigint,
    is_active boolean DEFAULT true
);


--
-- Name: user_role_mappings_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.user_role_mappings_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: user_role_mappings_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.user_role_mappings_id_seq OWNED BY public.user_role_mappings.id;


--
-- Name: user_school_roles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_school_roles (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    school_id bigint,
    role public.user_role NOT NULL,
    status character varying(50) DEFAULT 'ACTIVE'::character varying NOT NULL,
    joined_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    left_at timestamp without time zone
);


--
-- Name: user_school_roles_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.user_school_roles_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: user_school_roles_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.user_school_roles_id_seq OWNED BY public.user_school_roles.id;


--
-- Name: users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.users (
    id bigint NOT NULL,
    phone character varying(20) NOT NULL,
    name character varying(255),
    email character varying(255),
    password_hash character varying(255),
    status character varying(50) DEFAULT 'ACTIVE'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    metadata jsonb DEFAULT '{}'::jsonb,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;


--
-- Name: visitor_logs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.visitor_logs (
    id bigint NOT NULL,
    school_id bigint NOT NULL,
    visitor_name character varying(255) NOT NULL,
    purpose character varying(100) NOT NULL,
    meeting_with character varying(255),
    meeting_with_staff_id bigint,
    phone character varying(50),
    email character varying(255),
    number_of_people integer DEFAULT 1,
    id_card_number character varying(100),
    visit_date date NOT NULL,
    time_in time without time zone NOT NULL,
    est_time_out time without time zone,
    time_out time without time zone,
    status character varying(50) DEFAULT 'ON_SITE'::character varying,
    note text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: visitor_logs_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.visitor_logs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: visitor_logs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.visitor_logs_id_seq OWNED BY public.visitor_logs.id;


--
-- Name: academic_years id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.academic_years ALTER COLUMN id SET DEFAULT nextval('public.academic_years_id_seq'::regclass);


--
-- Name: account_requests id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.account_requests ALTER COLUMN id SET DEFAULT nextval('public.account_requests_id_seq'::regclass);


--
-- Name: admission_enquiries id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.admission_enquiries ALTER COLUMN id SET DEFAULT nextval('public.admission_enquiries_id_seq'::regclass);


--
-- Name: admission_enquiry_follow_ups id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.admission_enquiry_follow_ups ALTER COLUMN id SET DEFAULT nextval('public.admission_enquiry_follow_ups_id_seq'::regclass);


--
-- Name: audit_logs id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.audit_logs ALTER COLUMN id SET DEFAULT nextval('public.audit_logs_id_seq'::regclass);


--
-- Name: auth_sessions id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.auth_sessions ALTER COLUMN id SET DEFAULT nextval('public.auth_sessions_id_seq'::regclass);


--
-- Name: class id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.class ALTER COLUMN id SET DEFAULT nextval('public.class_id_seq'::regclass);


--
-- Name: class_subject_assignments id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.class_subject_assignments ALTER COLUMN id SET DEFAULT nextval('public.class_subject_assignments_id_seq'::regclass);


--
-- Name: class_teacher_assignments id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.class_teacher_assignments ALTER COLUMN id SET DEFAULT nextval('public.class_teacher_assignments_id_seq'::regclass);


--
-- Name: collection_plan id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.collection_plan ALTER COLUMN id SET DEFAULT nextval('public.collection_plan_id_seq'::regclass);


--
-- Name: collection_plan_item id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.collection_plan_item ALTER COLUMN id SET DEFAULT nextval('public.collection_plan_item_id_seq'::regclass);


--
-- Name: communication_announcements id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.communication_announcements ALTER COLUMN id SET DEFAULT nextval('public.communication_announcements_id_seq'::regclass);


--
-- Name: communication_deliveries id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.communication_deliveries ALTER COLUMN id SET DEFAULT nextval('public.communication_deliveries_id_seq'::regclass);


--
-- Name: communication_templates id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.communication_templates ALTER COLUMN id SET DEFAULT nextval('public.communication_templates_id_seq'::regclass);


--
-- Name: crm_activity_logs id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.crm_activity_logs ALTER COLUMN id SET DEFAULT nextval('public.crm_activity_logs_id_seq'::regclass);


--
-- Name: crm_demos id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.crm_demos ALTER COLUMN id SET DEFAULT nextval('public.crm_demos_id_seq'::regclass);


--
-- Name: crm_follow_ups id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.crm_follow_ups ALTER COLUMN id SET DEFAULT nextval('public.crm_follow_ups_id_seq'::regclass);


--
-- Name: crm_leads id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.crm_leads ALTER COLUMN id SET DEFAULT nextval('public.crm_leads_id_seq'::regclass);


--
-- Name: crm_quotations id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.crm_quotations ALTER COLUMN id SET DEFAULT nextval('public.crm_quotations_id_seq'::regclass);


--
-- Name: data_import_errors error_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_import_errors ALTER COLUMN error_id SET DEFAULT nextval('public.data_import_errors_error_id_seq'::regclass);


--
-- Name: data_import_jobs job_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_import_jobs ALTER COLUMN job_id SET DEFAULT nextval('public.data_import_jobs_job_id_seq'::regclass);


--
-- Name: departments id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.departments ALTER COLUMN id SET DEFAULT nextval('public.departments_id_seq'::regclass);


--
-- Name: employee_assets id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_assets ALTER COLUMN id SET DEFAULT nextval('public.employee_assets_id_seq'::regclass);


--
-- Name: employee_attendance id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_attendance ALTER COLUMN id SET DEFAULT nextval('public.employee_attendance_id_seq'::regclass);


--
-- Name: employee_audit_logs id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_audit_logs ALTER COLUMN id SET DEFAULT nextval('public.employee_audit_logs_id_seq'::regclass);


--
-- Name: employee_documents id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_documents ALTER COLUMN id SET DEFAULT nextval('public.employee_documents_id_seq'::regclass);


--
-- Name: employee_leaves id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_leaves ALTER COLUMN id SET DEFAULT nextval('public.employee_leaves_id_seq'::regclass);


--
-- Name: employee_lifecycle id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_lifecycle ALTER COLUMN id SET DEFAULT nextval('public.employee_lifecycle_id_seq'::regclass);


--
-- Name: employee_notes id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_notes ALTER COLUMN id SET DEFAULT nextval('public.employee_notes_id_seq'::regclass);


--
-- Name: employee_payroll id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_payroll ALTER COLUMN id SET DEFAULT nextval('public.employee_payroll_id_seq'::regclass);


--
-- Name: employee_performance id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_performance ALTER COLUMN id SET DEFAULT nextval('public.employee_performance_id_seq'::regclass);


--
-- Name: employee_timeline id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_timeline ALTER COLUMN id SET DEFAULT nextval('public.employee_timeline_id_seq'::regclass);


--
-- Name: exam_admit_cards id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_admit_cards ALTER COLUMN id SET DEFAULT nextval('public.exam_admit_cards_id_seq'::regclass);


--
-- Name: exam_attendances id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_attendances ALTER COLUMN id SET DEFAULT nextval('public.exam_attendances_id_seq'::regclass);


--
-- Name: exam_co_curricular_grades id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_co_curricular_grades ALTER COLUMN id SET DEFAULT nextval('public.exam_co_curricular_grades_id_seq'::regclass);


--
-- Name: exam_divisions id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_divisions ALTER COLUMN id SET DEFAULT nextval('public.exam_divisions_id_seq'::regclass);


--
-- Name: exam_grade_scales id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_grade_scales ALTER COLUMN id SET DEFAULT nextval('public.exam_grade_scales_id_seq'::regclass);


--
-- Name: exam_marks id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_marks ALTER COLUMN id SET DEFAULT nextval('public.exam_marks_id_seq'::regclass);


--
-- Name: exam_report_card_batches id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_report_card_batches ALTER COLUMN id SET DEFAULT nextval('public.exam_report_card_batches_id_seq'::regclass);


--
-- Name: exam_report_cards id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_report_cards ALTER COLUMN id SET DEFAULT nextval('public.exam_report_cards_id_seq'::regclass);


--
-- Name: exam_schedules id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_schedules ALTER COLUMN id SET DEFAULT nextval('public.exam_schedules_id_seq'::regclass);


--
-- Name: exam_setups id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_setups ALTER COLUMN id SET DEFAULT nextval('public.exam_setups_id_seq'::regclass);


--
-- Name: exam_teacher_remarks id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_teacher_remarks ALTER COLUMN id SET DEFAULT nextval('public.exam_teacher_remarks_id_seq'::regclass);


--
-- Name: exam_terms id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_terms ALTER COLUMN id SET DEFAULT nextval('public.exam_terms_id_seq'::regclass);


--
-- Name: families id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.families ALTER COLUMN id SET DEFAULT nextval('public.families_id_seq'::regclass);


--
-- Name: fee_category id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fee_category ALTER COLUMN id SET DEFAULT nextval('public.fee_category_id_seq'::regclass);


--
-- Name: fee_due id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fee_due ALTER COLUMN id SET DEFAULT nextval('public.fee_due_id_seq'::regclass);


--
-- Name: fee_generation_batch id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fee_generation_batch ALTER COLUMN id SET DEFAULT nextval('public.fee_generation_batch_id_seq'::regclass);


--
-- Name: fee_invoice id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fee_invoice ALTER COLUMN id SET DEFAULT nextval('public.fee_invoice_id_seq'::regclass);


--
-- Name: fee_payment id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fee_payment ALTER COLUMN id SET DEFAULT nextval('public.payment_id_seq'::regclass);


--
-- Name: fee_payment_allocation id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fee_payment_allocation ALTER COLUMN id SET DEFAULT nextval('public.fee_payment_allocation_id_seq'::regclass);


--
-- Name: fee_reminders id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fee_reminders ALTER COLUMN id SET DEFAULT nextval('public.fee_reminders_id_seq'::regclass);


--
-- Name: fee_structure id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fee_structure ALTER COLUMN id SET DEFAULT nextval('public.fee_structure_id_seq'::regclass);


--
-- Name: fee_structure_item id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fee_structure_item ALTER COLUMN id SET DEFAULT nextval('public.fee_structure_item_id_seq'::regclass);


--
-- Name: gate_passes id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gate_passes ALTER COLUMN id SET DEFAULT nextval('public.gate_passes_id_seq'::regclass);


--
-- Name: id_card_generations id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.id_card_generations ALTER COLUMN id SET DEFAULT nextval('public.id_card_generations_id_seq'::regclass);


--
-- Name: impersonation_sessions id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.impersonation_sessions ALTER COLUMN id SET DEFAULT nextval('public.impersonation_sessions_id_seq'::regclass);


--
-- Name: onboarding_drafts school_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.onboarding_drafts ALTER COLUMN school_id SET DEFAULT nextval('public.onboarding_drafts_school_id_seq'::regclass);


--
-- Name: online_admissions id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.online_admissions ALTER COLUMN id SET DEFAULT nextval('public.online_admissions_id_seq'::regclass);


--
-- Name: parcel_dispatches id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcel_dispatches ALTER COLUMN id SET DEFAULT nextval('public.parcel_dispatches_id_seq'::regclass);


--
-- Name: parcel_receives id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcel_receives ALTER COLUMN id SET DEFAULT nextval('public.parcel_receives_id_seq'::regclass);


--
-- Name: password_reset_tokens id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.password_reset_tokens ALTER COLUMN id SET DEFAULT nextval('public.password_reset_tokens_id_seq'::regclass);


--
-- Name: password_resets id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.password_resets ALTER COLUMN id SET DEFAULT nextval('public.password_resets_id_seq'::regclass);


--
-- Name: platform_modules id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.platform_modules ALTER COLUMN id SET DEFAULT nextval('public.platform_modules_id_seq'::regclass);


--
-- Name: recruitment_candidates id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recruitment_candidates ALTER COLUMN id SET DEFAULT nextval('public.recruitment_candidates_id_seq'::regclass);


--
-- Name: role_permissions id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.role_permissions ALTER COLUMN id SET DEFAULT nextval('public.role_permissions_id_seq'::regclass);


--
-- Name: school_account id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.school_account ALTER COLUMN id SET DEFAULT nextval('public.school_account_id_seq'::regclass);


--
-- Name: school_config_overrides id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.school_config_overrides ALTER COLUMN id SET DEFAULT nextval('public.school_config_overrides_id_seq'::regclass);


--
-- Name: school_departments id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.school_departments ALTER COLUMN id SET DEFAULT nextval('public.school_departments_id_seq'::regclass);


--
-- Name: school_designations id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.school_designations ALTER COLUMN id SET DEFAULT nextval('public.school_designations_id_seq'::regclass);


--
-- Name: school_leave_types id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.school_leave_types ALTER COLUMN id SET DEFAULT nextval('public.school_leave_types_id_seq'::regclass);


--
-- Name: school_module_access id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.school_module_access ALTER COLUMN id SET DEFAULT nextval('public.school_module_access_id_seq'::regclass);


--
-- Name: school_subscription_installments id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.school_subscription_installments ALTER COLUMN id SET DEFAULT nextval('public.school_subscription_installments_id_seq'::regclass);


--
-- Name: school_subscriptions id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.school_subscriptions ALTER COLUMN id SET DEFAULT nextval('public.school_subscriptions_id_seq'::regclass);


--
-- Name: schools id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.schools ALTER COLUMN id SET DEFAULT nextval('public.school_id_seq'::regclass);


--
-- Name: sections id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sections ALTER COLUMN id SET DEFAULT nextval('public.sections_id_seq'::regclass);


--
-- Name: staff id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.staff ALTER COLUMN id SET DEFAULT nextval('public.staff_id_seq'::regclass);


--
-- Name: staff_advances id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.staff_advances ALTER COLUMN id SET DEFAULT nextval('public.staff_advances_id_seq'::regclass);


--
-- Name: staff_attendance id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.staff_attendance ALTER COLUMN id SET DEFAULT nextval('public.staff_attendance_id_seq'::regclass);


--
-- Name: staff_bank_accounts id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.staff_bank_accounts ALTER COLUMN id SET DEFAULT nextval('public.staff_bank_accounts_id_seq'::regclass);


--
-- Name: staff_documents id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.staff_documents ALTER COLUMN id SET DEFAULT nextval('public.staff_documents_id_seq'::regclass);


--
-- Name: staff_leave_balances id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.staff_leave_balances ALTER COLUMN id SET DEFAULT nextval('public.staff_leave_balances_id_seq'::regclass);


--
-- Name: staff_leaves id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.staff_leaves ALTER COLUMN id SET DEFAULT nextval('public.staff_leaves_id_seq'::regclass);


--
-- Name: staff_payroll_details id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.staff_payroll_details ALTER COLUMN id SET DEFAULT nextval('public.staff_payroll_details_id_seq'::regclass);


--
-- Name: staff_payrolls id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.staff_payrolls ALTER COLUMN id SET DEFAULT nextval('public.staff_payrolls_id_seq'::regclass);


--
-- Name: staff_tasks id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.staff_tasks ALTER COLUMN id SET DEFAULT nextval('public.staff_tasks_id_seq'::regclass);


--
-- Name: student id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student ALTER COLUMN id SET DEFAULT nextval('public.student_id_seq'::regclass);


--
-- Name: student_attendance id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_attendance ALTER COLUMN id SET DEFAULT nextval('public.student_attendance_id_seq'::regclass);


--
-- Name: student_categories id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_categories ALTER COLUMN id SET DEFAULT nextval('public.student_categories_id_seq'::regclass);


--
-- Name: student_certificates id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_certificates ALTER COLUMN id SET DEFAULT nextval('public.student_certificates_id_seq'::regclass);


--
-- Name: student_documents id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_documents ALTER COLUMN id SET DEFAULT nextval('public.student_documents_id_seq'::regclass);


--
-- Name: student_fee_structure id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_fee_structure ALTER COLUMN id SET DEFAULT nextval('public.student_fee_structure_id_seq'::regclass);


--
-- Name: student_houses id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_houses ALTER COLUMN id SET DEFAULT nextval('public.student_houses_id_seq'::regclass);


--
-- Name: student_leaves id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_leaves ALTER COLUMN id SET DEFAULT nextval('public.student_leaves_id_seq'::regclass);


--
-- Name: student_referrals id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_referrals ALTER COLUMN id SET DEFAULT nextval('public.student_referrals_id_seq'::regclass);


--
-- Name: student_siblings id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_siblings ALTER COLUMN id SET DEFAULT nextval('public.student_siblings_id_seq'::regclass);


--
-- Name: student_subject_enrollments id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_subject_enrollments ALTER COLUMN id SET DEFAULT nextval('public.student_subject_enrollments_id_seq'::regclass);


--
-- Name: subject_attendance id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.subject_attendance ALTER COLUMN id SET DEFAULT nextval('public.subject_attendance_id_seq'::regclass);


--
-- Name: subjects id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.subjects ALTER COLUMN id SET DEFAULT nextval('public.subjects_id_seq'::regclass);


--
-- Name: subscription_plans id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.subscription_plans ALTER COLUMN id SET DEFAULT nextval('public.subscription_plans_id_seq'::regclass);


--
-- Name: super_admin_employees id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.super_admin_employees ALTER COLUMN id SET DEFAULT nextval('public.super_admin_employees_id_seq'::regclass);


--
-- Name: support_tickets id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.support_tickets ALTER COLUMN id SET DEFAULT nextval('public.support_tickets_id_seq'::regclass);


--
-- Name: tenant_entitlement_overrides id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tenant_entitlement_overrides ALTER COLUMN id SET DEFAULT nextval('public.tenant_entitlement_overrides_id_seq'::regclass);


--
-- Name: ticket_categories id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ticket_categories ALTER COLUMN id SET DEFAULT nextval('public.ticket_categories_id_seq'::regclass);


--
-- Name: ticket_histories id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ticket_histories ALTER COLUMN id SET DEFAULT nextval('public.ticket_histories_id_seq'::regclass);


--
-- Name: timetable_entries id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.timetable_entries ALTER COLUMN id SET DEFAULT nextval('public.timetable_entries_id_seq'::regclass);


--
-- Name: timetable_periods id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.timetable_periods ALTER COLUMN id SET DEFAULT nextval('public.timetable_periods_id_seq'::regclass);


--
-- Name: user_activity_logs id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_activity_logs ALTER COLUMN id SET DEFAULT nextval('public.user_activity_logs_id_seq'::regclass);


--
-- Name: user_assignments id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_assignments ALTER COLUMN id SET DEFAULT nextval('public.user_assignments_id_seq'::regclass);


--
-- Name: user_login_history id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_login_history ALTER COLUMN id SET DEFAULT nextval('public.user_login_history_id_seq'::regclass);


--
-- Name: user_requests id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_requests ALTER COLUMN id SET DEFAULT nextval('public.user_requests_id_seq'::regclass);


--
-- Name: user_role_mappings id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_role_mappings ALTER COLUMN id SET DEFAULT nextval('public.user_role_mappings_id_seq'::regclass);


--
-- Name: user_school_roles id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_school_roles ALTER COLUMN id SET DEFAULT nextval('public.user_school_roles_id_seq'::regclass);


--
-- Name: users id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- Name: visitor_logs id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.visitor_logs ALTER COLUMN id SET DEFAULT nextval('public.visitor_logs_id_seq'::regclass);


--
-- Name: academic_years academic_years_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.academic_years
    ADD CONSTRAINT academic_years_pkey PRIMARY KEY (id);


--
-- Name: account_requests account_requests_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.account_requests
    ADD CONSTRAINT account_requests_pkey PRIMARY KEY (id);


--
-- Name: admission_enquiries admission_enquiries_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.admission_enquiries
    ADD CONSTRAINT admission_enquiries_pkey PRIMARY KEY (id);


--
-- Name: admission_enquiry_follow_ups admission_enquiry_follow_ups_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.admission_enquiry_follow_ups
    ADD CONSTRAINT admission_enquiry_follow_ups_pkey PRIMARY KEY (id);


--
-- Name: audit_logs audit_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.audit_logs
    ADD CONSTRAINT audit_logs_pkey PRIMARY KEY (id);


--
-- Name: auth_sessions auth_sessions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.auth_sessions
    ADD CONSTRAINT auth_sessions_pkey PRIMARY KEY (id);


--
-- Name: class class_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.class
    ADD CONSTRAINT class_pkey PRIMARY KEY (id);


--
-- Name: class_subject_assignments class_subject_assignments_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.class_subject_assignments
    ADD CONSTRAINT class_subject_assignments_pkey PRIMARY KEY (id);


--
-- Name: class_teacher_assignments class_teacher_assignments_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.class_teacher_assignments
    ADD CONSTRAINT class_teacher_assignments_pkey PRIMARY KEY (id);


--
-- Name: collection_plan_item collection_plan_item_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.collection_plan_item
    ADD CONSTRAINT collection_plan_item_pkey PRIMARY KEY (id);


--
-- Name: collection_plan collection_plan_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.collection_plan
    ADD CONSTRAINT collection_plan_pkey PRIMARY KEY (id);


--
-- Name: communication_announcement_schools communication_announcement_schools_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.communication_announcement_schools
    ADD CONSTRAINT communication_announcement_schools_pkey PRIMARY KEY (announcement_id, school_id);


--
-- Name: communication_announcements communication_announcements_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.communication_announcements
    ADD CONSTRAINT communication_announcements_pkey PRIMARY KEY (id);


--
-- Name: communication_deliveries communication_deliveries_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.communication_deliveries
    ADD CONSTRAINT communication_deliveries_pkey PRIMARY KEY (id);


--
-- Name: communication_templates communication_templates_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.communication_templates
    ADD CONSTRAINT communication_templates_pkey PRIMARY KEY (id);


--
-- Name: crm_activity_logs crm_activity_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.crm_activity_logs
    ADD CONSTRAINT crm_activity_logs_pkey PRIMARY KEY (id);


--
-- Name: crm_demos crm_demos_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.crm_demos
    ADD CONSTRAINT crm_demos_pkey PRIMARY KEY (id);


--
-- Name: crm_follow_ups crm_follow_ups_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.crm_follow_ups
    ADD CONSTRAINT crm_follow_ups_pkey PRIMARY KEY (id);


--
-- Name: crm_leads crm_leads_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.crm_leads
    ADD CONSTRAINT crm_leads_pkey PRIMARY KEY (id);


--
-- Name: crm_quotations crm_quotations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.crm_quotations
    ADD CONSTRAINT crm_quotations_pkey PRIMARY KEY (id);


--
-- Name: crm_quotations crm_quotations_quotation_number_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.crm_quotations
    ADD CONSTRAINT crm_quotations_quotation_number_key UNIQUE (quotation_number);


--
-- Name: data_import_errors data_import_errors_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_import_errors
    ADD CONSTRAINT data_import_errors_pkey PRIMARY KEY (error_id);


--
-- Name: data_import_jobs data_import_jobs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_import_jobs
    ADD CONSTRAINT data_import_jobs_pkey PRIMARY KEY (job_id);


--
-- Name: departments departments_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.departments
    ADD CONSTRAINT departments_name_key UNIQUE (name);


--
-- Name: departments departments_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.departments
    ADD CONSTRAINT departments_pkey PRIMARY KEY (id);


--
-- Name: employee_assets employee_assets_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_assets
    ADD CONSTRAINT employee_assets_pkey PRIMARY KEY (id);


--
-- Name: employee_attendance employee_attendance_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_attendance
    ADD CONSTRAINT employee_attendance_pkey PRIMARY KEY (id);


--
-- Name: employee_audit_logs employee_audit_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_audit_logs
    ADD CONSTRAINT employee_audit_logs_pkey PRIMARY KEY (id);


--
-- Name: employee_documents employee_documents_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_documents
    ADD CONSTRAINT employee_documents_pkey PRIMARY KEY (id);


--
-- Name: employee_leaves employee_leaves_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_leaves
    ADD CONSTRAINT employee_leaves_pkey PRIMARY KEY (id);


--
-- Name: employee_lifecycle employee_lifecycle_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_lifecycle
    ADD CONSTRAINT employee_lifecycle_pkey PRIMARY KEY (id);


--
-- Name: employee_notes employee_notes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_notes
    ADD CONSTRAINT employee_notes_pkey PRIMARY KEY (id);


--
-- Name: employee_payroll employee_payroll_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_payroll
    ADD CONSTRAINT employee_payroll_pkey PRIMARY KEY (id);


--
-- Name: employee_performance employee_performance_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_performance
    ADD CONSTRAINT employee_performance_pkey PRIMARY KEY (id);


--
-- Name: employee_timeline employee_timeline_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_timeline
    ADD CONSTRAINT employee_timeline_pkey PRIMARY KEY (id);


--
-- Name: exam_admit_cards exam_admit_cards_card_number_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_admit_cards
    ADD CONSTRAINT exam_admit_cards_card_number_key UNIQUE (card_number);


--
-- Name: exam_admit_cards exam_admit_cards_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_admit_cards
    ADD CONSTRAINT exam_admit_cards_pkey PRIMARY KEY (id);


--
-- Name: exam_attendances exam_attendances_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_attendances
    ADD CONSTRAINT exam_attendances_pkey PRIMARY KEY (id);


--
-- Name: exam_co_curricular_grades exam_co_curricular_grades_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_co_curricular_grades
    ADD CONSTRAINT exam_co_curricular_grades_pkey PRIMARY KEY (id);


--
-- Name: exam_divisions exam_divisions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_divisions
    ADD CONSTRAINT exam_divisions_pkey PRIMARY KEY (id);


--
-- Name: exam_grade_scales exam_grade_scales_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_grade_scales
    ADD CONSTRAINT exam_grade_scales_pkey PRIMARY KEY (id);


--
-- Name: exam_marks exam_marks_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_marks
    ADD CONSTRAINT exam_marks_pkey PRIMARY KEY (id);


--
-- Name: exam_report_card_batches exam_report_card_batches_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_report_card_batches
    ADD CONSTRAINT exam_report_card_batches_pkey PRIMARY KEY (id);


--
-- Name: exam_report_cards exam_report_cards_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_report_cards
    ADD CONSTRAINT exam_report_cards_pkey PRIMARY KEY (id);


--
-- Name: exam_schedules exam_schedules_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_schedules
    ADD CONSTRAINT exam_schedules_pkey PRIMARY KEY (id);


--
-- Name: exam_setups exam_setups_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_setups
    ADD CONSTRAINT exam_setups_pkey PRIMARY KEY (id);


--
-- Name: exam_teacher_remarks exam_teacher_remarks_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_teacher_remarks
    ADD CONSTRAINT exam_teacher_remarks_pkey PRIMARY KEY (id);


--
-- Name: exam_terms exam_terms_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_terms
    ADD CONSTRAINT exam_terms_pkey PRIMARY KEY (id);


--
-- Name: families families_family_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.families
    ADD CONSTRAINT families_family_code_key UNIQUE (family_code);


--
-- Name: families families_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.families
    ADD CONSTRAINT families_pkey PRIMARY KEY (id);


--
-- Name: fee_category fee_category_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fee_category
    ADD CONSTRAINT fee_category_pkey PRIMARY KEY (id);


--
-- Name: fee_due fee_due_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fee_due
    ADD CONSTRAINT fee_due_pkey PRIMARY KEY (id);


--
-- Name: fee_generation_batch fee_generation_batch_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fee_generation_batch
    ADD CONSTRAINT fee_generation_batch_pkey PRIMARY KEY (id);


--
-- Name: fee_invoice fee_invoice_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fee_invoice
    ADD CONSTRAINT fee_invoice_pkey PRIMARY KEY (id);


--
-- Name: fee_payment_allocation fee_payment_allocation_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fee_payment_allocation
    ADD CONSTRAINT fee_payment_allocation_pkey PRIMARY KEY (id);


--
-- Name: fee_reminders fee_reminders_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fee_reminders
    ADD CONSTRAINT fee_reminders_pkey PRIMARY KEY (id);


--
-- Name: fee_structure_item fee_structure_item_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fee_structure_item
    ADD CONSTRAINT fee_structure_item_pkey PRIMARY KEY (id);


--
-- Name: fee_structure fee_structure_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fee_structure
    ADD CONSTRAINT fee_structure_pkey PRIMARY KEY (id);


--
-- Name: gate_passes gate_passes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gate_passes
    ADD CONSTRAINT gate_passes_pkey PRIMARY KEY (id);


--
-- Name: id_card_generations id_card_generations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.id_card_generations
    ADD CONSTRAINT id_card_generations_pkey PRIMARY KEY (id);


--
-- Name: impersonation_sessions impersonation_sessions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.impersonation_sessions
    ADD CONSTRAINT impersonation_sessions_pkey PRIMARY KEY (id);


--
-- Name: onboarding_drafts onboarding_drafts_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.onboarding_drafts
    ADD CONSTRAINT onboarding_drafts_pkey PRIMARY KEY (school_id);


--
-- Name: online_admissions online_admissions_application_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.online_admissions
    ADD CONSTRAINT online_admissions_application_id_key UNIQUE (application_id);


--
-- Name: online_admissions online_admissions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.online_admissions
    ADD CONSTRAINT online_admissions_pkey PRIMARY KEY (id);


--
-- Name: parcel_dispatches parcel_dispatches_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcel_dispatches
    ADD CONSTRAINT parcel_dispatches_pkey PRIMARY KEY (id);


--
-- Name: parcel_receives parcel_receives_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcel_receives
    ADD CONSTRAINT parcel_receives_pkey PRIMARY KEY (id);


--
-- Name: password_reset_tokens password_reset_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.password_reset_tokens
    ADD CONSTRAINT password_reset_tokens_pkey PRIMARY KEY (id);


--
-- Name: password_reset_tokens password_reset_tokens_token_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.password_reset_tokens
    ADD CONSTRAINT password_reset_tokens_token_key UNIQUE (token);


--
-- Name: password_resets password_resets_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.password_resets
    ADD CONSTRAINT password_resets_pkey PRIMARY KEY (id);


--
-- Name: password_resets password_resets_token_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.password_resets
    ADD CONSTRAINT password_resets_token_key UNIQUE (token);


--
-- Name: fee_payment payment_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fee_payment
    ADD CONSTRAINT payment_pkey PRIMARY KEY (id);


--
-- Name: permission_definitions permission_definitions_permission_key_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.permission_definitions
    ADD CONSTRAINT permission_definitions_permission_key_key UNIQUE (permission_key);


--
-- Name: permission_definitions permission_definitions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.permission_definitions
    ADD CONSTRAINT permission_definitions_pkey PRIMARY KEY (id);


--
-- Name: plan_modules plan_modules_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.plan_modules
    ADD CONSTRAINT plan_modules_pkey PRIMARY KEY (plan_id, module_id);


--
-- Name: platform_modules platform_modules_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.platform_modules
    ADD CONSTRAINT platform_modules_code_key UNIQUE (code);


--
-- Name: platform_modules platform_modules_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.platform_modules
    ADD CONSTRAINT platform_modules_pkey PRIMARY KEY (id);


--
-- Name: recruitment_candidates recruitment_candidates_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recruitment_candidates
    ADD CONSTRAINT recruitment_candidates_pkey PRIMARY KEY (id);


--
-- Name: role_permissions role_permissions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.role_permissions
    ADD CONSTRAINT role_permissions_pkey PRIMARY KEY (id);


--
-- Name: roles roles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (id);


--
-- Name: school_account school_account_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.school_account
    ADD CONSTRAINT school_account_pkey PRIMARY KEY (id);


--
-- Name: schools school_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.schools
    ADD CONSTRAINT school_code_key UNIQUE (code);


--
-- Name: school_config_overrides school_config_overrides_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.school_config_overrides
    ADD CONSTRAINT school_config_overrides_pkey PRIMARY KEY (id);


--
-- Name: school_config_overrides school_config_overrides_school_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.school_config_overrides
    ADD CONSTRAINT school_config_overrides_school_id_key UNIQUE (school_id);


--
-- Name: school_departments school_departments_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.school_departments
    ADD CONSTRAINT school_departments_pkey PRIMARY KEY (id);


--
-- Name: school_designations school_designations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.school_designations
    ADD CONSTRAINT school_designations_pkey PRIMARY KEY (id);


--
-- Name: school_leave_types school_leave_types_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.school_leave_types
    ADD CONSTRAINT school_leave_types_pkey PRIMARY KEY (id);


--
-- Name: school_module_access school_module_access_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.school_module_access
    ADD CONSTRAINT school_module_access_pkey PRIMARY KEY (id);


--
-- Name: schools school_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.schools
    ADD CONSTRAINT school_pkey PRIMARY KEY (id);


--
-- Name: school_subscription_installments school_subscription_installments_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.school_subscription_installments
    ADD CONSTRAINT school_subscription_installments_pkey PRIMARY KEY (id);


--
-- Name: school_subscriptions school_subscriptions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.school_subscriptions
    ADD CONSTRAINT school_subscriptions_pkey PRIMARY KEY (id);


--
-- Name: schools schools_subdomain_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.schools
    ADD CONSTRAINT schools_subdomain_key UNIQUE (subdomain);


--
-- Name: sections sections_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sections
    ADD CONSTRAINT sections_pkey PRIMARY KEY (id);


--
-- Name: staff_advances staff_advances_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.staff_advances
    ADD CONSTRAINT staff_advances_pkey PRIMARY KEY (id);


--
-- Name: staff_attendance staff_attendance_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.staff_attendance
    ADD CONSTRAINT staff_attendance_pkey PRIMARY KEY (id);


--
-- Name: staff_bank_accounts staff_bank_accounts_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.staff_bank_accounts
    ADD CONSTRAINT staff_bank_accounts_pkey PRIMARY KEY (id);


--
-- Name: staff_bank_accounts staff_bank_accounts_staff_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.staff_bank_accounts
    ADD CONSTRAINT staff_bank_accounts_staff_id_key UNIQUE (staff_id);


--
-- Name: staff_documents staff_documents_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.staff_documents
    ADD CONSTRAINT staff_documents_pkey PRIMARY KEY (id);


--
-- Name: staff_leave_balances staff_leave_balances_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.staff_leave_balances
    ADD CONSTRAINT staff_leave_balances_pkey PRIMARY KEY (id);


--
-- Name: staff_leaves staff_leaves_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.staff_leaves
    ADD CONSTRAINT staff_leaves_pkey PRIMARY KEY (id);


--
-- Name: staff_payroll_details staff_payroll_details_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.staff_payroll_details
    ADD CONSTRAINT staff_payroll_details_pkey PRIMARY KEY (id);


--
-- Name: staff_payroll_details staff_payroll_details_staff_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.staff_payroll_details
    ADD CONSTRAINT staff_payroll_details_staff_id_key UNIQUE (staff_id);


--
-- Name: staff_payrolls staff_payrolls_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.staff_payrolls
    ADD CONSTRAINT staff_payrolls_pkey PRIMARY KEY (id);


--
-- Name: staff staff_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.staff
    ADD CONSTRAINT staff_pkey PRIMARY KEY (id);


--
-- Name: staff_tasks staff_tasks_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.staff_tasks
    ADD CONSTRAINT staff_tasks_pkey PRIMARY KEY (id);


--
-- Name: student_attendance student_attendance_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_attendance
    ADD CONSTRAINT student_attendance_pkey PRIMARY KEY (id);


--
-- Name: student_categories student_categories_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_categories
    ADD CONSTRAINT student_categories_pkey PRIMARY KEY (id);


--
-- Name: student_certificates student_certificates_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_certificates
    ADD CONSTRAINT student_certificates_pkey PRIMARY KEY (id);


--
-- Name: student_documents student_documents_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_documents
    ADD CONSTRAINT student_documents_pkey PRIMARY KEY (id);


--
-- Name: student_fee_structure student_fee_structure_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_fee_structure
    ADD CONSTRAINT student_fee_structure_pkey PRIMARY KEY (id);


--
-- Name: student_houses student_houses_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_houses
    ADD CONSTRAINT student_houses_pkey PRIMARY KEY (id);


--
-- Name: student_leaves student_leaves_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_leaves
    ADD CONSTRAINT student_leaves_pkey PRIMARY KEY (id);


--
-- Name: student_parents student_parents_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_parents
    ADD CONSTRAINT student_parents_pkey PRIMARY KEY (student_id, parent_user_id);


--
-- Name: student student_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student
    ADD CONSTRAINT student_pkey PRIMARY KEY (id);


--
-- Name: student_referrals student_referrals_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_referrals
    ADD CONSTRAINT student_referrals_pkey PRIMARY KEY (id);


--
-- Name: student_siblings student_siblings_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_siblings
    ADD CONSTRAINT student_siblings_pkey PRIMARY KEY (id);


--
-- Name: student_subject_enrollments student_subject_enrollments_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_subject_enrollments
    ADD CONSTRAINT student_subject_enrollments_pkey PRIMARY KEY (id);


--
-- Name: subject_attendance subject_attendance_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.subject_attendance
    ADD CONSTRAINT subject_attendance_pkey PRIMARY KEY (id);


--
-- Name: subjects subjects_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.subjects
    ADD CONSTRAINT subjects_pkey PRIMARY KEY (id);


--
-- Name: subscription_plans subscription_plans_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.subscription_plans
    ADD CONSTRAINT subscription_plans_pkey PRIMARY KEY (id);


--
-- Name: super_admin_employees super_admin_employees_employee_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.super_admin_employees
    ADD CONSTRAINT super_admin_employees_employee_code_key UNIQUE (employee_code);


--
-- Name: super_admin_employees super_admin_employees_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.super_admin_employees
    ADD CONSTRAINT super_admin_employees_pkey PRIMARY KEY (id);


--
-- Name: super_admin_employees super_admin_employees_user_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.super_admin_employees
    ADD CONSTRAINT super_admin_employees_user_id_key UNIQUE (user_id);


--
-- Name: support_tickets support_tickets_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.support_tickets
    ADD CONSTRAINT support_tickets_pkey PRIMARY KEY (id);


--
-- Name: support_tickets support_tickets_ticket_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.support_tickets
    ADD CONSTRAINT support_tickets_ticket_code_key UNIQUE (ticket_code);


--
-- Name: tenant_entitlement_overrides tenant_entitlement_overrides_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tenant_entitlement_overrides
    ADD CONSTRAINT tenant_entitlement_overrides_pkey PRIMARY KEY (id);


--
-- Name: ticket_categories ticket_categories_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ticket_categories
    ADD CONSTRAINT ticket_categories_pkey PRIMARY KEY (id);


--
-- Name: ticket_histories ticket_histories_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ticket_histories
    ADD CONSTRAINT ticket_histories_pkey PRIMARY KEY (id);


--
-- Name: timetable_entries timetable_entries_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.timetable_entries
    ADD CONSTRAINT timetable_entries_pkey PRIMARY KEY (id);


--
-- Name: timetable_periods timetable_periods_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.timetable_periods
    ADD CONSTRAINT timetable_periods_pkey PRIMARY KEY (id);


--
-- Name: student_siblings uk_sibling_pair; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_siblings
    ADD CONSTRAINT uk_sibling_pair UNIQUE (primary_student_id, sibling_student_id);


--
-- Name: staff_attendance uk_staff_att_date; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.staff_attendance
    ADD CONSTRAINT uk_staff_att_date UNIQUE (staff_id, attendance_date);


--
-- Name: staff_leave_balances uk_staff_leave_bal; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.staff_leave_balances
    ADD CONSTRAINT uk_staff_leave_bal UNIQUE (staff_id, leave_type_id);


--
-- Name: staff_payrolls uk_staff_payroll_month; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.staff_payrolls
    ADD CONSTRAINT uk_staff_payroll_month UNIQUE (staff_id, payroll_month, payroll_year);


--
-- Name: staff uk_staff_school_user; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.staff
    ADD CONSTRAINT uk_staff_school_user UNIQUE (school_id, user_id);


--
-- Name: exam_admit_cards uq_admit_card_entry; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_admit_cards
    ADD CONSTRAINT uq_admit_card_entry UNIQUE (school_id, exam_setup_id, student_id);


--
-- Name: class_subject_assignments uq_class_subject; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.class_subject_assignments
    ADD CONSTRAINT uq_class_subject UNIQUE (school_id, class_id, section_id, subject_id, academic_year_id);


--
-- Name: exam_co_curricular_grades uq_co_curricular_entry; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_co_curricular_grades
    ADD CONSTRAINT uq_co_curricular_entry UNIQUE (school_id, term_id, student_id);


--
-- Name: exam_attendances uq_exam_attendance_entry; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_attendances
    ADD CONSTRAINT uq_exam_attendance_entry UNIQUE (school_id, term_id, student_id);


--
-- Name: exam_divisions uq_exam_divisions_name; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_divisions
    ADD CONSTRAINT uq_exam_divisions_name UNIQUE (school_id, name);


--
-- Name: exam_marks uq_exam_marks_entry; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_marks
    ADD CONSTRAINT uq_exam_marks_entry UNIQUE (school_id, exam_setup_id, subject_id, student_id);


--
-- Name: exam_report_cards uq_exam_report_card_entry; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_report_cards
    ADD CONSTRAINT uq_exam_report_card_entry UNIQUE (school_id, term_id, student_id, generation_mode);


--
-- Name: exam_schedules uq_exam_schedule_entry; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_schedules
    ADD CONSTRAINT uq_exam_schedule_entry UNIQUE (school_id, exam_setup_id, class_id, section_id, subject_id);


--
-- Name: exam_setups uq_exam_setups_school_term_name; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_setups
    ADD CONSTRAINT uq_exam_setups_school_term_name UNIQUE (school_id, term_id, name);


--
-- Name: exam_terms uq_exam_terms_school_name; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_terms
    ADD CONSTRAINT uq_exam_terms_school_name UNIQUE (school_id, name);


--
-- Name: student_subject_enrollments uq_student_subject; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_subject_enrollments
    ADD CONSTRAINT uq_student_subject UNIQUE (school_id, student_id, subject_id, academic_year_id);


--
-- Name: subject_attendance uq_subject_attendance_entry; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.subject_attendance
    ADD CONSTRAINT uq_subject_attendance_entry UNIQUE (school_id, student_id, subject_id, attendance_date, timetable_entry_id);


--
-- Name: exam_teacher_remarks uq_teacher_remark_entry; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_teacher_remarks
    ADD CONSTRAINT uq_teacher_remark_entry UNIQUE (school_id, term_id, student_id);


--
-- Name: timetable_entries uq_timetable_grid; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.timetable_entries
    ADD CONSTRAINT uq_timetable_grid UNIQUE (school_id, class_id, section_id, academic_year_id, day_of_week, period_id);


--
-- Name: user_school_roles uq_user_school_role; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_school_roles
    ADD CONSTRAINT uq_user_school_role UNIQUE (user_id, school_id, role);


--
-- Name: user_activity_logs user_activity_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_activity_logs
    ADD CONSTRAINT user_activity_logs_pkey PRIMARY KEY (id);


--
-- Name: user_assignments user_assignments_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_assignments
    ADD CONSTRAINT user_assignments_pkey PRIMARY KEY (id);


--
-- Name: user_login_history user_login_history_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_login_history
    ADD CONSTRAINT user_login_history_pkey PRIMARY KEY (id);


--
-- Name: user_requests user_requests_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_requests
    ADD CONSTRAINT user_requests_pkey PRIMARY KEY (id);


--
-- Name: user_role_mappings user_role_mappings_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_role_mappings
    ADD CONSTRAINT user_role_mappings_pkey PRIMARY KEY (id);


--
-- Name: user_school_roles user_school_roles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_school_roles
    ADD CONSTRAINT user_school_roles_pkey PRIMARY KEY (id);


--
-- Name: users users_phone_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_phone_key UNIQUE (phone);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: visitor_logs visitor_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.visitor_logs
    ADD CONSTRAINT visitor_logs_pkey PRIMARY KEY (id);


--
-- Name: idx_admission_enquiries_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_admission_enquiries_date ON public.admission_enquiries USING btree (enquiry_date);


--
-- Name: idx_admission_enquiries_school_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_admission_enquiries_school_id ON public.admission_enquiries USING btree (school_id);


--
-- Name: idx_admission_enquiries_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_admission_enquiries_status ON public.admission_enquiries USING btree (status);


--
-- Name: idx_admission_enquiry_follow_ups_enquiry_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_admission_enquiry_follow_ups_enquiry_id ON public.admission_enquiry_follow_ups USING btree (enquiry_id);


--
-- Name: idx_admit_cards_lookup; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_admit_cards_lookup ON public.exam_admit_cards USING btree (school_id, exam_setup_id, class_id, status);


--
-- Name: idx_admit_cards_student; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_admit_cards_student ON public.exam_admit_cards USING btree (school_id, student_id);


--
-- Name: idx_ar_school_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_ar_school_id ON public.account_requests USING btree (school_id);


--
-- Name: idx_ar_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_ar_status ON public.account_requests USING btree (status);


--
-- Name: idx_attendance_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_attendance_date ON public.student_attendance USING btree (attendance_date);


--
-- Name: idx_attendance_school_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_attendance_school_id ON public.student_attendance USING btree (school_id);


--
-- Name: idx_attendance_student_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_attendance_student_id ON public.student_attendance USING btree (student_id);


--
-- Name: idx_audit_action; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_audit_action ON public.audit_logs USING btree (action);


--
-- Name: idx_audit_actor_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_audit_actor_id ON public.audit_logs USING btree (actor_id);


--
-- Name: idx_audit_school_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_audit_school_id ON public.audit_logs USING btree (target_school_id);


--
-- Name: idx_audit_timestamp; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_audit_timestamp ON public.audit_logs USING btree ("timestamp");


--
-- Name: idx_ay_school_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_ay_school_id ON public.academic_years USING btree (school_id);


--
-- Name: idx_cat_school_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cat_school_id ON public.student_categories USING btree (school_id);


--
-- Name: idx_class_school_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_class_school_id ON public.class USING btree (school_id);


--
-- Name: idx_class_teacher_assignments_school_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_class_teacher_assignments_school_id ON public.class_teacher_assignments USING btree (school_id);


--
-- Name: idx_class_teacher_assignments_staff_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_class_teacher_assignments_staff_id ON public.class_teacher_assignments USING btree (staff_id);


--
-- Name: idx_co_curricular_filter; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_co_curricular_filter ON public.exam_co_curricular_grades USING btree (school_id, term_id, class_id, section_id);


--
-- Name: idx_collection_plan_school_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_collection_plan_school_id ON public.collection_plan USING btree (school_id);


--
-- Name: idx_comm_announcements_scheduled_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_comm_announcements_scheduled_at ON public.communication_announcements USING btree (scheduled_at);


--
-- Name: idx_comm_announcements_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_comm_announcements_status ON public.communication_announcements USING btree (status);


--
-- Name: idx_comm_deliveries_announcement_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_comm_deliveries_announcement_id ON public.communication_deliveries USING btree (announcement_id);


--
-- Name: idx_comm_deliveries_school_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_comm_deliveries_school_id ON public.communication_deliveries USING btree (school_id);


--
-- Name: idx_comm_deliveries_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_comm_deliveries_status ON public.communication_deliveries USING btree (status);


--
-- Name: idx_crm_activity_logs_lead_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_crm_activity_logs_lead_id ON public.crm_activity_logs USING btree (lead_id);


--
-- Name: idx_crm_demos_demo_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_crm_demos_demo_date ON public.crm_demos USING btree (demo_date);


--
-- Name: idx_crm_demos_lead_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_crm_demos_lead_id ON public.crm_demos USING btree (lead_id);


--
-- Name: idx_crm_follow_ups_lead_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_crm_follow_ups_lead_id ON public.crm_follow_ups USING btree (lead_id);


--
-- Name: idx_crm_follow_ups_scheduled_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_crm_follow_ups_scheduled_date ON public.crm_follow_ups USING btree (scheduled_date);


--
-- Name: idx_crm_leads_assigned_employee_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_crm_leads_assigned_employee_id ON public.crm_leads USING btree (assigned_employee_id);


--
-- Name: idx_crm_leads_pipeline_stage; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_crm_leads_pipeline_stage ON public.crm_leads USING btree (pipeline_stage);


--
-- Name: idx_crm_quotations_lead_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_crm_quotations_lead_id ON public.crm_quotations USING btree (lead_id);


--
-- Name: idx_csa_school_class; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_csa_school_class ON public.class_subject_assignments USING btree (school_id, class_id);


--
-- Name: idx_csa_section; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_csa_section ON public.class_subject_assignments USING btree (section_id);


--
-- Name: idx_data_import_errors_job_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_data_import_errors_job_id ON public.data_import_errors USING btree (job_id);


--
-- Name: idx_data_import_jobs_category; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_data_import_jobs_category ON public.data_import_jobs USING btree (category);


--
-- Name: idx_data_import_jobs_school_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_data_import_jobs_school_id ON public.data_import_jobs USING btree (school_id);


--
-- Name: idx_doc_student_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_doc_student_id ON public.student_documents USING btree (student_id);


--
-- Name: idx_employee_attendance_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_employee_attendance_date ON public.employee_attendance USING btree (date);


--
-- Name: idx_employee_attendance_emp_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_employee_attendance_emp_id ON public.employee_attendance USING btree (employee_id);


--
-- Name: idx_employee_audit_logs_emp_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_employee_audit_logs_emp_id ON public.employee_audit_logs USING btree (employee_id);


--
-- Name: idx_employee_documents_emp_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_employee_documents_emp_id ON public.employee_documents USING btree (employee_id);


--
-- Name: idx_employee_leaves_emp_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_employee_leaves_emp_id ON public.employee_leaves USING btree (employee_id);


--
-- Name: idx_employee_leaves_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_employee_leaves_status ON public.employee_leaves USING btree (status);


--
-- Name: idx_employee_payroll_emp_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_employee_payroll_emp_id ON public.employee_payroll USING btree (employee_id);


--
-- Name: idx_employee_payroll_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_employee_payroll_status ON public.employee_payroll USING btree (status);


--
-- Name: idx_employee_performance_emp_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_employee_performance_emp_id ON public.employee_performance USING btree (employee_id);


--
-- Name: idx_exam_attendance_filter; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_exam_attendance_filter ON public.exam_attendances USING btree (school_id, term_id, class_id, section_id);


--
-- Name: idx_exam_attendance_room; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_exam_attendance_room ON public.exam_attendances USING btree (school_id, room_number, session_status);


--
-- Name: idx_exam_divisions_lookup; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_exam_divisions_lookup ON public.exam_divisions USING btree (school_id, status);


--
-- Name: idx_exam_marks_filter; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_exam_marks_filter ON public.exam_marks USING btree (school_id, exam_setup_id, class_id, section_id, subject_id);


--
-- Name: idx_exam_marks_student; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_exam_marks_student ON public.exam_marks USING btree (school_id, student_id);


--
-- Name: idx_exam_report_card_batches_school; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_exam_report_card_batches_school ON public.exam_report_card_batches USING btree (school_id, status);


--
-- Name: idx_exam_report_cards_filter; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_exam_report_cards_filter ON public.exam_report_cards USING btree (school_id, term_id, class_id, section_id);


--
-- Name: idx_exam_schedules_search; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_exam_schedules_search ON public.exam_schedules USING btree (school_id, exam_setup_id, class_id, exam_date);


--
-- Name: idx_exam_schedules_subject; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_exam_schedules_subject ON public.exam_schedules USING btree (school_id, subject_id);


--
-- Name: idx_exam_setups_school_term; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_exam_setups_school_term ON public.exam_setups USING btree (school_id, term_id);


--
-- Name: idx_exam_terms_school_year; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_exam_terms_school_year ON public.exam_terms USING btree (school_id, academic_year_id);


--
-- Name: idx_family_code; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_family_code ON public.families USING btree (family_code);


--
-- Name: idx_family_school_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_family_school_id ON public.families USING btree (school_id);


--
-- Name: idx_fee_category_school_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fee_category_school_id ON public.fee_category USING btree (school_id);


--
-- Name: idx_fee_invoice_school_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fee_invoice_school_id ON public.fee_invoice USING btree (school_id);


--
-- Name: idx_fee_invoice_student_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fee_invoice_student_id ON public.fee_invoice USING btree (student_id);


--
-- Name: idx_fee_payment_student_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fee_payment_student_id ON public.fee_payment USING btree (student_id);


--
-- Name: idx_fee_structure_class_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fee_structure_class_id ON public.fee_structure USING btree (class_id);


--
-- Name: idx_fee_structure_school_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fee_structure_school_id ON public.fee_structure USING btree (school_id);


--
-- Name: idx_fgb_school_year; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fgb_school_year ON public.fee_generation_batch USING btree (school_id, academic_year_id);


--
-- Name: idx_fsi_structure_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fsi_structure_id ON public.fee_structure_item USING btree (fee_structure_id);


--
-- Name: idx_gate_passes_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gate_passes_date ON public.gate_passes USING btree (pass_date);


--
-- Name: idx_gate_passes_school_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gate_passes_school_id ON public.gate_passes USING btree (school_id);


--
-- Name: idx_gate_passes_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gate_passes_status ON public.gate_passes USING btree (status);


--
-- Name: idx_grade_scales_lookup; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_grade_scales_lookup ON public.exam_grade_scales USING btree (school_id, target_class, status);


--
-- Name: idx_history_ticket; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_history_ticket ON public.ticket_histories USING btree (ticket_id);


--
-- Name: idx_house_school_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_house_school_id ON public.student_houses USING btree (school_id);


--
-- Name: idx_id_card_generations_school_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_id_card_generations_school_id ON public.id_card_generations USING btree (school_id);


--
-- Name: idx_id_card_generations_student_id; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX idx_id_card_generations_student_id ON public.id_card_generations USING btree (student_id);


--
-- Name: idx_impersonation_original_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_impersonation_original_user ON public.impersonation_sessions USING btree (original_user_id);


--
-- Name: idx_impersonation_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_impersonation_status ON public.impersonation_sessions USING btree (status);


--
-- Name: idx_onboarding_drafts_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_onboarding_drafts_status ON public.onboarding_drafts USING btree (status);


--
-- Name: idx_parcel_dispatches_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_parcel_dispatches_date ON public.parcel_dispatches USING btree (dispatch_date);


--
-- Name: idx_parcel_dispatches_school_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_parcel_dispatches_school_id ON public.parcel_dispatches USING btree (school_id);


--
-- Name: idx_parcel_dispatches_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_parcel_dispatches_status ON public.parcel_dispatches USING btree (status);


--
-- Name: idx_parcel_receives_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_parcel_receives_date ON public.parcel_receives USING btree (date_received);


--
-- Name: idx_parcel_receives_school_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_parcel_receives_school_id ON public.parcel_receives USING btree (school_id);


--
-- Name: idx_parcel_receives_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_parcel_receives_status ON public.parcel_receives USING btree (status);


--
-- Name: idx_password_resets_token; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_password_resets_token ON public.password_resets USING btree (token);


--
-- Name: idx_password_resets_user_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_password_resets_user_id ON public.password_resets USING btree (user_id);


--
-- Name: idx_payment_school_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_payment_school_id ON public.fee_payment USING btree (school_id);


--
-- Name: idx_period_school_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_period_school_id ON public.timetable_periods USING btree (school_id);


--
-- Name: idx_prt_token; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_prt_token ON public.password_reset_tokens USING btree (token);


--
-- Name: idx_recruitment_candidates_school_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_recruitment_candidates_school_id ON public.recruitment_candidates USING btree (school_id);


--
-- Name: idx_school_account_school_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_school_account_school_id ON public.school_account USING btree (school_id);


--
-- Name: idx_school_departments_school_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_school_departments_school_id ON public.school_departments USING btree (school_id);


--
-- Name: idx_school_designations_school_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_school_designations_school_id ON public.school_designations USING btree (school_id);


--
-- Name: idx_school_leave_types_school_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_school_leave_types_school_id ON public.school_leave_types USING btree (school_id);


--
-- Name: idx_school_subscription_installments_sub_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_school_subscription_installments_sub_id ON public.school_subscription_installments USING btree (subscription_id);


--
-- Name: idx_school_subscriptions_school_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_school_subscriptions_school_id ON public.school_subscriptions USING btree (school_id);


--
-- Name: idx_section_class_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_section_class_id ON public.sections USING btree (class_id);


--
-- Name: idx_section_school_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_section_school_id ON public.sections USING btree (school_id);


--
-- Name: idx_sfs_student_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sfs_student_id ON public.student_fee_structure USING btree (student_id);


--
-- Name: idx_sse_school_student; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sse_school_student ON public.student_subject_enrollments USING btree (school_id, student_id);


--
-- Name: idx_sse_year; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sse_year ON public.student_subject_enrollments USING btree (academic_year_id);


--
-- Name: idx_staff_advances_staff_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_staff_advances_staff_id ON public.staff_advances USING btree (staff_id);


--
-- Name: idx_staff_attendance_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_staff_attendance_date ON public.staff_attendance USING btree (attendance_date);


--
-- Name: idx_staff_attendance_school_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_staff_attendance_school_id ON public.staff_attendance USING btree (school_id);


--
-- Name: idx_staff_attendance_staff_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_staff_attendance_staff_id ON public.staff_attendance USING btree (staff_id);


--
-- Name: idx_staff_documents_staff_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_staff_documents_staff_id ON public.staff_documents USING btree (staff_id);


--
-- Name: idx_staff_leave_balances_staff_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_staff_leave_balances_staff_id ON public.staff_leave_balances USING btree (staff_id);


--
-- Name: idx_staff_leaves_school_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_staff_leaves_school_id ON public.staff_leaves USING btree (school_id);


--
-- Name: idx_staff_leaves_staff_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_staff_leaves_staff_id ON public.staff_leaves USING btree (staff_id);


--
-- Name: idx_staff_payrolls_school_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_staff_payrolls_school_id ON public.staff_payrolls USING btree (school_id);


--
-- Name: idx_staff_payrolls_staff_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_staff_payrolls_staff_id ON public.staff_payrolls USING btree (staff_id);


--
-- Name: idx_staff_school_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_staff_school_id ON public.staff USING btree (school_id);


--
-- Name: idx_staff_tasks_staff_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_staff_tasks_staff_id ON public.staff_tasks USING btree (staff_id);


--
-- Name: idx_student_certificates_school_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_student_certificates_school_id ON public.student_certificates USING btree (school_id);


--
-- Name: idx_student_certificates_student_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_student_certificates_student_id ON public.student_certificates USING btree (student_id);


--
-- Name: idx_student_class_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_student_class_id ON public.student USING btree (class_id);


--
-- Name: idx_student_family_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_student_family_id ON public.student USING btree (family_id);


--
-- Name: idx_student_leaves_school_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_student_leaves_school_id ON public.student_leaves USING btree (school_id);


--
-- Name: idx_student_leaves_student_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_student_leaves_student_id ON public.student_leaves USING btree (student_id);


--
-- Name: idx_student_parents_parent_user_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_student_parents_parent_user_id ON public.student_parents USING btree (parent_user_id);


--
-- Name: idx_student_referrals_school_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_student_referrals_school_id ON public.student_referrals USING btree (school_id);


--
-- Name: idx_student_school_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_student_school_id ON public.student USING btree (school_id);


--
-- Name: idx_student_siblings_primary; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_student_siblings_primary ON public.student_siblings USING btree (primary_student_id);


--
-- Name: idx_student_siblings_school; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_student_siblings_school ON public.student_siblings USING btree (school_id);


--
-- Name: idx_student_siblings_sibling; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_student_siblings_sibling ON public.student_siblings USING btree (sibling_student_id);


--
-- Name: idx_subj_att_class_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_subj_att_class_date ON public.subject_attendance USING btree (school_id, class_id, subject_id, attendance_date);


--
-- Name: idx_subj_att_student; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_subj_att_student ON public.subject_attendance USING btree (school_id, student_id);


--
-- Name: idx_subject_school_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_subject_school_id ON public.subjects USING btree (school_id);


--
-- Name: idx_te_day; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_te_day ON public.timetable_entries USING btree (day_of_week);


--
-- Name: idx_te_school_class; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_te_school_class ON public.timetable_entries USING btree (school_id, class_id);


--
-- Name: idx_te_section; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_te_section ON public.timetable_entries USING btree (section_id);


--
-- Name: idx_te_teacher; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_te_teacher ON public.timetable_entries USING btree (teacher_id);


--
-- Name: idx_teacher_remarks_filter; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_teacher_remarks_filter ON public.exam_teacher_remarks USING btree (school_id, term_id, class_id, section_id);


--
-- Name: idx_tenant_entitlement_overrides_module; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tenant_entitlement_overrides_module ON public.tenant_entitlement_overrides USING btree (module_code);


--
-- Name: idx_tenant_entitlement_overrides_school; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tenant_entitlement_overrides_school ON public.tenant_entitlement_overrides USING btree (school_id);


--
-- Name: idx_tickets_school; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tickets_school ON public.support_tickets USING btree (school_id);


--
-- Name: idx_tickets_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tickets_status ON public.support_tickets USING btree (status);


--
-- Name: idx_ual_module; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_ual_module ON public.user_activity_logs USING btree (module);


--
-- Name: idx_ual_school_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_ual_school_id ON public.user_activity_logs USING btree (school_id);


--
-- Name: idx_ual_user_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_ual_user_id ON public.user_activity_logs USING btree (user_id);


--
-- Name: idx_ulh_school_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_ulh_school_id ON public.user_login_history USING btree (school_id);


--
-- Name: idx_ulh_user_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_ulh_user_id ON public.user_login_history USING btree (user_id);


--
-- Name: idx_unique_active_class_teacher; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX idx_unique_active_class_teacher ON public.class_teacher_assignments USING btree (section_id, academic_year_id) WHERE (((status)::text = 'ACTIVE'::text) AND (section_id IS NOT NULL));


--
-- Name: idx_user_requests_school_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_user_requests_school_id ON public.user_requests USING btree (school_id);


--
-- Name: idx_user_requests_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_user_requests_status ON public.user_requests USING btree (status);


--
-- Name: idx_user_requests_user_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_user_requests_user_id ON public.user_requests USING btree (user_id);


--
-- Name: idx_user_school_roles_school_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_user_school_roles_school_id ON public.user_school_roles USING btree (school_id);


--
-- Name: idx_user_school_roles_school_role; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_user_school_roles_school_role ON public.user_school_roles USING btree (school_id, role);


--
-- Name: idx_user_school_roles_user_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_user_school_roles_user_id ON public.user_school_roles USING btree (user_id);


--
-- Name: idx_users_phone; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_users_phone ON public.users USING btree (phone);


--
-- Name: idx_visitor_logs_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_visitor_logs_date ON public.visitor_logs USING btree (visit_date);


--
-- Name: idx_visitor_logs_school_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_visitor_logs_school_id ON public.visitor_logs USING btree (school_id);


--
-- Name: idx_visitor_logs_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_visitor_logs_status ON public.visitor_logs USING btree (status);


--
-- Name: class_subject_assignments class_subject_assignments_academic_year_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.class_subject_assignments
    ADD CONSTRAINT class_subject_assignments_academic_year_id_fkey FOREIGN KEY (academic_year_id) REFERENCES public.academic_years(id);


--
-- Name: class_subject_assignments class_subject_assignments_class_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.class_subject_assignments
    ADD CONSTRAINT class_subject_assignments_class_id_fkey FOREIGN KEY (class_id) REFERENCES public.class(id);


--
-- Name: class_subject_assignments class_subject_assignments_school_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.class_subject_assignments
    ADD CONSTRAINT class_subject_assignments_school_id_fkey FOREIGN KEY (school_id) REFERENCES public.schools(id);


--
-- Name: class_subject_assignments class_subject_assignments_section_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.class_subject_assignments
    ADD CONSTRAINT class_subject_assignments_section_id_fkey FOREIGN KEY (section_id) REFERENCES public.sections(id);


--
-- Name: class_subject_assignments class_subject_assignments_subject_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.class_subject_assignments
    ADD CONSTRAINT class_subject_assignments_subject_id_fkey FOREIGN KEY (subject_id) REFERENCES public.subjects(id);


--
-- Name: class_teacher_assignments class_teacher_assignments_academic_year_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.class_teacher_assignments
    ADD CONSTRAINT class_teacher_assignments_academic_year_id_fkey FOREIGN KEY (academic_year_id) REFERENCES public.academic_years(id) ON DELETE CASCADE;


--
-- Name: class_teacher_assignments class_teacher_assignments_class_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.class_teacher_assignments
    ADD CONSTRAINT class_teacher_assignments_class_id_fkey FOREIGN KEY (class_id) REFERENCES public.class(id) ON DELETE CASCADE;


--
-- Name: class_teacher_assignments class_teacher_assignments_school_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.class_teacher_assignments
    ADD CONSTRAINT class_teacher_assignments_school_id_fkey FOREIGN KEY (school_id) REFERENCES public.schools(id) ON DELETE CASCADE;


--
-- Name: class_teacher_assignments class_teacher_assignments_section_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.class_teacher_assignments
    ADD CONSTRAINT class_teacher_assignments_section_id_fkey FOREIGN KEY (section_id) REFERENCES public.sections(id) ON DELETE CASCADE;


--
-- Name: class_teacher_assignments class_teacher_assignments_staff_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.class_teacher_assignments
    ADD CONSTRAINT class_teacher_assignments_staff_id_fkey FOREIGN KEY (staff_id) REFERENCES public.staff(id) ON DELETE CASCADE;


--
-- Name: collection_plan_item collection_plan_item_collection_plan_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.collection_plan_item
    ADD CONSTRAINT collection_plan_item_collection_plan_id_fkey FOREIGN KEY (collection_plan_id) REFERENCES public.collection_plan(id) ON DELETE CASCADE;


--
-- Name: collection_plan collection_plan_school_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.collection_plan
    ADD CONSTRAINT collection_plan_school_id_fkey FOREIGN KEY (school_id) REFERENCES public.schools(id);


--
-- Name: communication_announcement_schools communication_announcement_schools_announcement_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.communication_announcement_schools
    ADD CONSTRAINT communication_announcement_schools_announcement_id_fkey FOREIGN KEY (announcement_id) REFERENCES public.communication_announcements(id) ON DELETE CASCADE;


--
-- Name: communication_announcement_schools communication_announcement_schools_school_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.communication_announcement_schools
    ADD CONSTRAINT communication_announcement_schools_school_id_fkey FOREIGN KEY (school_id) REFERENCES public.schools(id) ON DELETE CASCADE;


--
-- Name: communication_announcements communication_announcements_created_by_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.communication_announcements
    ADD CONSTRAINT communication_announcements_created_by_user_id_fkey FOREIGN KEY (created_by_user_id) REFERENCES public.users(id);


--
-- Name: communication_deliveries communication_deliveries_announcement_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.communication_deliveries
    ADD CONSTRAINT communication_deliveries_announcement_id_fkey FOREIGN KEY (announcement_id) REFERENCES public.communication_announcements(id) ON DELETE CASCADE;


--
-- Name: communication_deliveries communication_deliveries_school_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.communication_deliveries
    ADD CONSTRAINT communication_deliveries_school_id_fkey FOREIGN KEY (school_id) REFERENCES public.schools(id) ON DELETE CASCADE;


--
-- Name: crm_activity_logs crm_activity_logs_actor_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.crm_activity_logs
    ADD CONSTRAINT crm_activity_logs_actor_id_fkey FOREIGN KEY (actor_id) REFERENCES public.users(id);


--
-- Name: crm_activity_logs crm_activity_logs_lead_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.crm_activity_logs
    ADD CONSTRAINT crm_activity_logs_lead_id_fkey FOREIGN KEY (lead_id) REFERENCES public.crm_leads(id) ON DELETE CASCADE;


--
-- Name: crm_demos crm_demos_demo_by_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.crm_demos
    ADD CONSTRAINT crm_demos_demo_by_id_fkey FOREIGN KEY (demo_by_id) REFERENCES public.users(id);


--
-- Name: crm_demos crm_demos_lead_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.crm_demos
    ADD CONSTRAINT crm_demos_lead_id_fkey FOREIGN KEY (lead_id) REFERENCES public.crm_leads(id) ON DELETE CASCADE;


--
-- Name: crm_follow_ups crm_follow_ups_executive_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.crm_follow_ups
    ADD CONSTRAINT crm_follow_ups_executive_id_fkey FOREIGN KEY (executive_id) REFERENCES public.users(id);


--
-- Name: crm_follow_ups crm_follow_ups_lead_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.crm_follow_ups
    ADD CONSTRAINT crm_follow_ups_lead_id_fkey FOREIGN KEY (lead_id) REFERENCES public.crm_leads(id) ON DELETE CASCADE;


--
-- Name: crm_leads crm_leads_assigned_employee_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.crm_leads
    ADD CONSTRAINT crm_leads_assigned_employee_id_fkey FOREIGN KEY (assigned_employee_id) REFERENCES public.users(id);


--
-- Name: crm_leads crm_leads_converted_school_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.crm_leads
    ADD CONSTRAINT crm_leads_converted_school_id_fkey FOREIGN KEY (converted_school_id) REFERENCES public.schools(id);


--
-- Name: crm_quotations crm_quotations_lead_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.crm_quotations
    ADD CONSTRAINT crm_quotations_lead_id_fkey FOREIGN KEY (lead_id) REFERENCES public.crm_leads(id) ON DELETE CASCADE;


--
-- Name: data_import_errors data_import_errors_job_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_import_errors
    ADD CONSTRAINT data_import_errors_job_id_fkey FOREIGN KEY (job_id) REFERENCES public.data_import_jobs(job_id) ON DELETE CASCADE;


--
-- Name: employee_assets employee_assets_employee_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_assets
    ADD CONSTRAINT employee_assets_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES public.super_admin_employees(id) ON DELETE CASCADE;


--
-- Name: employee_lifecycle employee_lifecycle_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_lifecycle
    ADD CONSTRAINT employee_lifecycle_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.super_admin_employees(id) ON DELETE SET NULL;


--
-- Name: employee_lifecycle employee_lifecycle_employee_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_lifecycle
    ADD CONSTRAINT employee_lifecycle_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES public.super_admin_employees(id) ON DELETE CASCADE;


--
-- Name: employee_notes employee_notes_author_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_notes
    ADD CONSTRAINT employee_notes_author_id_fkey FOREIGN KEY (author_id) REFERENCES public.super_admin_employees(id) ON DELETE SET NULL;


--
-- Name: employee_notes employee_notes_employee_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_notes
    ADD CONSTRAINT employee_notes_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES public.super_admin_employees(id) ON DELETE CASCADE;


--
-- Name: employee_timeline employee_timeline_employee_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_timeline
    ADD CONSTRAINT employee_timeline_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES public.super_admin_employees(id) ON DELETE CASCADE;


--
-- Name: exam_admit_cards exam_admit_cards_class_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_admit_cards
    ADD CONSTRAINT exam_admit_cards_class_id_fkey FOREIGN KEY (class_id) REFERENCES public.class(id) ON DELETE CASCADE;


--
-- Name: exam_admit_cards exam_admit_cards_exam_setup_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_admit_cards
    ADD CONSTRAINT exam_admit_cards_exam_setup_id_fkey FOREIGN KEY (exam_setup_id) REFERENCES public.exam_setups(id) ON DELETE CASCADE;


--
-- Name: exam_admit_cards exam_admit_cards_school_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_admit_cards
    ADD CONSTRAINT exam_admit_cards_school_id_fkey FOREIGN KEY (school_id) REFERENCES public.schools(id) ON DELETE CASCADE;


--
-- Name: exam_admit_cards exam_admit_cards_section_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_admit_cards
    ADD CONSTRAINT exam_admit_cards_section_id_fkey FOREIGN KEY (section_id) REFERENCES public.sections(id) ON DELETE SET NULL;


--
-- Name: exam_admit_cards exam_admit_cards_student_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_admit_cards
    ADD CONSTRAINT exam_admit_cards_student_id_fkey FOREIGN KEY (student_id) REFERENCES public.student(id) ON DELETE CASCADE;


--
-- Name: exam_attendances exam_attendances_class_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_attendances
    ADD CONSTRAINT exam_attendances_class_id_fkey FOREIGN KEY (class_id) REFERENCES public.class(id) ON DELETE CASCADE;


--
-- Name: exam_attendances exam_attendances_exam_schedule_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_attendances
    ADD CONSTRAINT exam_attendances_exam_schedule_id_fkey FOREIGN KEY (exam_schedule_id) REFERENCES public.exam_schedules(id) ON DELETE SET NULL;


--
-- Name: exam_attendances exam_attendances_exam_setup_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_attendances
    ADD CONSTRAINT exam_attendances_exam_setup_id_fkey FOREIGN KEY (exam_setup_id) REFERENCES public.exam_setups(id) ON DELETE SET NULL;


--
-- Name: exam_attendances exam_attendances_school_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_attendances
    ADD CONSTRAINT exam_attendances_school_id_fkey FOREIGN KEY (school_id) REFERENCES public.schools(id) ON DELETE CASCADE;


--
-- Name: exam_attendances exam_attendances_section_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_attendances
    ADD CONSTRAINT exam_attendances_section_id_fkey FOREIGN KEY (section_id) REFERENCES public.sections(id) ON DELETE SET NULL;


--
-- Name: exam_attendances exam_attendances_student_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_attendances
    ADD CONSTRAINT exam_attendances_student_id_fkey FOREIGN KEY (student_id) REFERENCES public.student(id) ON DELETE CASCADE;


--
-- Name: exam_attendances exam_attendances_term_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_attendances
    ADD CONSTRAINT exam_attendances_term_id_fkey FOREIGN KEY (term_id) REFERENCES public.exam_terms(id) ON DELETE CASCADE;


--
-- Name: exam_co_curricular_grades exam_co_curricular_grades_class_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_co_curricular_grades
    ADD CONSTRAINT exam_co_curricular_grades_class_id_fkey FOREIGN KEY (class_id) REFERENCES public.class(id) ON DELETE CASCADE;


--
-- Name: exam_co_curricular_grades exam_co_curricular_grades_school_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_co_curricular_grades
    ADD CONSTRAINT exam_co_curricular_grades_school_id_fkey FOREIGN KEY (school_id) REFERENCES public.schools(id) ON DELETE CASCADE;


--
-- Name: exam_co_curricular_grades exam_co_curricular_grades_section_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_co_curricular_grades
    ADD CONSTRAINT exam_co_curricular_grades_section_id_fkey FOREIGN KEY (section_id) REFERENCES public.sections(id) ON DELETE SET NULL;


--
-- Name: exam_co_curricular_grades exam_co_curricular_grades_student_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_co_curricular_grades
    ADD CONSTRAINT exam_co_curricular_grades_student_id_fkey FOREIGN KEY (student_id) REFERENCES public.student(id) ON DELETE CASCADE;


--
-- Name: exam_co_curricular_grades exam_co_curricular_grades_term_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_co_curricular_grades
    ADD CONSTRAINT exam_co_curricular_grades_term_id_fkey FOREIGN KEY (term_id) REFERENCES public.exam_terms(id) ON DELETE CASCADE;


--
-- Name: exam_divisions exam_divisions_school_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_divisions
    ADD CONSTRAINT exam_divisions_school_id_fkey FOREIGN KEY (school_id) REFERENCES public.schools(id) ON DELETE CASCADE;


--
-- Name: exam_grade_scales exam_grade_scales_school_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_grade_scales
    ADD CONSTRAINT exam_grade_scales_school_id_fkey FOREIGN KEY (school_id) REFERENCES public.schools(id) ON DELETE CASCADE;


--
-- Name: exam_marks exam_marks_class_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_marks
    ADD CONSTRAINT exam_marks_class_id_fkey FOREIGN KEY (class_id) REFERENCES public.class(id) ON DELETE CASCADE;


--
-- Name: exam_marks exam_marks_exam_setup_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_marks
    ADD CONSTRAINT exam_marks_exam_setup_id_fkey FOREIGN KEY (exam_setup_id) REFERENCES public.exam_setups(id) ON DELETE CASCADE;


--
-- Name: exam_marks exam_marks_school_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_marks
    ADD CONSTRAINT exam_marks_school_id_fkey FOREIGN KEY (school_id) REFERENCES public.schools(id) ON DELETE CASCADE;


--
-- Name: exam_marks exam_marks_section_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_marks
    ADD CONSTRAINT exam_marks_section_id_fkey FOREIGN KEY (section_id) REFERENCES public.sections(id) ON DELETE SET NULL;


--
-- Name: exam_marks exam_marks_student_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_marks
    ADD CONSTRAINT exam_marks_student_id_fkey FOREIGN KEY (student_id) REFERENCES public.student(id) ON DELETE CASCADE;


--
-- Name: exam_marks exam_marks_subject_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_marks
    ADD CONSTRAINT exam_marks_subject_id_fkey FOREIGN KEY (subject_id) REFERENCES public.subjects(id) ON DELETE CASCADE;


--
-- Name: exam_report_card_batches exam_report_card_batches_class_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_report_card_batches
    ADD CONSTRAINT exam_report_card_batches_class_id_fkey FOREIGN KEY (class_id) REFERENCES public.class(id) ON DELETE SET NULL;


--
-- Name: exam_report_card_batches exam_report_card_batches_exam_setup_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_report_card_batches
    ADD CONSTRAINT exam_report_card_batches_exam_setup_id_fkey FOREIGN KEY (exam_setup_id) REFERENCES public.exam_setups(id) ON DELETE SET NULL;


--
-- Name: exam_report_card_batches exam_report_card_batches_school_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_report_card_batches
    ADD CONSTRAINT exam_report_card_batches_school_id_fkey FOREIGN KEY (school_id) REFERENCES public.schools(id) ON DELETE CASCADE;


--
-- Name: exam_report_card_batches exam_report_card_batches_section_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_report_card_batches
    ADD CONSTRAINT exam_report_card_batches_section_id_fkey FOREIGN KEY (section_id) REFERENCES public.sections(id) ON DELETE SET NULL;


--
-- Name: exam_report_card_batches exam_report_card_batches_term_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_report_card_batches
    ADD CONSTRAINT exam_report_card_batches_term_id_fkey FOREIGN KEY (term_id) REFERENCES public.exam_terms(id) ON DELETE SET NULL;


--
-- Name: exam_report_cards exam_report_cards_class_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_report_cards
    ADD CONSTRAINT exam_report_cards_class_id_fkey FOREIGN KEY (class_id) REFERENCES public.class(id) ON DELETE CASCADE;


--
-- Name: exam_report_cards exam_report_cards_exam_setup_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_report_cards
    ADD CONSTRAINT exam_report_cards_exam_setup_id_fkey FOREIGN KEY (exam_setup_id) REFERENCES public.exam_setups(id) ON DELETE SET NULL;


--
-- Name: exam_report_cards exam_report_cards_school_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_report_cards
    ADD CONSTRAINT exam_report_cards_school_id_fkey FOREIGN KEY (school_id) REFERENCES public.schools(id) ON DELETE CASCADE;


--
-- Name: exam_report_cards exam_report_cards_section_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_report_cards
    ADD CONSTRAINT exam_report_cards_section_id_fkey FOREIGN KEY (section_id) REFERENCES public.sections(id) ON DELETE SET NULL;


--
-- Name: exam_report_cards exam_report_cards_student_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_report_cards
    ADD CONSTRAINT exam_report_cards_student_id_fkey FOREIGN KEY (student_id) REFERENCES public.student(id) ON DELETE CASCADE;


--
-- Name: exam_report_cards exam_report_cards_term_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_report_cards
    ADD CONSTRAINT exam_report_cards_term_id_fkey FOREIGN KEY (term_id) REFERENCES public.exam_terms(id) ON DELETE CASCADE;


--
-- Name: exam_schedules exam_schedules_class_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_schedules
    ADD CONSTRAINT exam_schedules_class_id_fkey FOREIGN KEY (class_id) REFERENCES public.class(id) ON DELETE CASCADE;


--
-- Name: exam_schedules exam_schedules_exam_setup_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_schedules
    ADD CONSTRAINT exam_schedules_exam_setup_id_fkey FOREIGN KEY (exam_setup_id) REFERENCES public.exam_setups(id) ON DELETE CASCADE;


--
-- Name: exam_schedules exam_schedules_school_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_schedules
    ADD CONSTRAINT exam_schedules_school_id_fkey FOREIGN KEY (school_id) REFERENCES public.schools(id) ON DELETE CASCADE;


--
-- Name: exam_schedules exam_schedules_section_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_schedules
    ADD CONSTRAINT exam_schedules_section_id_fkey FOREIGN KEY (section_id) REFERENCES public.sections(id) ON DELETE SET NULL;


--
-- Name: exam_schedules exam_schedules_subject_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_schedules
    ADD CONSTRAINT exam_schedules_subject_id_fkey FOREIGN KEY (subject_id) REFERENCES public.subjects(id) ON DELETE CASCADE;


--
-- Name: exam_setups exam_setups_school_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_setups
    ADD CONSTRAINT exam_setups_school_id_fkey FOREIGN KEY (school_id) REFERENCES public.schools(id) ON DELETE CASCADE;


--
-- Name: exam_setups exam_setups_term_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_setups
    ADD CONSTRAINT exam_setups_term_id_fkey FOREIGN KEY (term_id) REFERENCES public.exam_terms(id) ON DELETE CASCADE;


--
-- Name: exam_teacher_remarks exam_teacher_remarks_class_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_teacher_remarks
    ADD CONSTRAINT exam_teacher_remarks_class_id_fkey FOREIGN KEY (class_id) REFERENCES public.class(id) ON DELETE CASCADE;


--
-- Name: exam_teacher_remarks exam_teacher_remarks_school_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_teacher_remarks
    ADD CONSTRAINT exam_teacher_remarks_school_id_fkey FOREIGN KEY (school_id) REFERENCES public.schools(id) ON DELETE CASCADE;


--
-- Name: exam_teacher_remarks exam_teacher_remarks_section_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_teacher_remarks
    ADD CONSTRAINT exam_teacher_remarks_section_id_fkey FOREIGN KEY (section_id) REFERENCES public.sections(id) ON DELETE SET NULL;


--
-- Name: exam_teacher_remarks exam_teacher_remarks_student_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_teacher_remarks
    ADD CONSTRAINT exam_teacher_remarks_student_id_fkey FOREIGN KEY (student_id) REFERENCES public.student(id) ON DELETE CASCADE;


--
-- Name: exam_teacher_remarks exam_teacher_remarks_term_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_teacher_remarks
    ADD CONSTRAINT exam_teacher_remarks_term_id_fkey FOREIGN KEY (term_id) REFERENCES public.exam_terms(id) ON DELETE CASCADE;


--
-- Name: exam_terms exam_terms_academic_year_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_terms
    ADD CONSTRAINT exam_terms_academic_year_id_fkey FOREIGN KEY (academic_year_id) REFERENCES public.academic_years(id) ON DELETE SET NULL;


--
-- Name: exam_terms exam_terms_school_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exam_terms
    ADD CONSTRAINT exam_terms_school_id_fkey FOREIGN KEY (school_id) REFERENCES public.schools(id) ON DELETE CASCADE;


--
-- Name: fee_due fee_due_collection_plan_item_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fee_due
    ADD CONSTRAINT fee_due_collection_plan_item_id_fkey FOREIGN KEY (collection_plan_item_id) REFERENCES public.collection_plan_item(id) ON DELETE SET NULL;


--
-- Name: fee_due fee_due_fee_category_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fee_due
    ADD CONSTRAINT fee_due_fee_category_id_fkey FOREIGN KEY (fee_category_id) REFERENCES public.fee_category(id);


--
-- Name: fee_due fee_due_fee_structure_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fee_due
    ADD CONSTRAINT fee_due_fee_structure_id_fkey FOREIGN KEY (fee_structure_id) REFERENCES public.fee_structure(id);


--
-- Name: fee_due fee_due_school_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fee_due
    ADD CONSTRAINT fee_due_school_id_fkey FOREIGN KEY (school_id) REFERENCES public.schools(id);


--
-- Name: fee_due fee_due_student_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fee_due
    ADD CONSTRAINT fee_due_student_id_fkey FOREIGN KEY (student_id) REFERENCES public.student(id);


--
-- Name: fee_payment fee_payment_account_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fee_payment
    ADD CONSTRAINT fee_payment_account_id_fkey FOREIGN KEY (account_id) REFERENCES public.school_account(id);


--
-- Name: fee_payment_allocation fee_payment_allocation_fee_due_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fee_payment_allocation
    ADD CONSTRAINT fee_payment_allocation_fee_due_id_fkey FOREIGN KEY (fee_due_id) REFERENCES public.fee_due(id);


--
-- Name: fee_payment_allocation fee_payment_allocation_fee_payment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fee_payment_allocation
    ADD CONSTRAINT fee_payment_allocation_fee_payment_id_fkey FOREIGN KEY (fee_payment_id) REFERENCES public.fee_payment(id);


--
-- Name: fee_structure fee_structure_collection_plan_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fee_structure
    ADD CONSTRAINT fee_structure_collection_plan_id_fkey FOREIGN KEY (collection_plan_id) REFERENCES public.collection_plan(id) ON DELETE SET NULL;


--
-- Name: admission_enquiries fk_admission_enquiry_assigned_staff; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.admission_enquiries
    ADD CONSTRAINT fk_admission_enquiry_assigned_staff FOREIGN KEY (assigned_to) REFERENCES public.staff(id) ON DELETE SET NULL;


--
-- Name: admission_enquiries fk_admission_enquiry_class; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.admission_enquiries
    ADD CONSTRAINT fk_admission_enquiry_class FOREIGN KEY (class_id) REFERENCES public.class(id) ON DELETE SET NULL;


--
-- Name: admission_enquiries fk_admission_enquiry_school; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.admission_enquiries
    ADD CONSTRAINT fk_admission_enquiry_school FOREIGN KEY (school_id) REFERENCES public.schools(id) ON DELETE CASCADE;


--
-- Name: account_requests fk_ar_school; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.account_requests
    ADD CONSTRAINT fk_ar_school FOREIGN KEY (school_id) REFERENCES public.schools(id);


--
-- Name: account_requests fk_ar_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.account_requests
    ADD CONSTRAINT fk_ar_user FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: employee_attendance fk_attendance_employee; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_attendance
    ADD CONSTRAINT fk_attendance_employee FOREIGN KEY (employee_id) REFERENCES public.super_admin_employees(id) ON DELETE CASCADE;


--
-- Name: student_attendance fk_attendance_school; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_attendance
    ADD CONSTRAINT fk_attendance_school FOREIGN KEY (school_id) REFERENCES public.schools(id);


--
-- Name: student_attendance fk_attendance_student; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_attendance
    ADD CONSTRAINT fk_attendance_student FOREIGN KEY (student_id) REFERENCES public.student(id);


--
-- Name: employee_audit_logs fk_audit_employee; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_audit_logs
    ADD CONSTRAINT fk_audit_employee FOREIGN KEY (employee_id) REFERENCES public.super_admin_employees(id) ON DELETE SET NULL;


--
-- Name: auth_sessions fk_auth_sessions_school; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.auth_sessions
    ADD CONSTRAINT fk_auth_sessions_school FOREIGN KEY (school_id) REFERENCES public.schools(id);


--
-- Name: auth_sessions fk_auth_sessions_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.auth_sessions
    ADD CONSTRAINT fk_auth_sessions_user FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: academic_years fk_ay_school; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.academic_years
    ADD CONSTRAINT fk_ay_school FOREIGN KEY (school_id) REFERENCES public.schools(id) ON DELETE CASCADE;


--
-- Name: student_categories fk_category_school; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_categories
    ADD CONSTRAINT fk_category_school FOREIGN KEY (school_id) REFERENCES public.schools(id) ON DELETE CASCADE;


--
-- Name: class fk_class_school; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.class
    ADD CONSTRAINT fk_class_school FOREIGN KEY (school_id) REFERENCES public.schools(id);


--
-- Name: departments fk_department_head; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.departments
    ADD CONSTRAINT fk_department_head FOREIGN KEY (head_employee_id) REFERENCES public.super_admin_employees(id) ON DELETE SET NULL;


--
-- Name: student_documents fk_doc_school; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_documents
    ADD CONSTRAINT fk_doc_school FOREIGN KEY (school_id) REFERENCES public.schools(id) ON DELETE CASCADE;


--
-- Name: student_documents fk_doc_student; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_documents
    ADD CONSTRAINT fk_doc_student FOREIGN KEY (student_id) REFERENCES public.student(id) ON DELETE CASCADE;


--
-- Name: employee_documents fk_document_employee; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_documents
    ADD CONSTRAINT fk_document_employee FOREIGN KEY (employee_id) REFERENCES public.super_admin_employees(id) ON DELETE CASCADE;


--
-- Name: employee_documents fk_document_uploader; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_documents
    ADD CONSTRAINT fk_document_uploader FOREIGN KEY (uploaded_by) REFERENCES public.super_admin_employees(id) ON DELETE SET NULL;


--
-- Name: admission_enquiry_follow_ups fk_enquiry_follow_up_enquiry; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.admission_enquiry_follow_ups
    ADD CONSTRAINT fk_enquiry_follow_up_enquiry FOREIGN KEY (enquiry_id) REFERENCES public.admission_enquiries(id) ON DELETE CASCADE;


--
-- Name: admission_enquiry_follow_ups fk_enquiry_follow_up_staff; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.admission_enquiry_follow_ups
    ADD CONSTRAINT fk_enquiry_follow_up_staff FOREIGN KEY (recorded_by) REFERENCES public.staff(id) ON DELETE SET NULL;


--
-- Name: families fk_family_school; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.families
    ADD CONSTRAINT fk_family_school FOREIGN KEY (school_id) REFERENCES public.schools(id) ON DELETE CASCADE;


--
-- Name: fee_category fk_fee_category_school; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fee_category
    ADD CONSTRAINT fk_fee_category_school FOREIGN KEY (school_id) REFERENCES public.schools(id);


--
-- Name: fee_invoice fk_fee_invoice_school; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fee_invoice
    ADD CONSTRAINT fk_fee_invoice_school FOREIGN KEY (school_id) REFERENCES public.schools(id);


--
-- Name: fee_invoice fk_fee_invoice_structure; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fee_invoice
    ADD CONSTRAINT fk_fee_invoice_structure FOREIGN KEY (fee_structure_id) REFERENCES public.fee_structure(id) ON DELETE SET NULL;


--
-- Name: fee_invoice fk_fee_invoice_student; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fee_invoice
    ADD CONSTRAINT fk_fee_invoice_student FOREIGN KEY (student_id) REFERENCES public.student(id);


--
-- Name: fee_payment fk_fee_payment_school; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fee_payment
    ADD CONSTRAINT fk_fee_payment_school FOREIGN KEY (school_id) REFERENCES public.schools(id);


--
-- Name: fee_payment fk_fee_payment_student; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fee_payment
    ADD CONSTRAINT fk_fee_payment_student FOREIGN KEY (student_id) REFERENCES public.student(id);


--
-- Name: fee_reminders fk_fee_reminders_school; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fee_reminders
    ADD CONSTRAINT fk_fee_reminders_school FOREIGN KEY (school_id) REFERENCES public.schools(id);


--
-- Name: fee_reminders fk_fee_reminders_staff; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fee_reminders
    ADD CONSTRAINT fk_fee_reminders_staff FOREIGN KEY (staff_id) REFERENCES public.staff(id);


--
-- Name: fee_reminders fk_fee_reminders_student; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fee_reminders
    ADD CONSTRAINT fk_fee_reminders_student FOREIGN KEY (student_id) REFERENCES public.student(id);


--
-- Name: fee_structure fk_fee_structure_class; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fee_structure
    ADD CONSTRAINT fk_fee_structure_class FOREIGN KEY (class_id) REFERENCES public.class(id) ON DELETE SET NULL;


--
-- Name: fee_structure fk_fee_structure_school; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fee_structure
    ADD CONSTRAINT fk_fee_structure_school FOREIGN KEY (school_id) REFERENCES public.schools(id);


--
-- Name: fee_generation_batch fk_fgb_class; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fee_generation_batch
    ADD CONSTRAINT fk_fgb_class FOREIGN KEY (class_id) REFERENCES public.class(id);


--
-- Name: fee_generation_batch fk_fgb_collection_plan; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fee_generation_batch
    ADD CONSTRAINT fk_fgb_collection_plan FOREIGN KEY (collection_plan_id) REFERENCES public.collection_plan(id);


--
-- Name: fee_generation_batch fk_fgb_fee_structure; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fee_generation_batch
    ADD CONSTRAINT fk_fgb_fee_structure FOREIGN KEY (fee_structure_id) REFERENCES public.fee_structure(id);


--
-- Name: fee_generation_batch fk_fgb_school; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fee_generation_batch
    ADD CONSTRAINT fk_fgb_school FOREIGN KEY (school_id) REFERENCES public.schools(id);


--
-- Name: fee_structure_item fk_fsi_category; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fee_structure_item
    ADD CONSTRAINT fk_fsi_category FOREIGN KEY (fee_category_id) REFERENCES public.fee_category(id);


--
-- Name: fee_structure_item fk_fsi_structure; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fee_structure_item
    ADD CONSTRAINT fk_fsi_structure FOREIGN KEY (fee_structure_id) REFERENCES public.fee_structure(id) ON DELETE CASCADE;


--
-- Name: gate_passes fk_gate_passes_approver; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gate_passes
    ADD CONSTRAINT fk_gate_passes_approver FOREIGN KEY (approved_by_staff_id) REFERENCES public.staff(id) ON DELETE SET NULL;


--
-- Name: gate_passes fk_gate_passes_school; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gate_passes
    ADD CONSTRAINT fk_gate_passes_school FOREIGN KEY (school_id) REFERENCES public.schools(id) ON DELETE CASCADE;


--
-- Name: gate_passes fk_gate_passes_staff; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gate_passes
    ADD CONSTRAINT fk_gate_passes_staff FOREIGN KEY (staff_id) REFERENCES public.staff(id) ON DELETE SET NULL;


--
-- Name: gate_passes fk_gate_passes_student; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gate_passes
    ADD CONSTRAINT fk_gate_passes_student FOREIGN KEY (student_id) REFERENCES public.student(id) ON DELETE SET NULL;


--
-- Name: student_houses fk_house_school; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_houses
    ADD CONSTRAINT fk_house_school FOREIGN KEY (school_id) REFERENCES public.schools(id) ON DELETE CASCADE;


--
-- Name: employee_leaves fk_leaves_approver; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_leaves
    ADD CONSTRAINT fk_leaves_approver FOREIGN KEY (approved_by) REFERENCES public.super_admin_employees(id) ON DELETE SET NULL;


--
-- Name: employee_leaves fk_leaves_employee; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_leaves
    ADD CONSTRAINT fk_leaves_employee FOREIGN KEY (employee_id) REFERENCES public.super_admin_employees(id) ON DELETE CASCADE;


--
-- Name: parcel_dispatches fk_parcel_dispatches_school; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcel_dispatches
    ADD CONSTRAINT fk_parcel_dispatches_school FOREIGN KEY (school_id) REFERENCES public.schools(id) ON DELETE CASCADE;


--
-- Name: parcel_receives fk_parcel_receives_school; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcel_receives
    ADD CONSTRAINT fk_parcel_receives_school FOREIGN KEY (school_id) REFERENCES public.schools(id) ON DELETE CASCADE;


--
-- Name: parcel_receives fk_parcel_receives_staff; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcel_receives
    ADD CONSTRAINT fk_parcel_receives_staff FOREIGN KEY (received_by_staff_id) REFERENCES public.staff(id) ON DELETE SET NULL;


--
-- Name: password_resets fk_password_resets_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.password_resets
    ADD CONSTRAINT fk_password_resets_user FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: employee_payroll fk_payroll_employee; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_payroll
    ADD CONSTRAINT fk_payroll_employee FOREIGN KEY (employee_id) REFERENCES public.super_admin_employees(id) ON DELETE CASCADE;


--
-- Name: employee_performance fk_performance_employee; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_performance
    ADD CONSTRAINT fk_performance_employee FOREIGN KEY (employee_id) REFERENCES public.super_admin_employees(id) ON DELETE CASCADE;


--
-- Name: employee_performance fk_performance_reviewer; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employee_performance
    ADD CONSTRAINT fk_performance_reviewer FOREIGN KEY (reviewer_id) REFERENCES public.super_admin_employees(id) ON DELETE SET NULL;


--
-- Name: timetable_periods fk_period_school; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.timetable_periods
    ADD CONSTRAINT fk_period_school FOREIGN KEY (school_id) REFERENCES public.schools(id) ON DELETE CASCADE;


--
-- Name: plan_modules fk_plan_modules_module; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.plan_modules
    ADD CONSTRAINT fk_plan_modules_module FOREIGN KEY (module_id) REFERENCES public.platform_modules(id) ON DELETE CASCADE;


--
-- Name: plan_modules fk_plan_modules_plan; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.plan_modules
    ADD CONSTRAINT fk_plan_modules_plan FOREIGN KEY (plan_id) REFERENCES public.subscription_plans(id) ON DELETE CASCADE;


--
-- Name: password_reset_tokens fk_prt_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.password_reset_tokens
    ADD CONSTRAINT fk_prt_user FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: recruitment_candidates fk_recruitment_school; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recruitment_candidates
    ADD CONSTRAINT fk_recruitment_school FOREIGN KEY (school_id) REFERENCES public.schools(id) ON DELETE CASCADE;


--
-- Name: school_config_overrides fk_school_config_overrides_school; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.school_config_overrides
    ADD CONSTRAINT fk_school_config_overrides_school FOREIGN KEY (school_id) REFERENCES public.schools(id) ON DELETE CASCADE;


--
-- Name: school_departments fk_school_dept_school; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.school_departments
    ADD CONSTRAINT fk_school_dept_school FOREIGN KEY (school_id) REFERENCES public.schools(id) ON DELETE CASCADE;


--
-- Name: school_designations fk_school_desig_dept; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.school_designations
    ADD CONSTRAINT fk_school_desig_dept FOREIGN KEY (department_id) REFERENCES public.school_departments(id) ON DELETE SET NULL;


--
-- Name: school_designations fk_school_desig_school; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.school_designations
    ADD CONSTRAINT fk_school_desig_school FOREIGN KEY (school_id) REFERENCES public.schools(id) ON DELETE CASCADE;


--
-- Name: school_leave_types fk_school_leave_type_school; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.school_leave_types
    ADD CONSTRAINT fk_school_leave_type_school FOREIGN KEY (school_id) REFERENCES public.schools(id) ON DELETE CASCADE;


--
-- Name: school_module_access fk_school_module_access_module; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.school_module_access
    ADD CONSTRAINT fk_school_module_access_module FOREIGN KEY (module_id) REFERENCES public.platform_modules(id) ON DELETE CASCADE;


--
-- Name: school_module_access fk_school_module_access_school; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.school_module_access
    ADD CONSTRAINT fk_school_module_access_school FOREIGN KEY (school_id) REFERENCES public.schools(id) ON DELETE CASCADE;


--
-- Name: schools fk_schools_plan; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.schools
    ADD CONSTRAINT fk_schools_plan FOREIGN KEY (plan_id) REFERENCES public.subscription_plans(id) ON DELETE SET NULL;


--
-- Name: sections fk_section_class; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sections
    ADD CONSTRAINT fk_section_class FOREIGN KEY (class_id) REFERENCES public.class(id) ON DELETE CASCADE;


--
-- Name: sections fk_section_school; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sections
    ADD CONSTRAINT fk_section_school FOREIGN KEY (school_id) REFERENCES public.schools(id) ON DELETE CASCADE;


--
-- Name: staff_advances fk_staff_adv_school; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.staff_advances
    ADD CONSTRAINT fk_staff_adv_school FOREIGN KEY (school_id) REFERENCES public.schools(id) ON DELETE CASCADE;


--
-- Name: staff_advances fk_staff_adv_staff; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.staff_advances
    ADD CONSTRAINT fk_staff_adv_staff FOREIGN KEY (staff_id) REFERENCES public.staff(id) ON DELETE CASCADE;


--
-- Name: staff_attendance fk_staff_att_school; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.staff_attendance
    ADD CONSTRAINT fk_staff_att_school FOREIGN KEY (school_id) REFERENCES public.schools(id) ON DELETE CASCADE;


--
-- Name: staff_attendance fk_staff_att_staff; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.staff_attendance
    ADD CONSTRAINT fk_staff_att_staff FOREIGN KEY (staff_id) REFERENCES public.staff(id) ON DELETE CASCADE;


--
-- Name: staff_bank_accounts fk_staff_bank_staff; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.staff_bank_accounts
    ADD CONSTRAINT fk_staff_bank_staff FOREIGN KEY (staff_id) REFERENCES public.staff(id) ON DELETE CASCADE;


--
-- Name: staff_documents fk_staff_doc_staff; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.staff_documents
    ADD CONSTRAINT fk_staff_doc_staff FOREIGN KEY (staff_id) REFERENCES public.staff(id) ON DELETE CASCADE;


--
-- Name: staff_leaves fk_staff_leave_approver; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.staff_leaves
    ADD CONSTRAINT fk_staff_leave_approver FOREIGN KEY (approved_by) REFERENCES public.staff(id) ON DELETE SET NULL;


--
-- Name: staff_leave_balances fk_staff_leave_bal_school; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.staff_leave_balances
    ADD CONSTRAINT fk_staff_leave_bal_school FOREIGN KEY (school_id) REFERENCES public.schools(id) ON DELETE CASCADE;


--
-- Name: staff_leave_balances fk_staff_leave_bal_staff; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.staff_leave_balances
    ADD CONSTRAINT fk_staff_leave_bal_staff FOREIGN KEY (staff_id) REFERENCES public.staff(id) ON DELETE CASCADE;


--
-- Name: staff_leave_balances fk_staff_leave_bal_type; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.staff_leave_balances
    ADD CONSTRAINT fk_staff_leave_bal_type FOREIGN KEY (leave_type_id) REFERENCES public.school_leave_types(id) ON DELETE CASCADE;


--
-- Name: staff_leaves fk_staff_leave_school; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.staff_leaves
    ADD CONSTRAINT fk_staff_leave_school FOREIGN KEY (school_id) REFERENCES public.schools(id) ON DELETE CASCADE;


--
-- Name: staff_leaves fk_staff_leave_staff; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.staff_leaves
    ADD CONSTRAINT fk_staff_leave_staff FOREIGN KEY (staff_id) REFERENCES public.staff(id) ON DELETE CASCADE;


--
-- Name: staff_leaves fk_staff_leave_type; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.staff_leaves
    ADD CONSTRAINT fk_staff_leave_type FOREIGN KEY (leave_type_id) REFERENCES public.school_leave_types(id) ON DELETE CASCADE;


--
-- Name: staff_payroll_details fk_staff_payroll_details_staff; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.staff_payroll_details
    ADD CONSTRAINT fk_staff_payroll_details_staff FOREIGN KEY (staff_id) REFERENCES public.staff(id) ON DELETE CASCADE;


--
-- Name: staff_payrolls fk_staff_payroll_school; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.staff_payrolls
    ADD CONSTRAINT fk_staff_payroll_school FOREIGN KEY (school_id) REFERENCES public.schools(id) ON DELETE CASCADE;


--
-- Name: staff_payrolls fk_staff_payroll_staff; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.staff_payrolls
    ADD CONSTRAINT fk_staff_payroll_staff FOREIGN KEY (staff_id) REFERENCES public.staff(id) ON DELETE CASCADE;


--
-- Name: staff fk_staff_school; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.staff
    ADD CONSTRAINT fk_staff_school FOREIGN KEY (school_id) REFERENCES public.schools(id);


--
-- Name: staff_tasks fk_staff_task_school; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.staff_tasks
    ADD CONSTRAINT fk_staff_task_school FOREIGN KEY (school_id) REFERENCES public.schools(id) ON DELETE CASCADE;


--
-- Name: staff_tasks fk_staff_task_staff; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.staff_tasks
    ADD CONSTRAINT fk_staff_task_staff FOREIGN KEY (staff_id) REFERENCES public.staff(id) ON DELETE CASCADE;


--
-- Name: student fk_student_category; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student
    ADD CONSTRAINT fk_student_category FOREIGN KEY (category_id) REFERENCES public.student_categories(id) ON DELETE SET NULL;


--
-- Name: student fk_student_class; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student
    ADD CONSTRAINT fk_student_class FOREIGN KEY (class_id) REFERENCES public.class(id);


--
-- Name: student fk_student_family; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student
    ADD CONSTRAINT fk_student_family FOREIGN KEY (family_id) REFERENCES public.families(id) ON DELETE SET NULL;


--
-- Name: student fk_student_house; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student
    ADD CONSTRAINT fk_student_house FOREIGN KEY (house_id) REFERENCES public.student_houses(id) ON DELETE SET NULL;


--
-- Name: student_parents fk_student_parents_parent; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_parents
    ADD CONSTRAINT fk_student_parents_parent FOREIGN KEY (parent_user_id) REFERENCES public.users(id);


--
-- Name: student_parents fk_student_parents_student; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_parents
    ADD CONSTRAINT fk_student_parents_student FOREIGN KEY (student_id) REFERENCES public.student(id);


--
-- Name: student fk_student_school; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student
    ADD CONSTRAINT fk_student_school FOREIGN KEY (school_id) REFERENCES public.schools(id);


--
-- Name: subjects fk_subject_school; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.subjects
    ADD CONSTRAINT fk_subject_school FOREIGN KEY (school_id) REFERENCES public.schools(id) ON DELETE CASCADE;


--
-- Name: tenant_entitlement_overrides fk_tenant_entitlement_overrides_school; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tenant_entitlement_overrides
    ADD CONSTRAINT fk_tenant_entitlement_overrides_school FOREIGN KEY (school_id) REFERENCES public.schools(id) ON DELETE CASCADE;


--
-- Name: user_activity_logs fk_ual_school; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_activity_logs
    ADD CONSTRAINT fk_ual_school FOREIGN KEY (school_id) REFERENCES public.schools(id);


--
-- Name: user_activity_logs fk_ual_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_activity_logs
    ADD CONSTRAINT fk_ual_user FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: user_login_history fk_ulh_school; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_login_history
    ADD CONSTRAINT fk_ulh_school FOREIGN KEY (school_id) REFERENCES public.schools(id);


--
-- Name: user_login_history fk_ulh_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_login_history
    ADD CONSTRAINT fk_ulh_user FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: user_requests fk_user_requests_school; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_requests
    ADD CONSTRAINT fk_user_requests_school FOREIGN KEY (school_id) REFERENCES public.schools(id) ON DELETE CASCADE;


--
-- Name: user_requests fk_user_requests_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_requests
    ADD CONSTRAINT fk_user_requests_user FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: user_school_roles fk_user_school_roles_school; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_school_roles
    ADD CONSTRAINT fk_user_school_roles_school FOREIGN KEY (school_id) REFERENCES public.schools(id);


--
-- Name: user_school_roles fk_user_school_roles_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_school_roles
    ADD CONSTRAINT fk_user_school_roles_user FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: visitor_logs fk_visitor_logs_school; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.visitor_logs
    ADD CONSTRAINT fk_visitor_logs_school FOREIGN KEY (school_id) REFERENCES public.schools(id) ON DELETE CASCADE;


--
-- Name: visitor_logs fk_visitor_logs_staff; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.visitor_logs
    ADD CONSTRAINT fk_visitor_logs_staff FOREIGN KEY (meeting_with_staff_id) REFERENCES public.staff(id) ON DELETE SET NULL;


--
-- Name: id_card_generations id_card_generations_school_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.id_card_generations
    ADD CONSTRAINT id_card_generations_school_id_fkey FOREIGN KEY (school_id) REFERENCES public.schools(id);


--
-- Name: id_card_generations id_card_generations_student_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.id_card_generations
    ADD CONSTRAINT id_card_generations_student_id_fkey FOREIGN KEY (student_id) REFERENCES public.student(id);


--
-- Name: impersonation_sessions impersonation_sessions_impersonated_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.impersonation_sessions
    ADD CONSTRAINT impersonation_sessions_impersonated_user_id_fkey FOREIGN KEY (impersonated_user_id) REFERENCES public.users(id);


--
-- Name: impersonation_sessions impersonation_sessions_original_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.impersonation_sessions
    ADD CONSTRAINT impersonation_sessions_original_user_id_fkey FOREIGN KEY (original_user_id) REFERENCES public.users(id);


--
-- Name: online_admissions online_admissions_category_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.online_admissions
    ADD CONSTRAINT online_admissions_category_id_fkey FOREIGN KEY (category_id) REFERENCES public.student_categories(id);


--
-- Name: online_admissions online_admissions_class_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.online_admissions
    ADD CONSTRAINT online_admissions_class_id_fkey FOREIGN KEY (class_id) REFERENCES public.class(id);


--
-- Name: online_admissions online_admissions_school_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.online_admissions
    ADD CONSTRAINT online_admissions_school_id_fkey FOREIGN KEY (school_id) REFERENCES public.schools(id);


--
-- Name: role_permissions role_permissions_permission_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.role_permissions
    ADD CONSTRAINT role_permissions_permission_id_fkey FOREIGN KEY (permission_id) REFERENCES public.permission_definitions(id);


--
-- Name: role_permissions role_permissions_role_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.role_permissions
    ADD CONSTRAINT role_permissions_role_id_fkey FOREIGN KEY (role_id) REFERENCES public.roles(id);


--
-- Name: school_account school_account_school_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.school_account
    ADD CONSTRAINT school_account_school_id_fkey FOREIGN KEY (school_id) REFERENCES public.schools(id);


--
-- Name: school_subscription_installments school_subscription_installments_subscription_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.school_subscription_installments
    ADD CONSTRAINT school_subscription_installments_subscription_id_fkey FOREIGN KEY (subscription_id) REFERENCES public.school_subscriptions(id) ON DELETE CASCADE;


--
-- Name: school_subscriptions school_subscriptions_plan_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.school_subscriptions
    ADD CONSTRAINT school_subscriptions_plan_id_fkey FOREIGN KEY (plan_id) REFERENCES public.subscription_plans(id);


--
-- Name: school_subscriptions school_subscriptions_school_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.school_subscriptions
    ADD CONSTRAINT school_subscriptions_school_id_fkey FOREIGN KEY (school_id) REFERENCES public.schools(id) ON DELETE CASCADE;


--
-- Name: student_certificates student_certificates_school_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_certificates
    ADD CONSTRAINT student_certificates_school_id_fkey FOREIGN KEY (school_id) REFERENCES public.schools(id);


--
-- Name: student_certificates student_certificates_student_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_certificates
    ADD CONSTRAINT student_certificates_student_id_fkey FOREIGN KEY (student_id) REFERENCES public.student(id);


--
-- Name: student_fee_structure student_fee_structure_academic_year_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_fee_structure
    ADD CONSTRAINT student_fee_structure_academic_year_id_fkey FOREIGN KEY (academic_year_id) REFERENCES public.academic_years(id);


--
-- Name: student_fee_structure student_fee_structure_collection_plan_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_fee_structure
    ADD CONSTRAINT student_fee_structure_collection_plan_id_fkey FOREIGN KEY (collection_plan_id) REFERENCES public.collection_plan(id) ON DELETE SET NULL;


--
-- Name: student_fee_structure student_fee_structure_fee_structure_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_fee_structure
    ADD CONSTRAINT student_fee_structure_fee_structure_id_fkey FOREIGN KEY (fee_structure_id) REFERENCES public.fee_structure(id);


--
-- Name: student_fee_structure student_fee_structure_student_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_fee_structure
    ADD CONSTRAINT student_fee_structure_student_id_fkey FOREIGN KEY (student_id) REFERENCES public.student(id);


--
-- Name: student_leaves student_leaves_school_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_leaves
    ADD CONSTRAINT student_leaves_school_id_fkey FOREIGN KEY (school_id) REFERENCES public.schools(id);


--
-- Name: student_leaves student_leaves_student_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_leaves
    ADD CONSTRAINT student_leaves_student_id_fkey FOREIGN KEY (student_id) REFERENCES public.student(id);


--
-- Name: student_referrals student_referrals_school_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_referrals
    ADD CONSTRAINT student_referrals_school_id_fkey FOREIGN KEY (school_id) REFERENCES public.schools(id);


--
-- Name: student_siblings student_siblings_primary_student_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_siblings
    ADD CONSTRAINT student_siblings_primary_student_id_fkey FOREIGN KEY (primary_student_id) REFERENCES public.student(id);


--
-- Name: student_siblings student_siblings_school_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_siblings
    ADD CONSTRAINT student_siblings_school_id_fkey FOREIGN KEY (school_id) REFERENCES public.schools(id);


--
-- Name: student_siblings student_siblings_sibling_student_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_siblings
    ADD CONSTRAINT student_siblings_sibling_student_id_fkey FOREIGN KEY (sibling_student_id) REFERENCES public.student(id);


--
-- Name: student_subject_enrollments student_subject_enrollments_academic_year_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_subject_enrollments
    ADD CONSTRAINT student_subject_enrollments_academic_year_id_fkey FOREIGN KEY (academic_year_id) REFERENCES public.academic_years(id);


--
-- Name: student_subject_enrollments student_subject_enrollments_school_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_subject_enrollments
    ADD CONSTRAINT student_subject_enrollments_school_id_fkey FOREIGN KEY (school_id) REFERENCES public.schools(id);


--
-- Name: student_subject_enrollments student_subject_enrollments_student_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_subject_enrollments
    ADD CONSTRAINT student_subject_enrollments_student_id_fkey FOREIGN KEY (student_id) REFERENCES public.student(id);


--
-- Name: student_subject_enrollments student_subject_enrollments_subject_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.student_subject_enrollments
    ADD CONSTRAINT student_subject_enrollments_subject_id_fkey FOREIGN KEY (subject_id) REFERENCES public.subjects(id);


--
-- Name: subject_attendance subject_attendance_class_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.subject_attendance
    ADD CONSTRAINT subject_attendance_class_id_fkey FOREIGN KEY (class_id) REFERENCES public.class(id) ON DELETE CASCADE;


--
-- Name: subject_attendance subject_attendance_school_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.subject_attendance
    ADD CONSTRAINT subject_attendance_school_id_fkey FOREIGN KEY (school_id) REFERENCES public.schools(id) ON DELETE CASCADE;


--
-- Name: subject_attendance subject_attendance_section_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.subject_attendance
    ADD CONSTRAINT subject_attendance_section_id_fkey FOREIGN KEY (section_id) REFERENCES public.sections(id) ON DELETE SET NULL;


--
-- Name: subject_attendance subject_attendance_student_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.subject_attendance
    ADD CONSTRAINT subject_attendance_student_id_fkey FOREIGN KEY (student_id) REFERENCES public.student(id) ON DELETE CASCADE;


--
-- Name: subject_attendance subject_attendance_subject_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.subject_attendance
    ADD CONSTRAINT subject_attendance_subject_id_fkey FOREIGN KEY (subject_id) REFERENCES public.subjects(id) ON DELETE CASCADE;


--
-- Name: subject_attendance subject_attendance_timetable_entry_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.subject_attendance
    ADD CONSTRAINT subject_attendance_timetable_entry_id_fkey FOREIGN KEY (timetable_entry_id) REFERENCES public.timetable_entries(id) ON DELETE SET NULL;


--
-- Name: super_admin_employees super_admin_employees_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.super_admin_employees
    ADD CONSTRAINT super_admin_employees_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: support_tickets support_tickets_assigned_employee_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.support_tickets
    ADD CONSTRAINT support_tickets_assigned_employee_id_fkey FOREIGN KEY (assigned_employee_id) REFERENCES public.users(id);


--
-- Name: support_tickets support_tickets_category_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.support_tickets
    ADD CONSTRAINT support_tickets_category_id_fkey FOREIGN KEY (category_id) REFERENCES public.ticket_categories(id);


--
-- Name: support_tickets support_tickets_creator_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.support_tickets
    ADD CONSTRAINT support_tickets_creator_user_id_fkey FOREIGN KEY (creator_user_id) REFERENCES public.users(id);


--
-- Name: support_tickets support_tickets_school_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.support_tickets
    ADD CONSTRAINT support_tickets_school_id_fkey FOREIGN KEY (school_id) REFERENCES public.schools(id);


--
-- Name: ticket_histories ticket_histories_employee_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ticket_histories
    ADD CONSTRAINT ticket_histories_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES public.users(id);


--
-- Name: ticket_histories ticket_histories_ticket_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ticket_histories
    ADD CONSTRAINT ticket_histories_ticket_id_fkey FOREIGN KEY (ticket_id) REFERENCES public.support_tickets(id);


--
-- Name: timetable_entries timetable_entries_academic_year_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.timetable_entries
    ADD CONSTRAINT timetable_entries_academic_year_id_fkey FOREIGN KEY (academic_year_id) REFERENCES public.academic_years(id);


--
-- Name: timetable_entries timetable_entries_class_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.timetable_entries
    ADD CONSTRAINT timetable_entries_class_id_fkey FOREIGN KEY (class_id) REFERENCES public.class(id);


--
-- Name: timetable_entries timetable_entries_period_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.timetable_entries
    ADD CONSTRAINT timetable_entries_period_id_fkey FOREIGN KEY (period_id) REFERENCES public.timetable_periods(id);


--
-- Name: timetable_entries timetable_entries_school_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.timetable_entries
    ADD CONSTRAINT timetable_entries_school_id_fkey FOREIGN KEY (school_id) REFERENCES public.schools(id);


--
-- Name: timetable_entries timetable_entries_section_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.timetable_entries
    ADD CONSTRAINT timetable_entries_section_id_fkey FOREIGN KEY (section_id) REFERENCES public.sections(id);


--
-- Name: timetable_entries timetable_entries_subject_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.timetable_entries
    ADD CONSTRAINT timetable_entries_subject_id_fkey FOREIGN KEY (subject_id) REFERENCES public.subjects(id);


--
-- Name: timetable_entries timetable_entries_teacher_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.timetable_entries
    ADD CONSTRAINT timetable_entries_teacher_id_fkey FOREIGN KEY (teacher_id) REFERENCES public.staff(id);


--
-- Name: user_assignments user_assignments_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_assignments
    ADD CONSTRAINT user_assignments_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: user_role_mappings user_role_mappings_role_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_role_mappings
    ADD CONSTRAINT user_role_mappings_role_id_fkey FOREIGN KEY (role_id) REFERENCES public.roles(id);


--
-- Name: user_role_mappings user_role_mappings_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_role_mappings
    ADD CONSTRAINT user_role_mappings_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- PostgreSQL database dump complete
--
