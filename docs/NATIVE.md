# Native Image & Startup Optimization

## GraalVM Native Image (QA)

Native image is deployed on QA via GitHub Actions for fast cold starts with scale-to-zero.

### Performance

| Metric | JVM + AOT | Native Image |
|--------|-----------|--------------|
| Startup | ~12-15s | **~0.6s** |
| Memory (Cloud Run) | 600Mi | 256-384Mi |
| Peak throughput | faster (JIT) | ~10-30% slower |
| Image size | ~73MB JAR + JRE | ~165MB binary |
| Build time (CI) | ~3 min | ~8-12 min |

### Build Config

- GraalVM CE 25, Java 25
- `-march=x86-64` (baseline, compatible with all x86-64 CPUs including Cloud Run)
- `--initialize-at-build-time` for logging + serialization libs (Log4j2, Jackson, SnakeYAML)
- No `-Ob` flag (full optimization for smaller binary + faster runtime)
- No JPA/Hibernate (JDBC only) — simplifies native compilation significantly

### Deploy to QA

```bash
make deploy-qa-native
```

This pushes a `qa-native-*` tag which triggers GitHub Actions:
1. Builds native image inside Docker (`Dockerfile.native`) on x86-64 runner
2. Pushes to Artifact Registry as `native-latest` + `native-{sha}`
3. Deploys to Cloud Run QA
4. Cleans up old revisions and images

### CI Pipeline (GitHub Actions)

- **Runner**: `ubuntu-latest` (4 vCPU, 16 GB RAM)
- **Docker BuildKit** with GHA cache (caches base image + dependency layers)
- **Build time**: ~8-12 min (first build ~15 min due to dependency download)
- Tag pattern: `qa-native-*` triggers `deploy-qa-native` job in `deploy.yml`

### Dockerfile.native

Multi-stage build:
1. **Builder**: `ghcr.io/graalvm/native-image-community:25` — compiles native binary
2. **Runtime**: `debian:bookworm-slim` — minimal runtime with ca-certificates

```dockerfile
ENTRYPOINT ["./top-leader", "-Xmx256m", "-Xms256m"]
```

### Reflection Hints (NativeImageConfiguration)

GraalVM native image removes all classes not reachable via static analysis. Jackson uses reflection at runtime (`Class.getRecordComponents()`) to deserialize JSON into records/classes. These types must be registered manually.

All reflection hints are centralized in `NativeImageConfiguration.java`:

```java
// RestClient response types (Jackson deserialization via reflection)
registerForJsonSerialization(hints, GcsLightweightClient.MetadataTokenResponse.class);
registerForJsonSerialization(hints, GoogleCalendarApiClientFactory.TokenResponse.class);
registerForJsonSerialization(hints, DaliResponse.class);
// ... etc
```

**When to add new hints**: Any class/record used with `RestClient.body(MyClass.class)` or `ParameterizedTypeReference<MyClass>` needs a `registerForJsonSerialization()` entry.

**Controller `@RequestBody` types**: Spring AOT handles these automatically — no manual hints needed.

### Known Issues & Fixes

| Issue | Cause | Fix |
|-------|-------|-----|
| `CPU does not support x86-64-v3` | `-march=native` compiled for CI CPU | Changed to `-march=x86-64` |
| `Record components not available` | Missing reflection hints for Jackson | Added to `NativeImageConfiguration` |
| Can't build on Apple Silicon | QEMU can't emulate x86-64-v3 | Build in CI (GitHub Actions) |
| `ConversionFailedException: SecurityContextImpl` | Spring Session uses Java serialization (not supported in native) | Registered serialization hints in `NativeImageConfiguration` |
| `ClassCastException: ArrayList -> Set` | AOT-generated repository code returns ArrayList for `@Query` | Changed `Set<String>` return types to `List<String>` |
| `NoSuchMethodError: matches(T) in ArgumentMatcher` | Mockito incompatible with GraalVM native | Remove Mockito, use manual test doubles |
| Boolean fields mapped as Integer | `@PersistenceCreator` needed for constructor-based entity instantiation | Added `@AllArgsConstructor(onConstructor_ = @PersistenceCreator)` + `IntegerToBooleanConverter` |

