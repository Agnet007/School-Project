# Deployment

The repository is `https://github.com/Agnet007/School-Project`. GitHub Pages publishes the frontend at `https://agnet007.github.io/School-Project/` with Vite base `/School-Project/`. Local development uses base `/`, `VITE_RUNTIME_MODE=LOCAL_DEVELOPMENT`, `VITE_API_BASE_URL=/api`, and the Vite proxy to `http://127.0.0.1:8080`.

Pages uses `GITHUB_PAGES_DEMO` with an empty API URL. It renders a read-only lesson preview and disables save and publish actions when no backend is available. Do not configure localhost as a public backend: it refers to the visitor's device. A future HTTPS backend may be supplied through the public `VITE_API_BASE_URL` repository variable; it must contain no secrets.

Set **Settings -> Pages -> Build and deployment -> Source -> GitHub Actions**. The workflow runs lint, typecheck, tests, build, uploads `frontend/dist`, and deploys through the `github-pages` environment. It does not use a `gh-pages` branch.

For a local artifact check, run `npm ci`, `npm run build`, and `npm run verify:pages`. If Actions reports a Pages configuration error, select GitHub Actions as the Pages source and rerun the workflow. Workflow failures should be diagnosed from the failed job step before changing build paths or environment variables.

The backend runs separately on a Java 21 host with PostgreSQL, environment-injected credentials, `/actuator/health`, Flyway on startup, TLS termination, and scheduled verified backups. Object storage is deferred until assets require it. Restore drills and database point-in-time recovery are production prerequisites.