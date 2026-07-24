CREATE TABLE lesson (
    lesson_id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_lesson_owner ON lesson(owner_id);

CREATE TABLE lesson_draft (
    draft_id UUID PRIMARY KEY,
    lesson_id UUID NOT NULL UNIQUE REFERENCES lesson(lesson_id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL CHECK (length(trim(title)) > 0),
    description VARCHAR(4000) NOT NULL DEFAULT '',
    revision BIGINT NOT NULL CHECK (revision >= 0),
    blocks_json JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_lesson_draft_updated ON lesson_draft(updated_at DESC);

CREATE TABLE lesson_version (
    lesson_id UUID NOT NULL REFERENCES lesson(lesson_id) ON DELETE RESTRICT,
    version INTEGER NOT NULL CHECK (version > 0),
    source_revision BIGINT NOT NULL CHECK (source_revision >= 0),
    title VARCHAR(200) NOT NULL,
    description VARCHAR(4000) NOT NULL DEFAULT '',
    snapshot_json JSONB NOT NULL,
    published_at TIMESTAMP WITH TIME ZONE NOT NULL,
    published_by UUID NOT NULL,
    PRIMARY KEY (lesson_id, version)
);

CREATE INDEX idx_lesson_version_published ON lesson_version(lesson_id, published_at DESC);