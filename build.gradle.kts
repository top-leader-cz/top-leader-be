plugins {
    java
    id("org.springframework.boot") version "4.0.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("io.freefair.lombok") version "9.2.0"
}

group = "com.topleader"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
    all {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
        exclude(group = "ch.qos.logback", module = "logback-classic")
        exclude(group = "ch.qos.logback", module = "logback-core")
    }
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:2.0.0-M1")
        mavenBom("com.google.cloud:spring-cloud-gcp-dependencies:7.4.2")
        mavenBom("org.testcontainers:testcontainers-bom:1.21.4")
    }
}

dependencies {
    // Google Cloud
    implementation("com.google.cloud:spring-cloud-gcp-starter-storage") {
        exclude(group = "commons-logging", module = "commons-logging")
        exclude(group = "com.google.cloud", module = "google-cloud-monitoring")
        exclude(group = "com.google.api.grpc", module = "proto-google-cloud-monitoring-v3")
        exclude(group = "com.google.cloud.opentelemetry", module = "exporter-metrics")
    }
    implementation("com.google.apis:google-api-services-calendar:v3-rev20250404-2.0.0")
    implementation("com.google.apis:google-api-services-oauth2:v2-rev20200213-2.0.0")
    implementation("com.google.api-client:google-api-client:2.6.0") {
        exclude(group = "commons-logging", module = "commons-logging")
    }
    implementation("com.google.auth:google-auth-library-oauth2-http:1.41.0")
    implementation("com.google.cloud.sql:postgres-socket-factory:1.28.0")

    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
    }
    implementation("org.springframework.boot:spring-boot-starter-jetty") {
        exclude(group = "org.eclipse.jetty.ee11.websocket", module = "jetty-ee11-websocket-jakarta-server")
        exclude(group = "org.eclipse.jetty.ee11.websocket", module = "jetty-ee11-websocket-jetty-server")
        exclude(group = "org.eclipse.jetty.ee11", module = "jetty-ee11-plus")
        exclude(group = "org.apache.tomcat.embed", module = "tomcat-embed-el")
    }
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-opentelemetry")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.springframework.session:spring-session-jdbc")

    // Spring AI
    implementation("org.springframework.ai:spring-ai-starter-model-openai")

    // Database
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-database-postgresql")

    // Logging
    implementation("org.springframework.boot:spring-boot-starter-log4j2")
    implementation("org.apache.logging.log4j:log4j-layout-template-json:2.24.3")

    // Utilities
    implementation("dev.failsafe:failsafe:3.3.2")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.1")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.icegreen:greenmail:2.1.8")
    testImplementation("net.javacrumbs.json-unit:json-unit-assertj:3.2.7")
    testImplementation("org.wiremock:wiremock-standalone:3.10.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("--add-opens", "java.base/java.time=ALL-UNNAMED")
    maxHeapSize = "1024m"
    exclude("**/OpenApiGeneratorTest.class")
}

tasks.bootJar {
    archiveFileName.set("top-leader.jar")
}

// Integration tests
val integrationTest by tasks.registering(Test::class) {
    description = "Runs integration tests."
    group = "verification"
    include("**/*IT.class")
    shouldRunAfter(tasks.test)
}

// OpenAPI spec generation
val generateOpenApi by tasks.registering(Test::class) {
    description = "Generates OpenAPI spec."
    group = "documentation"
    include("**/OpenApiGeneratorTest.class")
    shouldRunAfter(tasks.test)
}
