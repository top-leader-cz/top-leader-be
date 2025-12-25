# ============================================
# Stage 1: Build native image with GraalVM
# ============================================
FROM ghcr.io/graalvm/native-image-community:25-ol9 AS builder

# Install Maven
RUN microdnf install -y maven && microdnf clean all

WORKDIR /build

# Copy only pom.xml first for dependency caching
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build native image with AOT processing
RUN mvn -Pnative native:compile -DskipTests

# ============================================
# Stage 2: Minimal runtime image
# ============================================
FROM gcr.io/distroless/base-nossl-debian12:nonroot

WORKDIR /app

# Copy native binary from builder
COPY --from=builder /build/target/top-leader /app/top-leader

# App Engine Flexible uses port 8080
EXPOSE 8080

# Environment variables
ENV PORT=8080

# Run the native application
ENTRYPOINT ["/app/top-leader"]
