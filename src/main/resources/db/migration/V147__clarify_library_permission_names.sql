-- ============================================================================
-- V147__clarify_library_permission_names.sql
-- Clarify names and descriptions for library.book.view and library.book_list.view
-- ============================================================================

UPDATE permission_definitions
SET name = 'Personal Issued Books & Library View',
    description = 'Allows viewing personal issued books, loan history, and the school library book list'
WHERE permission_key = 'library.book.view';

UPDATE permission_definitions
SET name = 'Administrative Book Catalog / Inventory',
    description = 'Allows staff and librarians to view and manage school library book stock, accession numbers, and inventory'
WHERE permission_key = 'library.book_list.view';
