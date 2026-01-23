# Testing Strategy

## Overview

TopLeader backend uses a comprehensive testing strategy that combines unit tests, integration tests, and code coverage reporting to ensure high-quality, maintainable code.

## Test Structure

```
src/test/java/
├── com.topleader.topleader/
│   ├── IntegrationTest.java          # Base class for integration tests
│   ├── ApiTestHelper.java            # Helper for API testing
│   ├── TestUtils.java                # General test utilities
│   ├── AbstractResetDatabaseListener.java
│   ├── ResetDatabaseAfterTestMethodListener.java
│   └── [domain]/
│       ├── *ControllerIT.java        # Controller integration tests
│       ├── *RepositoryIT.java        # Repository integration tests
│       └── *ServiceTest.java         # Service unit tests
└── resources/
    ├── sql/                          # Test SQL scripts
    │   └── [domain]/
    │       └── *-test.sql
    └── application-test.yml          # Test configuration
```

## Test Types

### 1. Integration Tests (IT)

**Naming Convention:** `*IT.java` or `*IntegrationTest.java`

Integration tests extend `IntegrationTest` base class and provide:
- Full Spring Boot application context
- PostgreSQL TestContainer
- Mock SMTP server (GreenMail)
- Mock HTTP server (WireMock)
- Database reset between tests

**Example:**
```java
@Sql(scripts = {"/sql/user/user-repository-test.sql"})
class UserRepositoryIT extends IntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @WithMockUser(username = "test@example.com", authorities = "USER")
    void shouldFindUserByUsername() throws Exception {
        // Given
        var username = "test@example.com";

        // When
        var user = userRepository.findByUsername(username);

        // Then
        assertThat(user.isPresent(), is(true));
        assertThat(user.get().getUsername(), is(username));
    }
}
```

### 2. Controller Integration Tests

**Naming Convention:** `*ControllerIT.java`

Tests REST API endpoints with full Spring Security and database integration.

**Example:**
```java
@Sql(scripts = {"/sql/credit/credit-test.sql"})
class CreditControllerIT extends IntegrationTest {

    @Test
    @WithMockUser(authorities = "ADMIN")
    void shouldProcessPayment() throws Exception {
        mvc.perform(post("/api/protected/admin/payments"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }
}
```

### 3. Repository Tests

**Naming Convention:** `*RepositoryIT.java`

Tests Spring Data JDBC repositories with real database.

### 4. Unit Tests

**Naming Convention:** `*Test.java`

Pure unit tests for business logic without Spring context.

## Code Coverage

### JaCoCo Configuration

Code coverage is automatically generated after running tests.

**Commands:**
```bash
# Run tests and generate coverage report
./gradlew test

# View coverage report
open build/reports/jacoco/test/html/index.html

# Verify coverage meets minimum threshold (70%)
./gradlew jacocoTestCoverageVerification
```

**Coverage Rules:**
- **Overall minimum:** 70%
- **Per-class minimum:** 60%

**Exclusions:**
- Configuration classes (`config/**`, `configuration/**`)
- Application main class
- DTOs and entities
- Exception classes

### Coverage Reports

Coverage reports are generated in:
- **HTML:** `build/reports/jacoco/test/html/index.html`
- **XML:** `build/reports/jacoco/test/jacocoTestReport.xml`

## Test Data Management

### SQL Scripts

Test data is loaded using `@Sql` annotation:

```java
@Sql(scripts = {"/sql/domain/test-scenario.sql"})
class MyTest extends IntegrationTest {
    // ...
}
```

**Location:** `src/test/resources/sql/[domain]/`

### Database Reset

Database is automatically reset after each test method via `ResetDatabaseAfterTestMethodListener`.

## Testing Tools & Libraries

### Core Testing
- **JUnit 5** - Test framework
- **AssertJ / Hamcrest** - Assertions
- **Mockito** - Mocking (included in spring-boot-starter-test)

### Spring Testing
- **Spring Boot Test** - Spring context testing
- **Spring Security Test** - `@WithMockUser`, security testing
- **MockMvc** - REST API testing without HTTP

