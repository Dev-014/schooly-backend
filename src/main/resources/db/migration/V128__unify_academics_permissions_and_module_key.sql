-- V128: Unify all academic permissions under the canonical ACADEMICS module_key

UPDATE permission_definitions 
SET module_key = 'ACADEMICS' 
WHERE module_key = 'ACADEMIC';
