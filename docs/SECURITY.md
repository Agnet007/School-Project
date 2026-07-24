# Security and threat model

Untrusted inputs include all client payloads, imported lessons, rich text, plugin configuration, uploads, and sync operations. Controls are deny-by-default backend authorization, contextual ownership checks, Bean Validation, output encoding, allowlisted rich text, CSRF protection for cookie sessions, idempotency, optimistic concurrency, upload limits, audit events, dependency scanning, and secrets outside frontend bundles/logs.

Authentication provider, session mechanism, and rate limiter are deferred but must implement narrow ports. Offline assessment cannot be cheat-proof. Plugin hosts expose least privilege. Backups and conflict preservation mitigate accidental data loss; authorization and schema validation mitigate tampering.

Lesson payloads cap metadata, collections, rich spans, chart datasets/points, and request/upload sizes. Raw HTML is neither accepted nor rendered. Image schemes are allowlisted. Jackson rejects unknown configuration fields. DTO construction prevents mass assignment. Published versions have no mutation route or repository update operation. `If-Match`, aggregate revision checks, and JPA version predicates prevent silent stale overwrites. Problem responses contain correlation IDs and stable codes without payload logging.

Development identity headers are enabled only by `dev`/`test` profiles. Production remains deny-by-default until a real authentication and grant adapter is configured.