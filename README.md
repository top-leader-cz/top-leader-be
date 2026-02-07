# TopLeader Backend

Backend application for the TopLeader platform - a coaching and mentoring platform that connects coaches with users, manages scheduling, feedback, credits, and team collaboration.

## Why TopLeader?

TopLeader is a platform that helps organizations develop their people through professional coaching. It connects employees with certified coaches, manages coaching sessions, tracks progress, and provides insights through feedback and AI-powered analysis.

## Tech Stack

- **Java 25** with Spring Boot 4.0.2
- **PostgreSQL 15** database
- **Spring Data JDBC** for lightweight data access
- **Spring Security** (session-based authentication)
- **Spring AI** with OpenAI integration (function calling for coaching tools)
- **Spring Modulith** for modular monolith architecture
- **Spring Session JDBC** for distributed sessions
- **Flyway** for database migrations
- **Jetty** (lightweight alternative to Tomcat)
- **Log4j2** for structured logging
- **Lombok** for boilerplate reduction
- **Gradle** (Kotlin DSL) for build management
- **Docker Compose** for local development
- **Virtual Threads** enabled
- **OpenTelemetry** for observability

## Architecture Decisions

> **Key ADRs:** [Monolith Architecture](docs/adr/001-monolith-architecture.md) | [Session-based Auth](docs/adr/002-session-based-auth.md) | [Native Image Strategy](docs/adr/003-native-image-strategy.md)

### Why Java 25?

- **Compact Object Headers** (`-XX:+UseCompactObjectHeaders`) - ~10-15% memory reduction
- **Virtual Threads** - lightweight threads for I/O-bound operations (database, HTTP, email)
- **Enhanced Pattern Matching** - cleaner code with `switch` and `instanceof`
- **Better Native Image Support** - improved GraalVM compatibility with Spring Boot 4

### Why Spring Boot 4?

- **First-class GraalVM native image support** - most components work without reflection hints
- **Reduced reflection** - compile-time bean registration and DI optimization
- **Jakarta EE 11** - latest specifications
- **Native virtual threads integration** in the web layer

### Why Monolith?

> See [ADR-001](docs/adr/001-monolith-architecture.md)

Primarily for **cost savings**. Avoids the operational complexity of microservices while providing the best balance of simplicity and performance for a coaching platform with predictable load patterns.

### Why Spring Data JDBC?

- **Native image friendly** - minimal reflection, no runtime proxies
- **Simple model** - direct row-to-object mapping, no lazy loading
- **Full SQL control** - native SQL with `@Query`, no N+1 problems

### Why Jetty?

Smaller memory footprint and faster startup than Tomcat. Aligns with keeping the application lean.

### Why Log4j2?

Better performance (async, garbage-free logging), more flexible configuration, and better GraalVM native image support than Logback.

### Why Session-based Auth over JWT?

> See [ADR-002](docs/adr/002-session-based-auth.md)

- No token management complexity (refresh, rotation, blacklisting)
- Instant session invalidation on logout
- Already have a database - no additional infrastructure needed

### Why Pre-generated OpenAPI?

Dynamic spec generation uses reflection. We pre-generate `openapi.yaml` during CI/CD via `OpenApiGeneratorTest` for faster startup and native image compatibility.

### Why Gradle over Maven?

Faster builds (incremental, cached, parallel), Kotlin DSL with type-safe configuration and IDE support.

## GCP Infrastructure

We deploy on **Google Cloud Platform**:

**Backend:**
- **Cloud Run** (QA & Production) - containerized with Distroless images, auto-scaling to zero, pay-per-request
- **Cloud SQL** (PostgreSQL 15) - managed database with automatic backups

**Frontend:**
- **React + MUI** SPA on **Cloud Storage** + **Cloud CDN** behind **Global Load Balancer**

**Tooling:**
- **Terraform** - infrastructure as code for all GCP resources
- **Secret Manager** - secure storage for API keys and credentials
- **Cloud Scheduler** - cron jobs for background tasks
- **GitHub Actions** - CI/CD with tag-based deployments

**CI/CD Flow:**
- PRs to `develop`/`main` → build + tests only
- `qa-deploy` tag → Docker image → Cloud Run QA
- `release-v*.*.*` tag → Docker image → Cloud Run Production

