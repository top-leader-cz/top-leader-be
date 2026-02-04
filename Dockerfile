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

# Stage 2: Create custom JRE with jlink
FROM eclipse-temurin:25-jdk AS jre-builder

# Modules required for Spring Boot app
ENV MODULES="java.base,java.compiler,java.desktop,java.net.http,java.sql,java.naming,java.management,java.instrument,java.security.sasl,jdk.crypto.ec,jdk.unsupported"

RUN jlink \
    --add-modules $MODULES \
    --strip-debug \
    --no-man-pages \
    --no-header-files \
    --compress=zip-6 \
    --output /custom-jre

# Stage 3: Runtime with minimal custom JRE (distroless)
FROM gcr.io/distroless/base-debian12

# Copy custom JRE
COPY --from=jre-builder /custom-jre /opt/java

# Copy application
WORKDIR /app
COPY --from=builder /app/build/libs/top-leader.jar app.jar

ENV JAVA_HOME=/opt/java
ENV PATH="$JAVA_HOME/bin:$PATH"
ENV PORT=8080
EXPOSE 8080

# Run with optimizations for 512Mi Cloud Run
ENTRYPOINT ["/opt/java/bin/java", \
    "-XX:MaxRAMPercentage=70.0", \
    "-XX:InitialRAMPercentage=70.0", \
    "-XX:+UseG1GC", \
    "-XX:+UseCompactObjectHeaders", \
    "-XX:+UseStringDeduplication", \
    "-XX:+TieredCompilation", \
    "-XX:TieredStopAtLevel=1", \
    "-jar", "app.jar"]
