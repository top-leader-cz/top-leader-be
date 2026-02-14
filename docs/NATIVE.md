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
