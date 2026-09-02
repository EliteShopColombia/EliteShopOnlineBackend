-- Pre-create PostgreSQL extensions required by Liquibase migrations.
-- Migration 017 uses uuid_generate_v4() but the extension was originally
-- created in migration 021, causing a fresh-database startup failure.
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
