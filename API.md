# API

[shared/openapi/openapi.yaml](shared/openapi/openapi.yaml) is the machine-readable source of truth. Public endpoints use `/api/v1`, UUID identifiers, ISO 8601 timestamps, JSON, and Problem Details errors with stable error codes and correlation IDs.

Authentication strategy is deferred; every protected use case is backend-authorized against permission, scope, ownership, entitlement, and quota. Frontend checks are advisory only.

Mutations use `Idempotency-Key`; revision-bearing resources use explicit `revision` fields and `If-Match` or ETags. Collections use opaque cursor pagination with `limit` and `nextCursor`. Validation failures identify rejected fields without leaking sensitive values. Sync endpoints accept ordered operations and report applied, conflict, retryable, or permanent outcomes independently.

Endpoint groups will cover identity, lessons, classrooms, assignments, submissions, workspaces, synchronization, assets, and the built-in simulation catalog as their delivery phases land.

## Lesson endpoints

Phase 1 implements draft create/list/get, metadata patch, block add/update/reorder/duplicate/delete, publication, and published version list/get under `/api/v1/lessons`. Every draft mutation requires `If-Match` containing the quoted or unquoted numeric revision. Success returns the new revision as an ETag where applicable. A stale revision returns `409` with `LESSON_REVISION_CONFLICT` and `currentRevision`.

Published versions expose GET only. Block mutation payloads identify an uppercase `blockType` and its matching strict configuration. Unknown configuration fields, unsupported types, unsafe rich text, and invalid URLs return `400`. Development profile requests use isolated `X-Principal-Id` and `X-Lesson-Permissions` adapters; these headers are not production authentication.