```
┌─────────────────────────────────────────────────────────────────────┐
│                        Global Load Balancer                         │
│                     (SSL termination, routing)                       │
└─────────────────────────────────┬───────────────────────────────────┘
                                  │
                ┌─────────────────┴─────────────────┐
                │                                   │
                ▼                                   ▼
      ┌─────────────────┐                 ┌─────────────────┐
      │  Cloud Storage  │                 │   Cloud Run     │
      │    + CDN        │                 │   (Backend)     │
      │   (Frontend)    │                 │                 │
      │                 │                 │  /api/*         │
      │  /*.html, *.js  │                 │                 │
      └─────────────────┘                 └────────┬────────┘
                                                   │
                                                   ▼
                                          ┌───────────────┐
                                          │   Cloud SQL   │
                                          │ (PostgreSQL)  │
                                          └───────────────┘
```

### Native Image Strategy

> See [ADR-003](docs/adr/003-native-image-strategy.md)

The codebase is native-image ready (minimal reflection, no heavy ORM). Native image + Cloud Run = instant cold starts and minimal costs.

## Getting Started

### Prerequisites

- **Java 25** (managed via SDKMAN recommended)
- **Gradle** (managed via SDKMAN recommended)
- **Docker & Docker Compose** (for PostgreSQL)

### 1. Clone and Start Database

```bash
git clone <repository-url>
cd top-leader-be
docker-compose up -d
```

This starts PostgreSQL 15 on port `5434` (database: `top_leader`, user: `root`, password: `postgres`).

### 2. Configure Environment

The defaults in `application.yml` work for local development. For external services, configure:
- **OpenAI API Key** (AI features)
- **Google OAuth** credentials (calendar integration)
- **Calendly** credentials (scheduling)
- **GCP Storage** credentials (file storage)

### 3. Run

```bash
make build    # Build the application
make test     # Run tests
./gradlew bootRun  # Start on http://localhost:8080
```

Flyway migrations run automatically on startup.

**Swagger UI:** `http://localhost:8080/swagger-ui.html`

## Project Structure

```
src/main/java/com/topleader/topleader/
├── admin/                  # Admin views and management
├── coach/                  # Coach profiles and availability
├── common/                 # Shared utilities and cross-cutting concerns
│   ├── ai/                 # AI integrations (OpenAI, function calling tools)
│   ├── calendar/           # Calendar integrations (Google, Calendly)
│   ├── email/              # Email sending (SMTP)
│   ├── exception/          # Custom exceptions and error handling
│   ├── notification/       # Notification system
│   ├── password/           # Password management
│   └── util/               # Utilities and converters
├── configuration/          # Security, async, and app configs
├── credit/                 # Credit system management
├── feedback/               # Feedback forms and responses
├── history/                # History tracking
├── hr/                     # HR user management
├── message/                # User messaging system
├── myteam/                 # Team management
├── session/                # Session management
└── user/                   # Core user entity and features
```

## Security

See [docs/SECURITY.md](docs/SECURITY.md) for the complete security audit report.

- **Two filter chains:** HTTP Basic for internal jobs (`/api/protected/**`), form login for the app
- **Session-based auth** with Spring Session JDBC (15min timeout, `SameSite=lax`, `HttpOnly`, `Secure`)
- **RBAC:** `RESPONDENT`, `USER`, `MANAGER`, `COACH`, `HR`, `ADMIN` via `@Secured`
- **CORS disabled** in app - handled by Cloud Load Balancer (same-domain routing)
- **Security headers:** CSP, X-XSS-Protection, X-Content-Type-Options, X-Frame-Options
- **Input validation:** Jakarta validation, file upload magic byte checks, image re-encoding

## Testing

```bash
make test             # Run all tests
make test-coverage    # Run tests with coverage report
./gradlew test --tests "ClassName"  # Run specific test
```

The test suite uses:
- **Testcontainers** for PostgreSQL (not H2)
- **GreenMail** for email testing
- **WireMock** for external API mocking
- **JSON Unit** for JSON assertions

Test data is loaded from `src/test/resources/sql/` scripts.

## Database Migrations

Flyway migrations are in `src/main/resources/db/migration/1.0.0/`. We consolidated **80+ individual migrations** into a single `V1.0.0.1__init.sql` for faster startup.

## Monitoring

- **OpenTelemetry** for traces and metrics
- **Grafana Cloud** support (configured via environment)
- **Actuator** endpoints for health, metrics, and Prometheus

## Code Style

- Use `var` for local variables
- Records for DTOs
- Lombok `@Data`, `@Accessors(chain = true)`
- `@Query` with native SQL (prefer over derived queries)
- `/api/latest/*` controller paths with `@Secured`
- Streams over loops, `Optional` for null safety
- No JavaDoc above methods
