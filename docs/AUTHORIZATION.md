# Authorization

For each use case: authenticate the principal; resolve resource and scope; require the atomic permission; verify ownership or contextual policy; verify entitlement; enforce quota; record high-impact audit events. Missing evidence denies access. Grants bind a principal, permission, scope type, and scope ID. Roles only expand to grant presets. Controllers may reject early, but application services enforce meaningful decisions.

Lesson application services call `LessonAuthorizationGateway` for `lesson.create`, `lesson.read`, `lesson.edit`, and `lesson.publish`. Existing lessons use lesson scope and verify ownership. Production HTTP security denies protected routes unless authentication is configured; the current owner gateway is replaceable by grant resolution.

The `dev` and `test` profiles replace principal and permission resolution with request-scoped adapters. `X-Principal-Id` defaults to a fixed development UUID. `X-Lesson-Permissions` defaults to all four lesson permissions but can explicitly restrict tests. These adapters are profile-isolated and must never be activated in production.