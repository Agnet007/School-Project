# Testing

Frontend checks: ESLint, TypeScript, Vitest domain/component tests, IndexedDB and sync tests, then Playwright critical flows. Backend checks: domain/policy/use-case tests, MockMvc contracts, Flyway tests, idempotency/concurrency tests, and PostgreSQL Testcontainers integration. Shared fixtures validate JSON Schemas and OpenAPI. Test breadth grows with each roadmap phase.

Phase 1 backend tests cover aggregate invariants, typed JSON round trips, Flyway/JPA startup, complete publication flow, snapshot isolation, stale revisions, permission denial, unsafe rich text, unknown configuration fields, and absent version mutation routes. H2 PostgreSQL mode is the fast migration/repository test database; PostgreSQL Testcontainers remains deferred until container infrastructure is available.

Frontend Vitest covers registry completeness/capabilities, safe rich text and chart rendering, create/add/preview/publish interaction, published rendering, conflict state, validation, and HTTP `If-Match` behavior. CI compiles every schema, validates draft/version fixtures, asserts the invalid fixture fails, and lints OpenAPI.