# Roadmap

## Phase 0 - Repository foundation

- [x] Frontend React, TypeScript, and Vite application compiles and tests pass.
- [x] Backend Java 21 and Spring Boot application compiles and tests pass.
- [x] Shared versioned JSON Schemas and OpenAPI contract validate.
- [x] Architecture, security, authorization, synchronization, and extension decisions are documented.
- [x] GitHub Actions validate frontend, backend, and shared contracts.
- [x] GitHub Pages deployment workflow builds with a repository-relative base path.
- [x] GitHub Pages deployment uses the official artifact workflow, `/School-Project/` base, and backend-free demo mode.

Acceptance: clean installs can run all documented checks; CI encodes the same checks; persisted contract examples carry independent schema versions.

## Delivery phases

- [x] Phase 1 - Lesson core: drafts, immutable publication, structured blocks, editor/player foundations.
- [ ] Phase 2 - Offline foundation: PWA shell, Cache Storage, IndexedDB, installed lesson playback.
- [ ] Phase 3 - Local workspaces: notes, graph board, contexts, migrations, local-only policy.
- [ ] Phase 4 - Synchronization: operation queue, idempotency, retries, optimistic concurrency, conflicts.
- [ ] Phase 5 - Assessment: checking contracts, drafts, submissions, grading foundation.
- [ ] Phase 6 - Generators: deterministic random source, chart generator, invariant tests.
- [ ] Phase 7 - Simulations: plugin contract, restricted host, registry, reference simulation, snapshots.
- [ ] Phase 8 - Hardening: authorization coverage, accessibility, performance, end-to-end deployment checks.

Later phases remain deferred until all preceding acceptance criteria pass. Full analytics, real-time collaboration, third-party runtime plugins, and plugin marketplace are outside the initial roadmap.

## Phase 1 - Lesson core

- [x] Domain model enforces draft revision, block identity/order, typed configuration, and immutable publication invariants.
- [x] Application use cases cover draft creation/query/editing, block operations, and publication behind repository and authorization ports.
- [x] PostgreSQL persistence uses a new Flyway migration and immutable JSONB publication snapshots.
- [x] REST API exposes versioned lesson endpoints, `If-Match` concurrency, validation, and Problem Details errors.
- [x] Development principal and authorization adapters are isolated from production behavior.
- [x] Frontend lesson list, creation, editor, preview, publication, and published viewer use a typed repository abstraction.
- [x] Typed block registry validates and renders every Phase 1 block; required functional renderers are implemented.
- [x] JSON Schemas, OpenAPI, and representative cross-platform fixtures validate.
- [x] Security, domain, application, API, persistence, and frontend tests cover Phase 1 acceptance scenarios.
- [x] Architecture and feature documentation reflect implemented behavior and known limitations.

Acceptance: publishing creates immutable monotonic snapshots; later draft edits do not alter prior versions; stale revisions and unauthorized mutations fail without overwriting data; all repository checks pass.