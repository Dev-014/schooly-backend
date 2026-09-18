-- Add class_id to fee_structure table
ALTER TABLE fee_structure ADD COLUMN class_id BIGINT;
ALTER TABLE fee_structure ADD CONSTRAINT fk_fee_structure_class FOREIGN KEY (class_id) REFERENCES class (id) ON DELETE SET NULL;

-- Create an index for faster lookups
CREATE INDEX idx_fee_structure_class_id ON fee_structure(class_id);
