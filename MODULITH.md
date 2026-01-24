# Spring Modulith Guide

Complete guide for using Spring Modulith in the TopLeader project.

## What is Spring Modulith?

Spring Modulith enforces **modular monolith architecture** - a well-structured monolith with clear module boundaries that could later be extracted into microservices if needed.

## Benefits

### 1. Architecture Validation
- Prevents unwanted dependencies between modules
- Detects cyclic dependencies automatically
- Enforces clean architecture rules

### 2. Automatic Documentation
- Generates module diagrams (PlantUML)
- Documents public APIs of each module
- Creates module canvas for each module

### 3. Event-Driven Communication
- Modules communicate via Domain Events
- Loose coupling between modules
- Easier to test and maintain

### 4. Future-Proof Architecture
- Clear module boundaries make extraction to microservices easier
- Prevents "big ball of mud" architecture
- Supports gradual refactoring

## Module Structure Best Practices

### Public API Pattern

```
module/
├── ModuleController.java     # Public API (REST endpoints)
├── ModuleService.java         # Public service interface
└── internal/                  # Implementation details (PRIVATE)
    ├── ModuleServiceImpl.java
    ├── ModuleRepository.java
    └── domain/
        └── ModuleEntity.java
```

**Rules:**
- Only classes in module root are public API
- `internal/` package is NEVER accessed from other modules
- Domain entities should be in `internal/domain/`

### Example: User Module

```
user/
├── UserController.java        # Public: REST API
├── User.java                  # Public: User DTO
├── UserService.java           # Public: Service interface
└── internal/
    ├── UserEntity.java        # Private: Database entity
    ├── UserRepository.java    # Private: Data access
    └── UserServiceImpl.java   # Private: Implementation
```

## Event-Driven Communication

Instead of direct dependencies between modules, use **Domain Events**.

### Publishing Events

```java
@Service
public class UserService {
    private final ApplicationEventPublisher events;

    public void createUser(User user) {
        userRepository.save(user);

        // Publish event instead of calling other modules directly
        events.publishEvent(new UserCreatedEvent(user.getId(), user.getUsername()));
    }
}
```

### Listening to Events

```java
@Service
class CreditService {

    @EventListener
    void onUserCreated(UserCreatedEvent event) {
        // Automatically create credit account for new user
        var account = new CreditAccount()
            .setUserId(event.userId())
            .setBalance(0);
        creditRepository.save(account);
    }
}
```

### Async Event Processing

```java
@Service
class EmailService {

    @Async
    @EventListener
    void onUserCreated(UserCreatedEvent event) {
        // Send welcome email asynchronously
        emailService.sendWelcomeEmail(event.userId());
    }
}
```

**Benefits:**
- ✅ No direct dependency between `user` and `credit` modules
- ✅ Easy to add new listeners without modifying existing code
- ✅ Can be made asynchronous with `@Async`
- ✅ Events can be persisted for audit trail

## Running Modularity Tests

### Verify Module Structure

```bash
JAVA_HOME=~/.sdkman/candidates/java/25 $GRADLE_HOME/bin/gradle test --tests ModularityTests.verifiesModularStructure
```

**What it checks:**
- ❌ Fails if modules have cyclic dependencies
- ❌ Fails if module accesses `internal/` of another module
- ❌ Fails if unnamed dependencies exist
- ✅ Passes if architecture is clean

### Generate Documentation

```bash
JAVA_HOME=~/.sdkman/candidates/java/25 $GRADLE_HOME/bin/gradle test --tests ModularityTests.createModuleDocumentation
```

**Output:**
- `target/spring-modulith-docs/` - Module documentation
- PlantUML diagrams showing module relationships
- Module canvas for each module

## Common Issues and Solutions

### Issue: "Cycle detected between modules X and Y"

**Cause:** Two modules depend on each other

**Solutions:**
1. Use **Domain Events** instead of direct calls
2. Extract common functionality to `common` module
3. Rethink module boundaries - maybe they should be one module