### Local Native Build (macOS only)

```bash
make native          # Build for local macOS (Apple Silicon)
```

This builds a macOS ARM binary — useful for local testing but **not deployable** to Cloud Run (needs linux/amd64).

---

## JVM + Spring AOT (PROD)

Production uses JVM with Spring AOT for better peak throughput after JIT warmup.

### Current Optimization Strategy

1. **Spring AOT** (`-Dspring.aot.enabled=true`) — pre-computes bean definitions at build time
2. **Custom JRE** — pre-built jlink JRE in Artifact Registry (~53MB)
3. **JVM flags** — G1GC, memory settings, compact object headers
4. **Clean build** — `gradlew clean bootJar` prevents stale AOT cache

### Dockerfile Configuration

```dockerfile
FROM eclipse-temurin:25-jdk AS builder
RUN ./gradlew clean bootJar -x test --no-daemon

FROM europe-west3-docker.pkg.dev/.../topleader-jre:latest
ENTRYPOINT ["/opt/java/bin/java", \
    "-Dspring.aot.enabled=true", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:InitialRAMPercentage=75.0", \
    "-XX:+UseG1GC", \
    "-XX:+UseCompactObjectHeaders", \
    "-XX:+UseStringDeduplication", \
    "-XX:+TieredCompilation", \
    "-XX:MaxMetaspaceSize=128m", \
    "-XX:+ExitOnOutOfMemoryError", \
    "-XX:G1HeapRegionSize=4m", \
    "-jar", "app.jar"]
```

### JVM Flags

| Flag | Purpose |
|------|---------|
| `MaxRAMPercentage=75.0` | Max heap = 75% of container memory |
| `InitialRAMPercentage=75.0` | Pre-allocate heap at startup (faster) |
| `UseG1GC` | Best GC for containers |
| `UseCompactObjectHeaders` | Java 25 feature, reduces object header size |
| `UseStringDeduplication` | Reduces memory for duplicate strings |
| `TieredCompilation` | Full JIT (C1 + C2) for best peak throughput |
| `MaxMetaspaceSize=128m` | Cap metaspace (Spring Boot needs ~90-100m) |
| `ExitOnOutOfMemoryError` | Kill container on OOM (Cloud Run restarts it) |

---

## CDS (Class Data Sharing) - Not Recommended

We attempted CDS but it did not work well with Spring Boot.

### Issues

- Training environment mismatch — CDS requires exact match between training and runtime
- `UseCompactObjectHeaders` flag mismatch between training and runtime
- Spring Security/Session classes not properly archived
- No startup improvement (20s vs 16s without CDS)

### Why CDS Doesn't Work with Spring Boot

- Extensive reflection and dynamic class loading
- Training run exits before all classes are loaded
- Database connections, security filters, session management require runtime init

---

## Strategy: JVM for PROD, Native for QA

| | QA (Native) | PROD (JVM + AOT) |
|---|---|---|
| Startup | ~0.6s | ~12-15s |
| Peak throughput | slower | **faster** (JIT) |
| Memory | 256-384Mi | 600Mi |
| Scale to zero | yes (fast cold start) | possible but slow cold start |
| Build time | ~8-12 min | ~3 min |

**Rationale**: QA has low traffic and scale-to-zero — fast cold start matters. PROD has sustained traffic — peak throughput matters more than startup.

### Future Considerations

- **GraalVM PGO** (Profile-Guided Optimization) — could close the throughput gap
- **Project Leyden** — better CDS/AOT for standard JVM
- **CRaC** — checkpoint/restore when Cloud Run adds support
- **Native for PROD** — consider when native throughput gap narrows

---

## Session Serialization

Spring Session JDBC defaults to Java serialization (`SerializingConverter`/`DeserializingConverter`) for storing session attributes in the `spring_session_attributes` table. Java serialization does **not work** in GraalVM native images without serialization metadata.

### Solution: GraalVM Serialization Hints

Register Spring Security session types for Java serialization in `NativeImageConfiguration.registerSessionSecurityClasses()`. No custom serialization code needed — the default Spring Session JDBC converters work as-is.

