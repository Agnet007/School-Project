# ADR 0004: Versioned documents

Status: Accepted.

Persisted documents carry `documentType`, `schemaVersion`, `entityId`, and `revision`. Migrations run sequentially, preserve original data until validation succeeds, and commit atomically where possible. Published lesson versions are immutable snapshots.