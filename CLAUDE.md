# CLAUDE.md

This file provides guidance to Claude Code when working with code in this repository.

## Project Overview

TopLeader is a Spring Boot backend application for a coaching/mentoring platform.

## Tech Stack

- **Java 25** with Spring Boot 4.0.2
- **PostgreSQL** with Flyway migrations
- **Spring Data JDBC** (fully migrated from JPA)
- **Spring Security** (form login + session-based auth)
- **Spring Modulith 2.0.2** (modular monolith architecture)
- **Lombok** for boilerplate reduction
- **Gradle** build system

## Project Structure

```
src/main/java/com/topleader/topleader/
├── common/                 # Shared kernel - used by all modules
│   ├── calendar/           # Calendar integrations (Google, Calendly)
│   ├── email/              # Email sending and templates
│   ├── exception/          # Custom exceptions
│   └── util/               # Utilities, converters
├── coach/                  # Coach profiles, availability
├── session/                # Session management
├── user/                   # User management
├── hr/                     # HR functionalities
└── ...                     # Other business modules
```

## Spring Modulith - Module Rules

1. **No cyclic dependencies** between modules
2. **`common` is a shared kernel** - all modules can use it
3. **`common` must NOT depend on business modules** (coach, user, session, etc.)
4. **Use DTOs to avoid cross-module dependencies** - when common needs data from other modules, create DTOs in common

Example: `SessionEmailData` DTO in `common/email/` instead of using `ScheduledSession` from session module.

## Key Patterns for Code Generation

### Entity Pattern (JDBC)
```java
@Data
@Table("table_name")
@Accessors(chain = true)
public class MyEntity {
    @Id
    private Long id;
    private String name;
    // No @Column, @GeneratedValue, or JPA annotations
}
```

### Repository Pattern (JDBC)
```java
public interface MyRepository extends ListCrudRepository<MyEntity, Long> {

    // Prefer @Query with native SQL
    @Query("SELECT * FROM table WHERE type = :type")
    List<MyEntity> findByType(String type);

    // Pass enums as strings
    default List<MyEntity> findByType(MyEnum type) {
        return findByType(type.name());
    }

    // Modifying queries
    @Modifying
    @Query("DELETE FROM table WHERE id = :id")
    void deleteById(Long id);
}
```

### Nullable Parameters in @Query
```java
// String parameters - use COALESCE
@Query("""
    SELECT * FROM table
    WHERE (COALESCE(:name, '') = '' OR name = :name)
    """)
List<MyEntity> findFiltered(String name);

// DateTime parameters - use CAST
@Query("""
    SELECT * FROM table
    WHERE (CAST(:fromDate AS timestamp) IS NULL OR date >= :fromDate)
    """)
List<MyEntity> findFiltered(LocalDateTime fromDate);

// DO NOT use (:param IS NULL OR column = :param) - doesn't work in JDBC!
```

### Pagination with @Query
```java
// Query method with Pageable
@Query("SELECT * FROM table WHERE active = true")
List<MyEntity> findActive(Pageable pageable);

// Separate count method
@Query("SELECT COUNT(*) FROM table WHERE active = true")
long countActive();

// In controller
var content = repository.findActive(pageable);
var total = repository.countActive();
return new PageImpl<>(content, pageable, total);
```

### Transaction Management
- **AVOID `@Transactional` in most cases** - JDBC handles it automatically
- **Only use `@Transactional` for multi-operation atomicity**
- Never for single repository calls or read-only methods

### Controller Pattern
```java
@RestController
@RequestMapping("/api/latest/my-resource")
@Secured({"ROLE_USER"})
public class MyController {

    @GetMapping
    public List<MyDto> getAll(@AuthenticationPrincipal UserDetails user) {
        return service.findAll(user.getUsername());
    }
}
```

### Exception Handling
```java
// Use existing exceptions from common/exception/
throw new NotFoundException();
throw new ApiValidationException(ERROR_CODE, "field", "value", "message");
```

### JDBC Custom Converters
```java
// In JdbcConfiguration.java
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

## Code Style

- Use `var` for local variables
- Records for DTOs
- Chain setters with `@Accessors(chain = true)`
- **No JavaDoc comments above methods**
- **Prefer streams over for/while loops**
- **Use Optional for null-safe operations**:
  ```java
  // Good
  return Optional.ofNullable(source)
          .filter(StringUtils::isNotBlank)
          .map(MyType::valueOf)
          .orElse(defaultValue);

  // Bad
  var value = source != null ? source.getValue() : null;
  if (StringUtils.isBlank(value)) return defaultValue;
  ```
- **NEVER use fully qualified class names** - always import and use simple names

## Database Migrations

- Flyway migrations in `src/main/resources/db/migration/`
- Naming: `V{version}.{number}__{description}.sql`
- Example: `V1.0.0.2__add_user_table.sql`

## Development Environment

### Build System
- **Gradle** and **Java** are managed via **SDKMAN**
- Gradle location: `~/.sdkman/candidates/gradle/current/bin/gradle`
- Java location: `~/.sdkman/candidates/java/25`
- Use `make` commands for common tasks (see `Makefile` for available commands)

### Common Commands
```bash
make build          # Build the application
make test           # Run tests
make test-coverage  # Run tests with coverage report
make native         # Build GraalVM native image
```

### Testing
- Tests use **PostgreSQL TestContainers** (not H2)
- TestContainers configuration in `EnablePostgresTestContainerContextCustomizerFactory`
- Test-specific datasource configuration in `application-test.yml` ensures Agroal works with TestContainers