### Integration Testing
- **Testcontainers** - PostgreSQL container
- **GreenMail** - SMTP server mock
- **WireMock** - HTTP client mocking

### Utilities
- **JSON Unit** - JSON assertion library

## Best Practices

### 1. Test Organization

**Follow Given-When-Then structure:**
```java
@Test
void shouldProcessPayment() {
    // Given - Setup test data and preconditions
    var payment = createPayment();

    // When - Execute the action being tested
    var result = paymentService.process(payment);

    // Then - Verify the outcome
    assertThat(result.isSuccess(), is(true));
}
```

### 2. Test Naming

Use descriptive test names that explain what is being tested:
- ✅ `shouldReturnUserWhenUsernameExists()`
- ✅ `shouldThrowExceptionWhenCreditInsufficient()`
- ❌ `test1()`
- ❌ `testUser()`

### 3. Security Testing

Always test authorization:
```java
@Test
@WithMockUser(authorities = "USER")
void shouldAllowAccessForUser() { /* ... */ }

@Test
@WithMockUser(authorities = "WRONG_ROLE")
void shouldDenyAccessForWrongRole() {
    mvc.perform(get("/api/protected/admin/endpoint"))
        .andExpect(status().isForbidden());
}

@Test
void shouldRequireAuthentication() {
    mvc.perform(get("/api/protected/endpoint"))
        .andExpect(status().isUnauthorized());
}
```

### 4. Test Isolation

Each test should be independent:
- Use `@Sql` to load fresh test data
- Don't rely on test execution order
- Clean up resources in `@AfterEach`

### 5. API Testing Helper

Use `ApiTestHelper` for cleaner API tests:
```java
@Autowired
private ObjectMapper objectMapper;

private ApiTestHelper apiHelper;

@BeforeEach
void setUp() {
    apiHelper = new ApiTestHelper(mvc, objectMapper);
}

@Test
@WithMockUser(authorities = "USER")
void shouldCreateUser() throws Exception {
    var request = new CreateUserRequest("John", "Doe");

    apiHelper.performPost("/api/users", request)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists());
}
```

### 6. Test Data

Keep test data minimal and focused:
- Only include data relevant to the test
- Use descriptive identifiers (e.g., `test.user@example.com`)
- Document complex test scenarios

## Running Tests

### All Tests
```bash
./gradlew test
```

### Specific Test Class
```bash
./gradlew test --tests UserRepositoryIT
```

### Specific Test Method
```bash
./gradlew test --tests UserRepositoryIT.shouldFindUserByUsername
```

### With Coverage Report
```bash
./gradlew test jacocoTestReport
```

### Continuous Testing
```bash
./gradlew test --continuous
```

## CI/CD Integration

Tests run automatically in GitHub Actions:
- On every pull request
- On push to main branch
- Coverage reports are generated and archived

## OpenAPI & Postman

### Generate OpenAPI Spec

```bash
./gradlew generateOpenApi
```

This creates `src/main/resources/static/openapi.yaml`

### Convert to Postman Collection

```bash
python scripts/openapi_to_postman.py \
    src/main/resources/static/openapi.yaml \
    postman/top-leader.postman_collection.json
```

Import the generated JSON file into Postman.

## Future Improvements

### High Priority
1. **Contract Testing** - Add Spring Cloud Contract for API contracts
2. **Performance Testing** - Add JMH benchmarks for critical paths
3. **Mutation Testing** - Add PIT mutation testing for test quality
4. **Architecture Testing** - Add ArchUnit for architecture rules

### Medium Priority
5. **Chaos Testing** - Add Chaos Monkey for resilience testing
6. **Security Testing** - Add OWASP ZAP integration
7. **Visual Regression** - Add Percy or similar for UI testing

### Low Priority
8. **Load Testing** - Add Gatling for load testing
9. **Accessibility Testing** - Add axe-core for a11y testing

## Resources

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/reference/testing/index.html)
- [Testcontainers](https://testcontainers.com/)
- [AssertJ](https://assertj.github.io/doc/)
- [JaCoCo](https://www.jacoco.org/jacoco/trunk/doc/)

## Questions?

Contact the development team or check the `#engineering` channel in Slack.
