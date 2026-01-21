# Stage 1: Build
FROM eclipse-temurin:25-jdk AS builder

WORKDIR /app

# Copy Gradle files first for better caching
COPY gradle gradle
COPY gradlew build.gradle.kts settings.gradle.kts ./
RUN chmod +x gradlew

# Download dependencies (cached layer)
RUN ./gradlew dependencies --no-daemon || true

# Copy source and build with Spring AOT
COPY src src
RUN ./gradlew bootJar -x test -Dspring.aot.enabled=true --no-daemon

# Stage 2: Runtime (Chainguard distroless)
FROM cgr.dev/chainguard/jre:latest

WORKDIR /app

COPY --from=builder /app/build/libs/top-leader.jar app.jar

# Cloud Run uses PORT env variable
ENV PORT=8080
EXPOSE 8080

# Run with optimizations (no CDS)
ENTRYPOINT ["java", \
    "-XX:+UseCompactObjectHeaders", \
    "-XX:+UseStringDeduplication", \
    "-XX:+DisableExplicitGC", \
    "-XX:+TieredCompilation", \
    "-XX:TieredStopAtLevel=1", \
    "-jar", "app.jar"]
