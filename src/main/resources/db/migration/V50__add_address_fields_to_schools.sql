-- V50__add_address_fields_to_schools.sql

ALTER TABLE schools 
ADD COLUMN city VARCHAR(255),
ADD COLUMN state VARCHAR(255),
ADD COLUMN pincode VARCHAR(50);
