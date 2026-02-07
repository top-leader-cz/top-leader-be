# Native Image & Startup Optimization

## CDS (Class Data Sharing) - Not Recommended

We attempted to implement CDS (Class Data Sharing) to improve startup time, but it did not work well with Spring Boot.

### Issues Encountered

1. **Training environment mismatch** - CDS requires the training run to match the runtime environment exactly. Spring Boot's dynamic nature (proxies, AOP, lazy initialization) means many classes are not loaded during training.

2. **`UseCompactObjectHeaders` flag mismatch** - The JVM flag must be consistent between training and runtime, otherwise the archive is incompatible.

3. **Spring Security/Session issues** - Authentication-related classes were not properly archived, causing login failures at runtime.

4. **No startup improvement** - With CDS enabled, startup was actually slower (20s vs 16s without CDS).

### Why CDS Doesn't Work Well with Spring Boot

- Spring Boot uses extensive reflection and dynamic class loading
- Spring AOT helps but doesn't solve all issues
- The training run (`-Dspring.context.exit=onRefresh`) exits before all classes are loaded
- Database connections, security filters, and session management require runtime initialization

### Alternatives

| Option | Startup Time | Notes |
|--------|--------------|-------|
| JVM + Spring AOT | ~15-17s | Current approach |
| CRaC (Checkpoint/Restore) | <1s | Requires special JDK, complex setup |
| GraalVM Native Image | **~0.3-0.5s** | No JPA = very fast startup, longer build time |

### Current Optimization Strategy

Instead of CDS, we use:

1. **Spring AOT** - Pre-computes bean definitions at build time
2. **jlink custom JRE** - Minimal JRE with only required modules (~53MB)
3. **JVM flags** - Tiered compilation, G1GC, memory settings
4. **Distroless base image** - Minimal container footprint

### Dockerfile Configuration

```dockerfile
# Stage 1: Build with Spring AOT
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /app
COPY gradle gradle
COPY gradlew build.gradle.kts settings.gradle.kts ./
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon || true
COPY src src
RUN ./gradlew bootJar -x test -Dspring.aot.enabled=true --no-daemon

# Stage 2: Create custom JRE with jlink (~53MB)
FROM eclipse-temurin:25-jdk AS jre-builder
ENV MODULES="java.base,java.compiler,java.desktop,java.net.http,java.sql,java.logging,java.naming,java.management,java.instrument,java.security.sasl,jdk.crypto.ec,jdk.unsupported"
RUN jlink \
    --add-modules $MODULES \
    --strip-debug \
    --no-man-pages \
    --no-header-files \
    --compress=zip-6 \
    --output /custom-jre

# Stage 3: Runtime with distroless (~20MB base)
FROM gcr.io/distroless/base-debian12
COPY --from=jre-builder /custom-jre /opt/java
WORKDIR /app
COPY --from=builder /app/build/libs/top-leader.jar app.jar
ENV JAVA_HOME=/opt/java
ENV PATH="$JAVA_HOME/bin:$PATH"
ENV PORT=8080
EXPOSE 8080

# JVM optimizations for Cloud Run (512Mi)
ENTRYPOINT ["/opt/java/bin/java", \
    "-XX:MaxRAMPercentage=70.0", \
    "-XX:InitialRAMPercentage=70.0", \
    "-XX:+UseG1GC", \
    "-XX:+UseCompactObjectHeaders", \
    "-XX:+UseStringDeduplication", \
    "-XX:+TieredCompilation", \
    "-XX:TieredStopAtLevel=1", \
    "-jar", "app.jar"]
```

### JVM Flags Explained

| Flag | Purpose |
|------|---------|
| `MaxRAMPercentage=70.0` | Max heap = 70% of 512MB (~358MB) |
| `InitialRAMPercentage=70.0` | Pre-allocate heap at startup (faster) |
| `UseG1GC` | Best GC for containers |
| `UseCompactObjectHeaders` | Java 25 feature, reduces object header size |
| `UseStringDeduplication` | Reduces memory for duplicate strings |
| `TieredCompilation` | Faster JIT warmup |
| `TieredStopAtLevel=1` | C1 compiler only (faster startup, slower peak) |

### jlink Modules Explained

| Module | Required For |
|--------|--------------|
| `java.base` | Core Java |
| `java.compiler` | Spring AOT |
| `java.desktop` | AWT fonts (some libs need it) |
| `java.net.http` | HTTP client |
| `java.sql` | JDBC/PostgreSQL |
| `java.logging` | Logging |
| `java.naming` | JNDI (Spring) |
| `java.management` | Required by Log4j2 |
| `java.instrument` | Bytecode (Spring AOP) |
| `java.security.sasl` | Security |
| `jdk.crypto.ec` | TLS/SSL |
| `jdk.unsupported` | Unsafe (Netty, Jackson) |

### Future Considerations

- **Project Leyden** - When fully available, may provide better CDS support for Spring Boot
- **CRaC** - Consider when Cloud Run adds native support
- **GraalVM Native** - Already configured in build.gradle.kts, use when build time is acceptable

### Optional: Remove postgres-socket-factory (Cloud Run only)

The `com.google.cloud.sql:postgres-socket-factory` dependency can be removed if running exclusively on Cloud Run. This saves ~0.5s startup and reduces JAR size.

**Current setup (App Engine + Cloud Run compatible):**
```yaml
spring:
  datasource:
    url: jdbc:postgresql:///<DATABASE_NAME>
    hikari:
      data-source-properties:
        socketFactory: com.google.cloud.sql.postgres.SocketFactory
        cloudSqlInstance: <PROJECT_ID>:<REGION>:<INSTANCE_NAME>
```

**Cloud Run only setup (no socket factory needed):**
```yaml
spring:
  datasource:
    url: jdbc:postgresql:///<DATABASE_NAME>?host=/cloudsql/<PROJECT_ID>:<REGION>:<INSTANCE_NAME>
    # Remove socketFactory and cloudSqlInstance from data-source-properties
```

**Trade-offs:**

| Keep socket-factory | Remove socket-factory |
|---------------------|----------------------|
| Works on App Engine + Cloud Run | Cloud Run only |
| IAM authentication support | Password auth only |
| Auto retry/reconnect | Manual handling |
| +0.5s startup | Faster startup |