```java
// NativeImageConfiguration.java
hints.serialization().registerType(TypeReference.of(Class.forName(className)));
```

**Registered types**: `SecurityContextImpl`, `UsernamePasswordAuthenticationToken`, `SimpleGrantedAuthority`, `User`, `WebAuthenticationDetails`, and JDK collection types used by Spring Security.

**Why this works**: GraalVM native image supports Java serialization when types are registered via `RuntimeHints.serialization()`. This is simpler than JSON serialization — no custom converters, no Jackson configuration, and fully compatible with existing sessions in the database.

---

## Native Test Progress (WIP)

Running `make native-test` (`nativeTest` Gradle task) to verify all 257 tests pass in native image. Status: **in progress**.

### Test Pipeline

Native tests require a two-step build:
1. `gradle test` (JVM) — runs tests with TestContainers, populates `build/test-results/test/` directory
2. `nativeTestCompile` — has `onlyIf` condition checking `testListDirectory.exists()` (populated by step 1)
3. `nativeTest` — runs the compiled native test binary against an **external PostgreSQL** (not TestContainers)

```bash
# Full pipeline
make test && make native-test

# Environment variables for native test DB (external PostgreSQL)
export NATIVE_DB_URL=jdbc:postgresql://localhost:5432/topleadertest
export NATIVE_DB_USER=topleader
export NATIVE_DB_PASS=topleader
```

The `build.gradle.kts` propagates `NATIVE_DB_URL/USER/PASS` to `processTestAot` and `nativeTest` tasks.

### Completed Fixes

#### 1. Boolean field mapping (100 -> 201 tests passing)

Spring Data JDBC AOT generates code that uses property-based instantiation by default. Boolean fields (`is_coach`, `is_public`, etc.) were mapped as `Integer` instead of `Boolean`.

**Fix**: Added `@AllArgsConstructor(onConstructor_ = @PersistenceCreator)` to 8 entity classes to force constructor-based instantiation + added `IntegerToBooleanConverter` in `JdbcConfiguration.java`.

Affected entities: `Coach`, `FeedbackForm`, `FeedbackFormQuestion`, `Question`, `Recipient`, `Message`, `ScheduledSession`, `UserInsight`.

#### 2. Set<String> return type in @Query (ClassCastException)

Spring Data JDBC AOT generates repository code that always returns `ArrayList` for `@Query` methods. When the method signature says `Set<String>`, it throws `ClassCastException: ArrayList cannot be cast to Set`.

**Fix**: Changed return types from `Set<String>` to `List<String>`:
- `UserRepository.findAllowedCoachRates()` — returns `List<String>`
- `CompanyRepository.findCoachRatesByCompanyId()` — returns `List<String>`

Updated all callers to wrap with `new HashSet<>(list)` where `Set` is needed:
- `CompanyController.loadWithRates()`
- `CoachListController.getAllowedRates()`
- `AdminViewControllerIT` assertion changed to `containsExactlyInAnyOrderElementsOf()`

#### 3. Mockito incompatibility (NoSuchMethodError)

Mockito's `ArgumentMatcher.matches(T)` method is not found in native image due to generic type erasure. Affects 5 tests in 2 classes.

**Partially fixed**: Replaced Mockito mocks with simple lambdas in `TestBeanConfiguration` for `searchArticles`/`searchVideos` beans.

**TODO**: Remove Mockito from these test classes:
- `FeedbackControllerTest` (3 tests) — uses `mock()`, `when().thenReturn()` (simple stubs, no argument matchers)
- `MessageServiceTest` (2 tests) — uses `mock()`, `when()`, `verify()`, `never()`, `any()`, `anyString()`, `anyList()`

Approach: Replace with manual test doubles (stub implementations that track invocations).

#### 4. Reflection hints for test classes

Added to `TestBeanConfiguration.TestRuntimeHints`:
- `ResetDatabaseAfterTestMethodListener` — needs reflection for instantiation in native image
- JsonPath function classes (`Length`, `Concatenate`, `Min`, `Max`, `Average`, `Sum`) — used by json-unit-assertj

