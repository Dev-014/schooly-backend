CREATE TABLE fee_generation_batch (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    academic_year_id BIGINT NOT NULL,
    class_id BIGINT NOT NULL,
    fee_structure_id BIGINT NOT NULL,
    collection_plan_id BIGINT,
    number_of_students INT NOT NULL,
    total_amount_generated NUMERIC(10, 2) NOT NULL,
    generated_by VARCHAR(255) NOT NULL,
    generated_at TIMESTAMP NOT NULL,
    
    CONSTRAINT fk_fgb_school FOREIGN KEY (school_id) REFERENCES schools(id),
    CONSTRAINT fk_fgb_class FOREIGN KEY (class_id) REFERENCES class(id),
    CONSTRAINT fk_fgb_fee_structure FOREIGN KEY (fee_structure_id) REFERENCES fee_structure(id),
    CONSTRAINT fk_fgb_collection_plan FOREIGN KEY (collection_plan_id) REFERENCES collection_plan(id)
);

CREATE INDEX idx_fgb_school_year ON fee_generation_batch (school_id, academic_year_id);
