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

## Current Preparations

### Completed
- [x] **Java 25** - best native image support in JDK
- [x] **Spring Boot 4** - first-class AOT support
- [x] **Jetty** over Tomcat - better native compatibility
- [x] **Log4j2** over Logback - native-friendly logging
- [x] **Pre-generated OpenAPI** - no runtime reflection for API docs
- [x] **Failsafe** for resilience - no reflection
- [x] **Virtual Threads** - simplifies concurrency model

### In Progress
- [ ] Remove **iCal4j** - heavy reflection, replace with simple implementation
- [ ] Evaluate **Hibernate** alternatives - consider jOOQ or plain JDBC
- [ ] Add GraalVM reachability metadata where needed
- [ ] Test native compilation in CI pipeline

### Blocked
- **Hibernate 7** - still requires reflection hints, but improving
- **Some Spring AI components** - waiting for native support

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
