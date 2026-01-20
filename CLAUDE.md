# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/claude-code) when working with code in this repository.

## Project Overview

TopLeader is a Spring Boot backend application for a coaching/mentoring platform. It connects coaches with users (coachees), manages scheduling, feedback, and credits.

## Agent Usage Preferences

**IMPORTANT:** Always use specialized agents for tasks that match their capabilities. Default to running agents in background when possible.

### Automatic Agent Selection Rules

- **Code Exploration** - ALWAYS use **Explore agent** for:
  - Questions starting with: "find", "where is", "how does", "show me", "search for"
  - Analyzing codebase structure or patterns
  - Understanding how features work
  - Example: "find all REST endpoints", "how does authentication work?"

- **Implementation Tasks** - ALWAYS use **Plan agent first**, then implement:
  - Adding new features or functionality
  - Refactoring existing code
  - Architectural changes
  - Example: "implement user logout", "add new API endpoint"

- **Complex Analysis** - Use **general-purpose agent** for:
  - Multi-step research and analysis tasks
  - Native image readiness analysis
  - Dependency analysis
  - Performance optimization analysis

### Background Execution

**Run agents in background (`run_in_background: true`) when:**
- User indicates they have another question ("mám další otázku", "meanwhile", "in the background")
- User explicitly mentions "agent" or "na pozadí" at end of request
- Task is exploratory/analytical and doesn't block user's next question
- Analysis will take multiple steps but user doesn't need immediate results

**Example triggers for background execution:**
- "analyzuj XYZ, mám další otázku"
- "najdi všechny XYZ agent"
- "prozkoumej XYZ na pozadí"

### Agent Keywords (Czech/English)

| Czech Keywords | English Keywords | Agent Type |
|----------------|------------------|------------|
| najdi, kde je, jak funguje | find, where is, how does | Explore |
| implementuj, přidej, refaktoruj | implement, add, refactor | Plan |
| analyzuj, prozkoumej, vyhodnoť | analyze, explore, evaluate | General-purpose |
| na pozadí, agent | in background, agent | run_in_background=true |

## Tech Stack

- **Java 25** with Spring Boot 4.0.1
- **PostgreSQL** with Flyway migrations
- **Spring Data JDBC** (fully migrated from Hibernate/JPA for native image compatibility)
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

### Entity Pattern (JDBC)
- Use `@Table(name = "table_name")` with Lombok (`@Data`, `@Accessors(chain = true)`)
- Use Spring Data annotations: `@Id`, `@Transient`
- For entities with manual ID control, implement `Persistable<ID>`
- Store enums as VARCHAR strings, JSON data as JSONB
- Use custom converters in `JdbcConfiguration` for complex types

### Repository Pattern (JDBC)
- Extend `ListCrudRepository<T, ID>` for basic CRUD only
- Add `PagingAndSortingRepository<T, ID>` only when pagination is actually needed
- **Prefer `@Query` over derived query methods** - avoids need for converters and gives full SQL control
- Use `@Modifying` for INSERT/UPDATE/DELETE operations
- **Pass enums as strings** using `.name()` to avoid converter complexity:
  ```java
  // Repository
  @Query("SELECT * FROM table WHERE type = :type")
  List<Entity> findByType(String type);

  // Usage
  repository.findByType(MyEnum.VALUE.name());
  ```

### Nullable Parameters in @Query (JDBC)
Spring Data JDBC does **NOT** support `:param IS NULL` syntax like JPA. Use these PostgreSQL techniques instead:

**For String parameters** - use COALESCE:
```sql
@Query("""
    SELECT * FROM my_table
    WHERE (COALESCE(:name, '') = '' OR name = :name)
    """)
List<MyEntity> findFiltered(String name);
```

**For DateTime parameters** - use CAST:
```sql
@Query("""
    SELECT * FROM my_table
    WHERE (CAST(:fromDate AS timestamp) IS NULL OR date >= :fromDate)
    AND (CAST(:toDate AS timestamp) IS NULL OR date <= :toDate)
    """)
List<MyEntity> findFiltered(LocalDateTime fromDate, LocalDateTime toDate);
```

**For nullable columns with LEFT JOIN** - handle NULL in column:
```sql
WHERE (CAST(:fromDate AS timestamp) IS NULL OR date IS NULL OR date >= :fromDate)
```

