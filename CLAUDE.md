# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/claude-code) when working with code in this repository.

## Project Overview

TopLeader is a Spring Boot backend application for a coaching/mentoring platform. It connects coaches with users (coachees), manages scheduling, feedback, and credits.

## Tech Stack

- **Java 25** with Spring Boot 4.0.1
- **PostgreSQL** with Flyway migrations
- **Spring Data JPA** (Hibernate 7.x)
- **Spring Security** (form login + session-based auth)
- **Lombok** for boilerplate reduction
- **Maven** build system

## Build & Run Commands

```bash
# Compile (requires Java 25)
JAVA_HOME=~/.sdkman/candidates/java/25 mvn clean compile

# Run tests
JAVA_HOME=~/.sdkman/candidates/java/25 mvn test

# Run specific test class
JAVA_HOME=~/.sdkman/candidates/java/25 mvn test -Dtest=ClassName

# Build without tests
JAVA_HOME=~/.sdkman/candidates/java/25 mvn clean package -DskipTests
```

## Project Structure

```
src/main/java/com/topleader/topleader/
├── admin/                  # Admin views and management
├── coach/                  # Coach profiles, availability
├── common/                 # Shared utilities and cross-cutting concerns
│   ├── ai/                 # AI integrations (AiClient, AiPromptService)
│   ├── calendar/           # Calendar integrations (Google, Calendly)
│   ├── email/              # Email sending (EmailService, templates)
│   ├── exception/          # Custom exceptions and error handling
│   │   ├── NotFoundException.java
│   │   ├── ApiValidationException.java
│   │   ├── ErrorCodeConstants.java
│   │   ├── ErrorController.java
│   │   └── JsonConversionException.java
│   ├── notification/       # Notification system
│   ├── password/           # Password management
│   └── util/               # Utilities, converters, common helpers
├── company/                # Company entity
├── configuration/          # Security, async configs
├── credit/                 # Credit management
├── feedback/               # Feedback forms
├── feedback_notification/  # Feedback notification handling
├── history/                # History tracking
├── hr/                     # HR user management
├── ical/                   # iCal format handling
├── message/                # User messaging
├── myteam/                 # Team management
├── report/                 # Reports
├── scheduled_session/      # Scheduled sessions
├── session/                # Session management
└── user/                   # Core user entity and features
```

## Key Patterns

### Entity Pattern
- Use `@Entity` with Lombok (`@Data`, , `@Accessors(chain = true)`)
- ID generation: `GenerationType.SEQUENCE` for Long, `GenerationType.UUID` for UUID
- Audit fields: `createdAt`, `createdBy`, `updatedAt`, `updatedBy` with `@PrePersist`/`@PreUpdate`

### Repository Pattern
- Extend `JpaRepository<T, ID>`
- Add `JpaSpecificationExecutor<T>` for complex queries

### Controller Pattern
- Use `@RestController` with `/api/latest/...` paths
- RBAC via `@Secured({"HR", "ADMIN"})` annotations
- Get current user via `@AuthenticationPrincipal UserDetails user`

### RBAC Roles
- `RESPONDENT`, `USER`, `MANAGER`, `COACH`, `HR`, `ADMIN`

### Exception Handling
All exception classes are in `common/exception/`:
- `NotFoundException` - no-arg constructor, returns 404
- `ApiValidationException` - for validation errors with error codes and field-level details
- `ErrorCodeConstants` - centralized error code constants
- `ErrorController` - global exception handler (@ControllerAdvice)
- `JsonConversionException` - JSON parsing/serialization errors

## Database Migrations

Flyway migrations in `src/main/resources/db/migration/0.0.1/`

Naming: `V0.0.1.{number}__{description}.sql`

Current latest: `V0.0.1.81__coaching_package.sql`

## Testing

- Uses Spring Boot Test with `@SpringBootTest`
- TestContainers for PostgreSQL
- Test data via SQL scripts in `src/test/resources/sql/`

## Deployment

### CI/CD Triggers

| Event | Action |
|-------|--------|
| PR to `develop`/`main` | Build + tests only |
| Tag `qa-deploy` | Deploy to QA environment |
| Tag `release-v*.*.*` | Deploy to PROD environment |

### Deploy Commands (Makefile)

```bash
# Deploy to QA (recreates qa-deploy tag)
make deploy-qa

# Deploy to PROD (auto-increments version, creates release-v*.*.* tag)
make deploy-prod
```

### Version Tagging

- QA: Uses movable `qa-deploy` tag (deleted and recreated each time)
- PROD: Uses semantic versioning `release-v{major}.{minor}.{patch}` (same pattern as frontend)
- `make deploy-prod` automatically increments patch version from latest tag

## Code Style

- Use `var` for local variables
- Records for DTOs
- Chain setters with `@Accessors(chain = true)`
- Validation with Jakarta `@NotNull`, `@Valid`, `@Min`
- **No JavaDoc comments above methods** - keep code clean without method documentation
- **Prefer streams over for/while loops** - use functional style with `stream()`, `map()`, `filter()`, `collect()`, etc.
