ALTER TABLE incomes
    ADD COLUMN academic_year_id BIGINT,
    ADD CONSTRAINT fk_income_academic_year
        FOREIGN KEY (academic_year_id) REFERENCES academic_years (id) ON DELETE SET NULL;

CREATE INDEX idx_incomes_academic_year_id ON incomes (academic_year_id);

ALTER TABLE expenses
    ADD COLUMN academic_year_id BIGINT,
    ADD CONSTRAINT fk_expense_academic_year
        FOREIGN KEY (academic_year_id) REFERENCES academic_years (id) ON DELETE SET NULL;

CREATE INDEX idx_expenses_academic_year_id ON expenses (academic_year_id);
