CREATE TABLE id_card_generations (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL REFERENCES schools(id),
    student_id BIGINT NOT NULL REFERENCES student(id),
    status VARCHAR(50) NOT NULL DEFAULT 'GENERATED',
    generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_id_card_generations_school_id ON id_card_generations(school_id);
CREATE UNIQUE INDEX idx_id_card_generations_student_id ON id_card_generations(student_id);
