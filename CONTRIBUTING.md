# Contributing

Use English for code and contracts. Keep feature packages independent and persisted formats versioned. Add a roadmap issue before introducing a `TODO` comment.

Run frontend lint, typecheck, tests, and build plus `mvn verify` before review. Database changes require Flyway migrations. Contract changes require compatible JSON Schema and OpenAPI updates. Never commit secrets, generated build output, or unsynchronized user-data deletion behavior.