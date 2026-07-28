--liquibase formatted sql

--changeset bob-pharma:002-add-pg-trgm-search-index
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS idx_drug_name_trgm
ON drug
USING GIN (name gin_trgm_ops);