Added to `NativeImageConfiguration.TopLeaderRuntimeHints`:
- `McpToolsConfig.SessionHistoryEntry` — needed for Jackson serialization

#### 5. DB cleanup between tests (ScriptStatementFailedException)

`ResetDatabaseAfterTestMethodListener` needed `beforeTestMethod` + `ensureDataSource` for robust cleanup in native image. The listener truncates all tables except `flyway_schema_history` before and after each test method.

**Status**: Fixed in code but **not yet verified** in native image. The last native test run (236/257 errors) was before this fix was applied, and DB was not initialized.

#### 6. AOT RowMapper bug for custom projection types

Spring Data JDBC AOT generates `getRowMapperFactory().create(Message.class)` instead of `UnreadMessagesCount.class` when a `@Query` method returns a custom projection type (e.g. `List<UnreadMessagesCount>`). This causes `MappingInstantiationException` in native image.

**Fix**: Moved `getUnreadMessagesCount` from `MessageRepository` to `MessageService` using `NamedParameterJdbcOperations` with manual `RowMapper`.

Affected: `MessageControllerIT.testMarkMessagesAsDisplayed()`, `MessageControllerIT.testGetUserChatInfo()`.

#### 7. Spring AI `entity(Class)` fails in native

`ChatClient.entity(Summary.class)` uses `BeanOutputConverter` which generates JSON schema via reflection — fails in native image. Other `entity()` calls use `ParameterizedTypeReference` which takes a different code path and works fine.

**Fix**: Changed `AiClient.generateSummary()` to use `chatClient.prompt(prompt).call().content()` + `JsonUtils.fromJsonString(content, Summary.class)` instead of `.entity(Summary.class)`.

#### 8. Missing resource registration for translation files

`translation/questions-translation.json` was not registered in `NativeImageConfiguration`, causing `NullPointerException` when loading translations at runtime in native image.

**Fix**: Added `hints.resources().registerPattern("translation/**")` in `NativeImageConfiguration`.

#### 9. DaliResponse null handling

`DaliResponse.created` was `long` (primitive) which fails when Jackson encounters `null` from mocks/tests.

**Fix**: Changed `long created` → `Long created` in `DaliResponse` record.

#### 10. Native image memory limit

Native image build could exceed available memory on CI or local machines.

**Fix**: Added `-J-Xmx10g` to `buildArgs` in `build.gradle.kts` `graalvmNative` section.

### Remaining Issues

| Issue | Tests affected | Status |
|-------|---------------|--------|
| Awaitility deadlocks | 1 test (UserInsightControllerIT.dashboardWithMcp) | Not investigated |
| AWT `UnsatisfiedLinkError` | 1 test (CoachControllerIT.setCoachImage) | Not investigated |
| Summary null fields | 1 test (PublicFeedbackControllerIT.submitForm) | Fixes applied (JsonUtils + translation resource), needs native verification |

### Key Files

| File | Purpose |
|------|---------|
| `NativeImageConfiguration.java` | All production reflection/serialization hints |
| `TestBeanConfiguration.java` | Test-only reflection hints + mock bean replacements |
| `ResetDatabaseAfterTestMethodListener.java` | DB cleanup between native tests |
| `JdbcConfiguration.java` | JDBC converters including `IntegerToBooleanConverter` |
| `build.gradle.kts` | `NATIVE_DB_URL` propagation to AOT + native test tasks |
| `Makefile` | `native-test` target |

### Running Native Tests Locally

```bash
# 1. Start external PostgreSQL (e.g. Docker)
docker run -d --name pg-native-test \
  -e POSTGRES_DB=topleadertest \
  -e POSTGRES_USER=topleader \
  -e POSTGRES_PASSWORD=topleader \
  -p 5432:5432 postgres:17

# 2. Set env vars
export NATIVE_DB_URL=jdbc:postgresql://localhost:5432/topleadertest
export NATIVE_DB_USER=topleader
export NATIVE_DB_PASS=topleader

# 3. Clean build + JVM tests + native tests
gradle clean test nativeTest

# Important: don't use -x test — nativeTestCompile needs the test results directory
```
