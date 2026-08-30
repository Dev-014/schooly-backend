-- V71__add_collection_plan_to_fee_structure.sql

ALTER TABLE fee_structure ADD COLUMN collection_plan_id BIGINT REFERENCES collection_plan(id) ON DELETE SET NULL;