**Example:**
```
Bad:  user → coach → user (cycle!)
Good: user → event → coach (no cycle)
```

### Issue: "Module X accesses internal types of module Y"

**Cause:** Module imports classes from `internal/` package of another module

**Solutions:**
1. Move needed class to public API of module Y
2. Create public interface/DTO in module Y
3. Use events instead of direct access

**Example:**
```java
// Bad - accessing internal class
import com.topleader.topleader.user.internal.UserRepository;

// Good - using public API
import com.topleader.topleader.user.UserService;
```

### Issue: "Module not found"

**Cause:** Package structure doesn't match Spring Modulith conventions

**Solution:**
- Modules must be direct subpackages of `com.topleader.topleader`
- Use `@ApplicationModule` annotation for non-conventional structures

## Module Documentation with @ApplicationModule

Add `package-info.java` to each module for better documentation:

```java
/**
 * User Management Module
 *
 * Handles user authentication, registration, and profile management.
 *
 * Public API:
 * - UserController - REST endpoints for user operations
 * - UserService - Business logic interface
 * - User - User DTO
 *
 * Events published:
 * - UserCreatedEvent - When new user is registered
 * - UserUpdatedEvent - When user profile is updated
 *
 * Dependencies:
 * - common - Shared utilities
 * - credit - For credit account creation (via events)
 */
@ApplicationModule(
    displayName = "User Management",
    allowedDependencies = {"common"}
)
package com.topleader.topleader.user;

import org.springframework.modulith.ApplicationModule;
```

## Integration with CI/CD

Add modularity validation to your CI pipeline:

```yaml
# .github/workflows/build.yml
- name: Validate Module Structure
  run: |
    JAVA_HOME=~/.sdkman/candidates/java/25 \
    $GRADLE_HOME/bin/gradle test --tests ModularityTests.verifiesModularStructure
```

This ensures architecture rules are enforced on every PR.

## Migration Strategy

### Phase 1: Document Current State (DONE)
- ✅ Spring Modulith added to dependencies
- ✅ ModularityTests created
- ✅ Run tests to see current violations

### Phase 2: Fix Critical Violations
1. Run `ModularityTests.verifiesModularStructure`
2. Fix cyclic dependencies first
3. Move `internal/` violating classes to public API or use events

### Phase 3: Refine Module Boundaries
1. Add `internal/` packages to hide implementation
2. Define explicit `@ApplicationModule` for each module
3. Create public API interfaces

### Phase 4: Event-Driven Refactoring
1. Identify cross-module calls
2. Replace with Domain Events
3. Make event processing async where appropriate

## Best Practices

### 1. Keep `common` module minimal
- Only truly shared utilities
- No business logic
- Stable, rarely changing code

### 2. Avoid god modules
- If module has >10 subpackages, consider splitting
- Each module should have single responsibility

### 3. Events over direct calls
- Prefer event-driven communication
- Easier to test and maintain
- Natural async boundaries

### 4. Document module contracts
- Add `package-info.java` with `@ApplicationModule`
- Document public API and events
- Specify allowed dependencies

### 5. Run tests frequently
- Add to CI/CD pipeline
- Run before commits
- Prevents architecture degradation

## Tools and Utilities

### Actuator Endpoints

Spring Modulith provides runtime inspection:

```bash
# View module structure (if Actuator enabled)
curl http://localhost:8080/actuator/modulith
```

### PlantUML Diagrams

Generated diagrams show:
- Module dependencies
- Event flows
- Public APIs

View in: `target/spring-modulith-docs/`

## Further Reading

- [Spring Modulith Reference](https://docs.spring.io/spring-modulith/reference/)
- [Spring Modulith GitHub](https://github.com/spring-projects/spring-modulith)
- [Modular Monolith Pattern](https://www.kamilgrzybek.com/blog/posts/modular-monolith-primer)
- [Domain Events Pattern](https://martinfowler.com/eaaDev/DomainEvent.html)
