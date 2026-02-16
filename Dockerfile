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
RUN ./gradlew clean bootJar -x test --no-daemon

# Stage 2: Runtime with pre-built custom JRE
FROM europe-west3-docker.pkg.dev/topleader-394306/top-leader/topleader-jre:latest

# Copy application
WORKDIR /app
COPY --from=builder /app/build/libs/top-leader.jar app.jar

ENV JAVA_HOME=/opt/java
ENV PATH="$JAVA_HOME/bin:$PATH"
ENV PORT=8080
EXPOSE 8080

# Run with optimizations for Cloud Run
ENTRYPOINT ["/opt/java/bin/java", \
    "-Dspring.aot.enabled=false", \
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
