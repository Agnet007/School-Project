# Data model

Stable UUIDs identify principals, lessons, drafts, immutable versions, blocks, assignments, attempts, workspaces, documents, sync operations, conflicts, and events. Mutable aggregates carry revisions for optimistic concurrency. JSON documents carry document type and schema version. Relational entities and API DTOs remain separate. Deletions requiring synchronization use tombstones.

## Lesson core

`lesson` stores logical identity, owner, and creation time. `lesson_draft` stores one editable draft per lesson with title, description, revision, timestamps, and typed blocks as JSONB. `lesson_version` uses `(lesson_id, version)` as an immutable primary key and stores source revision, metadata, publication facts, and a complete block JSONB snapshot. Foreign keys prevent orphan drafts and versions; owner, update, and publication indexes support current queries.

Block IDs are stable UUIDs independent from positions. Positions are contiguous deterministic integers; reorder never changes IDs, duplication creates a new ID, and deletion only normalizes positions. JSONB is not exposed as an untyped domain map: one codec converts strict typed Java records at infrastructure boundaries.