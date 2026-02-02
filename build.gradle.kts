plugins {
    java
    jacoco
    id("org.springframework.boot") version "4.0.2"
    id("io.spring.dependency-management") version "1.1.7"
    id("io.freefair.lombok") version "9.2.0"
    id("org.graalvm.buildtools.native") version "0.10.6"
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
        mavenBom("org.springframework.ai:spring-ai-bom:2.0.0-M2")
        mavenBom("org.testcontainers:testcontainers-bom:1.21.4")
        mavenBom("org.springframework.modulith:spring-modulith-bom:2.0.2")
    }
}

dependencies {
    // Google Cloud
    implementation("com.google.apis:google-api-services-calendar:v3-rev20251207-2.0.0")
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
    implementation("org.springframework.boot:spring-boot-starter-opentelemetry")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.springframework.session:spring-session-jdbc")

    // Spring AI
    implementation("org.springframework.ai:spring-ai-starter-model-openai")

    // Database
    implementation("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-database-postgresql")

    // H2 for AOT processing and tests only
    // Excluded from production JAR via bootJar configuration below
    runtimeOnly("com.h2database:h2")


    // Logging (JSON formatting handled by Spring Boot structured logging)
    implementation("org.springframework.boot:spring-boot-starter-log4j2")

    // Utilities
    implementation("dev.failsafe:failsafe:3.3.2")

    // Conditional SpringDoc dependency: UI for dev/qa, API-only for production
    val includeSwaggerUI = project.findProperty("swagger.ui") != "false"
    if (includeSwaggerUI) {
        implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.1")
    } else {
        implementation("org.springdoc:springdoc-openapi-starter-webmvc-api:3.0.1")
    }
    println("SpringDoc mode: ${if (includeSwaggerUI) "UI included" else "API-only (production)"}")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.icegreen:greenmail:2.1.8")
    testImplementation("net.javacrumbs.json-unit:json-unit-assertj:3.2.7")
    testImplementation("org.wiremock:wiremock-standalone:3.10.0")
    testImplementation("org.springframework.modulith:spring-modulith-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("--add-opens", "java.base/java.time=ALL-UNNAMED")
    maxHeapSize = "1024m"
    exclude("**/OpenApiGeneratorTest.class")
    exclude("**/ModularityTests.class")
    finalizedBy(tasks.jacocoTestReport) // Generate coverage report after tests
}

// JaCoCo configuration
tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    // Exclude DTOs, domain models, configs, and Lombok-generated code
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude(
                    "**/domain/**",
                    "**/*\$*Dto.class",
                    "**/*\$*Request*.class",
                    "**/*\$*Response*.class",
                    "**/*Dto.class",
                    "**/*Request.class",
                    "**/*Response.class",
                    "**/configuration/**",
                    "**/*Configuration.class",
                    "**/*Config.class",
                    "**/exception/**",
                    "**/*Exception.class",
                    "**/TopLeaderApplication.class",
                    "**/*\$*Builder.class"
                )
            }
        })
    )
}

// Verify minimum coverage threshold
tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal() // 80% minimum coverage
            }
        }
    }
    // Use same exclusions as jacocoTestReport
    classDirectories.setFrom(tasks.jacocoTestReport.get().classDirectories)
}

tasks.bootJar {
    archiveFileName.set("top-leader.jar")
    // Exclude H2 from production JAR (only needed for AOT processing and tests)
    // H2 is filtered from classpath dependencies
    val h2Excluded = configurations.runtimeClasspath.get().filter {
        !it.name.contains("h2-")
    }
    classpath(h2Excluded)
}

// OpenAPI spec generation
val generateOpenApi by tasks.registering(Test::class) {
    description = "Generates OpenAPI spec."
    group = "documentation"
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    // Remove the exclude rule from parent Test configuration
    setExcludes(emptySet())
    filter {
        includeTestsMatching("com.topleader.topleader.OpenApiGeneratorTest")
    }
    shouldRunAfter(tasks.test)
}

// GraalVM Native Image configuration
graalvmNative {
    binaries {
        named("main") {
            imageName.set("top-leader")
            mainClass.set("com.topleader.topleader.TopLeaderApplication")

            val baseArgs = mutableListOf(
                "-H:+ReportExceptionStackTraces",
                "-Ob",  // Quick build - faster compilation, slower runtime
                "-J-Xmx10g",  // More heap for faster build
                listOf(
                    "org.slf4j",
                    "org.apache.logging.slf4j",
                    "org.apache.logging.log4j",
                    "org.apache.commons.logging",
                    "org.springframework.boot.logging",
                    "org.springframework.boot.ansi",
                    "com.fasterxml.jackson",
                    "org.yaml.snakeyaml",
                    "org.codehaus.stax2",
                    "com.ctc.wstx"
                ).joinToString(",", prefix = "--initialize-at-build-time=")
            )

            // Add CPU optimizations if specified (e.g., -Pnative.march=x86-64-v3)
            val march = project.findProperty("native.march") as String?
            if (march != null) {
                baseArgs.add("-march=$march")
            }

            buildArgs.addAll(baseArgs)
            // Exclude H2 from native image - only needed for AOT processing
            classpath = classpath.filter { !it.name.startsWith("h2-") }
        }
    }
    toolchainDetection.set(false)
}

// Configure AOT processing to use H2 (only for AOT hint generation, not baked into runtime)
tasks.named<org.springframework.boot.gradle.tasks.aot.ProcessAot>("processAot") {
    args(
        "--spring.datasource.url=jdbc:h2:mem:aot;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "--spring.datasource.username=sa",
        "--spring.datasource.password=",
        "--spring.datasource.driver-class-name=org.h2.Driver",
        "--spring.flyway.enabled=false",
        "--spring.ai.openai.api-key=dummy-key"
    )
}

// Configure Test AOT processing with H2 and exclude TestContainers-based tests
tasks.named<org.springframework.boot.gradle.tasks.aot.ProcessTestAot>("processTestAot") {
    args(
        "--spring.datasource.url=jdbc:h2:mem:aot;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "--spring.datasource.username=sa",
        "--spring.datasource.password=",
        "--spring.datasource.driver-class-name=org.h2.Driver",
        "--spring.flyway.enabled=false",
        "--spring.ai.openai.api-key=dummy-key"
    )
}
