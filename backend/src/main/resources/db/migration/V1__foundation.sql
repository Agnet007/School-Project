CREATE TABLE platform_schema_metadata (
    document_type VARCHAR(100) PRIMARY KEY,
    current_schema_version INTEGER NOT NULL CHECK (current_schema_version > 0),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);