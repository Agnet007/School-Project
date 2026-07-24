# Offline Learning Platform

Monorepo for an offline-first educational platform. Phase 0 establishes a React/Vite frontend, a Java 21/Spring Boot backend, shared contracts, and CI.

## Local checks

```sh
npm ci
npm run lint
npm run typecheck
npm test
npm run build
cd backend && mvn verify
```

Backend development requires Java 21, Maven 3.9+, and PostgreSQL. Configuration uses `DATABASE_URL`, `DATABASE_USER`, and `DATABASE_PASSWORD`. No production secret belongs in frontend environment variables.

See [ARCHITECTURE.md](ARCHITECTURE.md), [API.md](API.md), and [docs/ROADMAP.md](docs/ROADMAP.md).