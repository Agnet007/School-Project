# Offline Learning Platform

Monorepo for an offline-first educational platform. The static frontend is deployed at [School-Project on GitHub Pages](https://agnet007.github.io/School-Project/), while the Java 21/Spring Boot backend remains a separate service.

GitHub Pages builds use `/School-Project/` and `GITHUB_PAGES_DEMO`, so the public site works without a backend in read-only demo mode. Local development uses `/` with the Vite proxy at `http://127.0.0.1:8080`. A future HTTPS backend can be supplied with the public `VITE_API_BASE_URL` repository variable; localhost is never a public backend.

## Local checks

```sh
npm ci
npm run lint
npm run typecheck
npm test
npm run build
npm run verify:pages
cd backend && mvn verify
```

Pages deployment is configured in `.github/workflows/pages.yml`. Repository Settings -> Pages -> Build and deployment -> Source must be set to **GitHub Actions**. 

```

```

Backend development requires Java 21, Maven 3.9+, and PostgreSQL. Configuration uses `DATABASE_URL`, `DATABASE_USER`, and `DATABASE_PASSWORD`. No production secret belongs in frontend environment variables.

See [ARCHITECTURE.md](ARCHITECTURE.md), [API.md](API.md), and [docs/ROADMAP.md](docs/ROADMAP.md).