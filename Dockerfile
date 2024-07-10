# First stage: JDK with GraalVM
FROM ghcr.io/graalvm/native-image-community:21 AS build

# Install Maven
COPY apache-maven pom.xml /opt/maven/
COPY apache-maven/bin/mvn /usr/bin/mvn

# Set Maven environment variables
ENV MAVEN_HOME=/opt/maven
ENV PATH=$MAVEN_HOME/bin:$PATH

WORKDIR /usr/src/app

COPY pom.xml pom.xml

# RUN mvn dependency:go-offline

COPY src src

RUN export MAVEN_OPTS="-Xms12g -Xmx16g -XX:MaxDirectMemorySize=8192m"

RUN export spring_profiles_active=qa

RUN mvn clean package -Pnative -DskipTests -Dmaven.test.skip=true

# Second stage: Lightweight debian-slim image
FROM alpine:3.19

WORKDIR /app

RUN apk add gcompat
# Copy the native binary from the build stage
COPY --from=build /usr/src/app/target/top-leader /app/top-leader

# Run the application
CMD ["/app/top-leader"]