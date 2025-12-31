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
├── ai/                     # AI integrations
├── calendar/               # Calendar integrations
├── coach/                  # Coach profiles, availability
├── company/                # Company entity
├── configuration/          # Security, async configs
├── credit/                 # Credit management
├── email/                  # Email sending
├── exception/              # Custom exceptions (NotFoundException, ApiValidationException)
├── feedback/               # Feedback forms
├── feedback_notification/  # Feedback notification handling
├── history/                # History tracking
├── hr/                     # HR user management
├── ical/                   # iCal format handling
├── message/                # User messaging
├── myteam/                 # Team management
├── notification/            # Notifications
├── password/               # Password management
├── report/                 # Reports
├── scheduled_session/      # Scheduled sessions
├── session/                # Session management
├── user/                   # Core user entity and features
└── util/                   # Utilities, converters
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
- `NotFoundException` - no-arg constructor, returns 404
- `ApiValidationException` - for validation errors with error codes

## Database Migrations

Flyway migrations in `src/main/resources/db/migration/0.0.1/`

Naming: `V0.0.1.{number}__{description}.sql`

Current latest: `V0.0.1.81__coaching_package.sql`

## Testing

- Uses Spring Boot Test with `@SpringBootTest`
- TestContainers for PostgreSQL
- Test data via SQL scripts in `src/test/resources/sql/`

## Code Style

- Use `var` for local variables
- Records for DTOs
- Chain setters with `@Accessors(chain = true)`
- Validation with Jakarta `@NotNull`, `@Valid`, `@Min`
- **No JavaDoc comments above methods** - keep code clean without method documentation