**DO NOT use** (doesn't work in JDBC):
```sql
WHERE (:param IS NULL OR column = :param)  -- This fails!
```

### Pagination with @Query (JDBC)
Spring Data JDBC @Query does **NOT** support `countQuery` attribute like JPA. Use separate methods:

```java
@Query("""
    SELECT * FROM my_table
    WHERE (COALESCE(:name, '') = '' OR name = :name)
    """)
List<MyEntity> findFiltered(String name, Pageable pageable);  // Adds LIMIT/OFFSET automatically

@Query("""
    SELECT COUNT(*) FROM my_table
    WHERE (COALESCE(:name, '') = '' OR name = :name)
    """)
long countFiltered(String name);
```

In controller, combine both to create Page:
```java
var content = repository.findFiltered(name, pageable).stream().map(Entity::toDto).toList();
var total = repository.countFiltered(name);
return new PageImpl<>(content, pageable, total);
```

### Transaction Management
- **AVOID `@Transactional` in most cases** - Spring Data JDBC handles transactions automatically per operation
- **Only use `@Transactional` when something fails** and you need:
  - Multiple write operations to be atomic (all succeed or all fail)
  - Explicit rollback behavior on exceptions
- **DO NOT** use `@Transactional` for:
  - Simple repository reads (findById, findAll, etc.)
  - Single repository write operations (save, delete)
  - Methods that only call one repository method
  - @Modifying @Query methods (they handle their own transaction)
- When migrating from JPA, **remove** `@Transactional` unless tests fail without it
- Spring Data JDBC repositories handle transactions per operation automatically

### Controller Pattern
- Use `@RestController` with `/api/latest/...` paths
- RBAC via `@Secured({"HR", "ADMIN"})` annotations
- Get current user via `@AuthenticationPrincipal UserDetails user`

### RBAC Roles
- `RESPONDENT`, `USER`, `MANAGER`, `COACH`, `HR`, `ADMIN`

### JDBC Custom Converters
All converters are in `configuration/JdbcConfiguration`:
- Use `@ReadingConverter` for database → Java conversions
- Use `@WritingConverter` for Java → database conversions
- **Always use Optional for null-safe conversions**:
  ```java
  @ReadingConverter
  static class MyConverter implements Converter<String, MyType> {
      public MyType convert(String source) {
          return Optional.ofNullable(source)
                  .filter(StringUtils::isNotBlank)
                  .map(MyType::valueOf)
                  .orElse(null);
      }
  }
  ```
- Register converters in `jdbcCustomConversions()` method
- Converters handle: Enums, JSONB types, custom objects

### Exception Handling
All exception classes are in `common/exception/`:
- `NotFoundException` - no-arg constructor, returns 404
- `ApiValidationException` - for validation errors with error codes and field-level details
- `ErrorCodeConstants` - centralized error code constants
- `ErrorController` - global exception handler (@ControllerAdvice)
- `JsonConversionException` - JSON parsing/serialization errors

## JDBC Migration - Completed ✅

### Why Spring Data JDBC?
This project has **fully migrated from Hibernate/JPA to Spring Data JDBC** for native image compatibility.

**Reasons for choosing Spring Data JDBC over jOOQ:**
- Simpler than jOOQ - no DSL complexity
- No code generation overhead
- Fewer dependencies and build steps
- Better Spring Boot integration

**Benefits over Hibernate/JPA:**
- Minimal reflection usage (native image ready)
- No lazy loading proxies or runtime bytecode generation
- Direct row-to-object mapping
- Full SQL control with `@Query`
- Smaller native image footprint

### Migration Status
**ALL modules fully migrated** to Spring Data JDBC:
- **message** - User messaging system (Message, UserChat, LastMessage)
- **myteam** - Team management views (MyTeamView)
- **session** - All session-related entities:
  - CoachSessionView - View for coach sessions
  - CoachingPackage - Coaching package management
  - ReportSessionView - Session reporting view
  - ScheduledSession - Scheduled coaching sessions
  - UserAllocation - User credit allocation

### Migration Steps
When migrating an entity from JPA to JDBC:

1. **Update Entity Annotations**:
   - Remove: `@Entity`, `@GeneratedValue`, `@SequenceGenerator`, `@Column`
   - Add: `@Table(name = "table_name")` from `org.springframework.data.relational.core.mapping`
   - Change: `@Id` to `org.springframework.data.annotation.Id`

2. **Update Repository**:
   - Change from: `JpaRepository<T, ID>`
   - To: `ListCrudRepository<T, ID>, PagingAndSortingRepository<T, ID>`
   - **IMPORTANT**: Always use `ListCrudRepository` instead of `CrudRepository` to return `List` instead of `Iterable`
   - All repository methods should return `List<T>` for consistency and ease of use in tests
   - Use `@Query` from `org.springframework.data.jdbc.repository.query` where enum are query to avoid converters else use named query
   - Convert JPQL to native SQL

3. **Update Service Layer**:
   - Change: `jakarta.transaction.Transactional` → `org.springframework.transaction.annotation.Transactional`
   - Remove JPA `Example` API usage
   - **@PrePersist/@PreUpdate replacement**: Manually set `createdAt`, `updatedAt` timestamps in service methods using `LocalDateTime.now()`
   - **NEVER use fully qualified class names** - always import classes and use simple names (e.g., `LocalDateTime.now()`, not `java.time.LocalDateTime.now()`)

4. **Handle Complex Types**:
   - JSON arrays/objects: Store as String (JSONB), add getter methods for deserialization
   - Enums: Convert via custom converters in `JdbcConfiguration`
   - Custom types: Add `@ReadingConverter` and `@WritingConverter`

5. **Update Tests**:
   - Replace JPA `Example` API with stream filtering
   - Add `@Transactional` to tests accessing lazy-loaded data

## Database Migrations

Flyway migrations in `src/main/resources/db/migration/`

Current directories:
- `0.0.1/` - Initial migrations (legacy)
- `1.0.0/` - JPA to JDBC migration scripts

Naming: `V{version}.{number}__{description}.sql`

Example: `V1.0.0.2__jpa_remove.sql`

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
- **Use Optional for null-safe operations** - avoid ternary operators and if-null checks:
  ```java
  // Good - using Optional
  return Optional.ofNullable(source)
          .filter(StringUtils::isNotBlank)
          .map(MyType::valueOf)
          .orElse(defaultValue);

  // Bad - ternary operator
  var value = source != null ? source.getValue() : null;
  if (StringUtils.isBlank(value)) return defaultValue;
  ```
