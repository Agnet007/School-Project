# Synchronization

Each operation records `operationId`, entity type/ID, operation type, base revision, payload, client creation time, retry count, and status. Status moves through `PENDING`, `SYNCING`, then `APPLIED`, `CONFLICT`, `FAILED_RETRYABLE`, or `FAILED_PERMANENT`. Retries use bounded exponential backoff and the same idempotency key. Operations preserve per-entity order.

Published lesson versions never mutate. Note and workspace conflicts retain both versions. Progress merges only documented monotonic fields; none exist until their schema is implemented. Submitted attempts remain immutable; server receipt time and server evaluation are authoritative. Conflict records are recoverable and never silently discard local data.