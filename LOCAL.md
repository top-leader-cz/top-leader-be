# Local Development

## Prerequisites

- **Java 25** via SDKMAN: `sdk use java 25`
- **Gradle** via SDKMAN
- **Docker** (for PostgreSQL)

## Running the Application

### 1. Start PostgreSQL (clean database)

```bash
docker compose down -v db && docker compose up -d db
```

This removes the old container and volume (wipes all data), then starts a fresh PostgreSQL 15 with an empty `top_leader` database. Flyway will automatically create all tables on app startup.

### 2. Start the application

**IntelliJ:** Open `TopLeaderApplication.java` and click the green Run button next to `main()`.

**Command line:**
```bash
JAVA_HOME=~/.sdkman/candidates/java/25 ~/.sdkman/candidates/gradle/current/bin/gradle bootRun
```

### 3. App is available at `http://localhost:8080`

## Database Details

- **Host:** `localhost:5432`
- **Database:** `top_leader`
- **User:** `root`
- **Password:** `postgres`

No Spring profile is needed — the default `application.yml` connects to the local database.

## Connecting to Cloud SQL (QA/PROD)

```bash
make db-proxy
```

Starts Cloud SQL Auth Proxy so you can connect to the cloud database via `localhost:5432`.

## Common Commands

```bash
make build          # Build the application
make test           # Run tests (uses TestContainers, no local DB needed)
make test-coverage  # Run tests with coverage report
make deploy-qa      # Deploy to QA (builds + creates git tag)
make deploy-prod    # Deploy to PROD (builds + creates release tag)
```
