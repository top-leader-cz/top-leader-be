# ADR-003: Native Image Strategy

**Status:** In Progress
**Date:** 2025-10-15
**Decision Makers:** Development Team

## Context

We want to optimize the application for cloud deployment with:
- Instant startup times (cold starts)
- Reduced memory footprint
- Lower cloud costs (pay-per-use models like Cloud Run)

GraalVM Native Image compilation can achieve these goals but requires careful library selection and architecture decisions.

## Decision

We are **preparing for GraalVM Native Image** compilation through incremental changes:
1. Avoid reflection-heavy libraries
2. Use Java 25 + Spring Boot 4 for best native support
3. Pre-generate artifacts that would require reflection at runtime
4. Plan removal of incompatible libraries

## Rationale

### Why Native Images?

| Metric | JVM | Native Image |
|--------|-----|--------------|
| Startup time | 5-15 seconds | 50-200 ms |
| Memory footprint | 300-500 MB | 50-100 MB |
| Peak performance | Higher | Lower (acceptable) |
| Cloud Run cold start | Problematic | Excellent |

### Incremental Approach
- Don't block current development
- Validate each change works in both modes
- Maintain JVM as fallback

### Why Spring Data JDBC over Hibernate?

Spring Data JDBC is significantly better for native images:

| Aspect              | Hibernate/JPA              | Spring Data JDBC        |
|---------------------|----------------------------|-------------------------|
| Reflection usage    | Heavy (proxies, lazy load) | Minimal (simple mapping)|
| Runtime bytecode    | Yes (proxy generation)     | No                      |
| Native hints needed | Many                       | Few                     |
| Startup time        | Slower (entity scanning)   | Fast (direct mapping)   |
| Native image size   | Larger                     | Smaller                 |
| Query control       | JPQL/HQL abstraction       | Native SQL              |

**Key benefits:**
- No lazy loading proxies → no reflection issues
- Direct row-to-object mapping → simpler model
- Full SQL control with `@Query` → better optimization
- Smaller native image footprint → lower cloud costs

## Current Preparations

### Completed
- [x] **Java 25** - best native image support in JDK
- [x] **Spring Boot 4** - first-class AOT support
- [x] **Jetty** over Tomcat - better native compatibility
- [x] **Log4j2** over Logback - native-friendly logging
- [x] **Pre-generated OpenAPI** - no runtime reflection for API docs
- [x] **Failsafe** for resilience - no reflection
- [x] **Virtual Threads** - simplifies concurrency model
- [x] **Spring Data JDBC** - migrated from Hibernate/JPA for minimal reflection, simpler model, full SQL control

### Removed Heavy Reflection Libraries
- [x] **Apache Velocity** (`velocity-engine-core`, `velocity-tools-generic`) - template engine with heavy reflection, replaced with simple string templates
- [x] **Resilience4j** - reflection-heavy resilience library, replaced with **Failsafe** (zero reflection)
- [x] **Vavr** - functional library with reflection overhead, replaced with native Java streams
- [x] **springdoc-openapi runtime** - dynamic OpenAPI generation, replaced with pre-generated `openapi.yaml`
- [x] **google-http-client-jackson2** - legacy Google HTTP client, using modern alternatives
- [x] **spring-cloud-gcp-starter-logging** - removed GCP-specific logging, using standard Log4j2
- [x] **iCal4j** - heavy reflection iCalendar library, replaced with template-based `ICalService` using `TemplateService` and `.ics` templates
- [x] **Hibernate/JPA** - heavy reflection ORM framework, replaced with **Spring Data JDBC** for minimal reflection and native image compatibility

### In Progress
- [ ] Add GraalVM reachability metadata where needed
- [ ] Test native compilation in CI pipeline
- [ ] Validate Spring AI components for native support

### Blockers Removed
- ~~**Hibernate 7**~~ - ✅ Migrated to Spring Data JDBC (minimal reflection)
- **Some Spring AI components** - waiting for native support (non-critical)

## Consequences

### Positive
- Instant cold starts on Cloud Run
- 50-80% memory reduction
- Lower cloud costs
- Faster autoscaling

### Negative
- Longer build times (AOT compilation)
- Some libraries won't work
- Debugging is harder in native mode
- Must maintain reflection hints

### Mitigations
- Keep JVM mode as fallback for development
- Use Spring Boot's AOT processing
- Gradual migration, not big bang

## Migration Path

```
Phase 1 (Current): Prepare codebase
├── Remove reflection-heavy code
├── Use native-compatible libraries
└── Pre-generate runtime artifacts

Phase 2: Test native compilation
├── Add native-maven-plugin / native-gradle-plugin
├── Run tests in native mode
└── Fix reflection issues with hints

Phase 3: Production deployment
├── Build native image in CI
├── Deploy to Cloud Run
└── Monitor and optimize
```

## Alternatives Considered

### Stay on JVM
- Simpler, but doesn't solve cold start problem
- Higher memory costs

### CRaC (Coordinated Restore at Checkpoint)
- Alternative to native images
- Faster startup via checkpoint/restore
- Less mature ecosystem
- May consider as complement

### AWS Lambda SnapStart
- Platform-specific solution
- We're on GCP, not applicable

## References

- [GraalVM Native Image](https://www.graalvm.org/native-image/)
- [Spring Boot Native Image Support](https://docs.spring.io/spring-boot/docs/current/reference/html/native-image.html)
- [Spring Framework AOT](https://docs.spring.io/spring-framework/reference/core/aot.html)
