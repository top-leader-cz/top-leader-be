# Testing Quick Start Guide

Quick reference for running tests and generating reports.

## 🚀 Quick Commands

```bash
# Run all tests
make test

# Run tests + generate coverage report
make test-coverage

# View coverage report in browser
make test-report

# Verify coverage meets 70% minimum
make coverage-verify

# Generate Postman collection
make postman
```

## 📊 Code Coverage

### Current Status

Run `make test-coverage` to see current coverage.

**Target:** 70% overall coverage, 60% per class

### View Report

```bash
make test-report
```

Opens: `build/reports/jacoco/test/html/index.html`

### Coverage by Package

The report shows coverage for:
- **Controllers** - API endpoints
- **Services** - Business logic
- **Repositories** - Data access
- **Utilities** - Helper functions

**Excluded from coverage:**
- Configuration classes
- DTOs/Entities
- Exception classes
- Application main class

## 🧪 Writing Tests

### Integration Test Template

```java
@Sql(scripts = {"/sql/domain/test-data.sql"})
class MyControllerIT extends IntegrationTest {

    @Test
    @WithMockUser(username = "test@example.com", authorities = "USER")
    void shouldReturnSuccess() throws Exception {
        mvc.perform(get("/api/protected/endpoint"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }
}
```

### Key Components

1. **Extend `IntegrationTest`** - Full Spring context + TestContainers
2. **Use `@Sql`** - Load test data
3. **Use `@WithMockUser`** - Mock authentication
4. **Use `MockMvc`** - Test REST API
5. **Use Hamcrest/AssertJ** - Make assertions

## 📝 Test Data

Create SQL file in `src/test/resources/sql/[domain]/`:

```sql
-- src/test/resources/sql/user/user-test.sql

INSERT INTO user_account (id, username, first_name, last_name, email, created_at)
VALUES (1, 'test@example.com', 'Test', 'User', 'test@example.com', NOW());
```

Reference in test:

```java
@Sql(scripts = {"/sql/user/user-test.sql"})
class UserTest extends IntegrationTest { ... }
```

## 🔧 API Testing with Postman

### 1. Generate OpenAPI Spec

```bash
make openapi
```

Creates: `src/main/resources/static/openapi.yaml`

### 2. Convert to Postman Collection

```bash
make postman
```

Creates: `postman/top-leader.postman_collection.json`

### 3. Import into Postman

1. Open Postman
2. Click **Import**
3. Select `postman/top-leader.postman_collection.json`
4. Set `baseUrl` variable to your API URL

## 🎯 Testing Best Practices

### 1. Given-When-Then Structure

```java
@Test
void shouldProcessPayment() {
    // Given - Setup
    var payment = createPayment();

    // When - Execute
    var result = service.process(payment);

    // Then - Verify
    assertThat(result.isSuccess(), is(true));
}
```

### 2. Test Security

Always test authentication and authorization:

```java
@Test
void shouldRequireAuthentication() {
    mvc.perform(get("/api/protected/endpoint"))
        .andExpect(status().isUnauthorized());
}

@Test
@WithMockUser(authorities = "WRONG_ROLE")
void shouldDenyAccessForWrongRole() {
    mvc.perform(get("/api/protected/admin/endpoint"))
        .andExpect(status().isForbidden());
}
```

### 3. Test Error Cases

```java
@Test
@WithMockUser
void shouldReturn400ForInvalidInput() {
    var invalidRequest = """
        {
            "email": "not-an-email"
        }
        """;

    mvc.perform(post("/api/users")
            .contentType("application/json")
            .content(invalidRequest))
        .andExpect(status().isBadRequest());
}
```

## 📚 More Resources

See [TESTING_STRATEGY.md](./TESTING_STRATEGY.md) for detailed testing documentation.

## 🐛 Troubleshooting

### Tests fail with "Container startup failed"

**Issue:** TestContainer can't start PostgreSQL

**Solution:**
```bash
# Check Docker is running
docker ps

# Restart Docker Desktop
```

### Tests fail with "Port already in use"

**Issue:** Another instance is using the port

**Solution:**
```bash
# Kill process on port 8080
lsof -ti:8080 | xargs kill -9
```

### Coverage report not generated

**Issue:** JaCoCo report missing

**Solution:**
```bash
# Clean and rebuild
./gradlew clean test jacocoTestReport
```

## 💡 Pro Tips

1. **Run tests continuously during development:**
   ```bash
   ./gradlew test --continuous
   ```

2. **Run specific test:**
   ```bash
   ./gradlew test --tests UserControllerIT
   ```

3. **Run tests matching pattern:**
   ```bash
   ./gradlew test --tests "*ControllerIT"
   ```

4. **Skip tests during build:**
   ```bash
   ./gradlew build -x test
   ```

5. **Debug test in IDE:**
   - Right-click test class/method
   - Select "Debug 'TestName'"
   - Set breakpoints as needed
