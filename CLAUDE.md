# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot 3.4.3 application using Java 21 that serves as a prototype and testing service for Groupon's internal platform updates. The application demonstrates integration with multiple services including PostgreSQL, Redis, MBus (message bus), and external APIs.

## Key Commands

### Build and Test
- `mvn clean package` - Build the application
- `mvn clean verify` - Run all tests, integration tests, and code quality checks
- `mvn test` - Run unit tests only
- `mvn spring-boot:run` - Run the application locally

### Dependencies
- `docker compose up postgres mbus redis` - Start required services
- Requires Java 21 and Maven (preferably via sdkman)

### Development
- Application runs on port 8080 by default
- Debug port 5005 available for remote debugging
- Uses Spring DevTools for hot reloading




## Development Conventions

- Use constructor injection over field injection
- Follow Spring Boot naming conventions (*Controller, *Service, *Repository)
- Use SLF4J for logging with Lombok's @Slf4j
- Externalize configuration in application.yml
- Use Spring profiles for environment-specific configurations
- Use Lombok annotations but prefer explicit over @Data

## Testing Strategy
- Unit tests in `src/test/java/`
- Integration tests using `@SpringBootTest`
- Abstract base class: `IntegrationTest.java`
- Test configuration: `TestBeanConfiguration.java`
- Separate test application.yml configuration


## Spring Boot Rules
# structure
src/main/java/         # Main Java source code
src/main/resources/    # Application resources
src/test/java/         # Test source code
pom.xml                # Maven build file
com.groupon.spring.oxygen # base java package

# naming
Controller: *Controller.java
Service: *Service.java
Repository: *Repository.java
Configuration:  *Configuration.java

# conventions
- Use @RestController for API endpoints
- Use @Service for business logic
- Use @Repository for data access
- Application entry point must be in a class annotated with @SpringBootApplication
- Use Lombok annotations (e.g., @Getter, @Setter, @Builder, @Data, @RequiredArgsConstructor) to reduce boilerplate prefer explicit annotations for clarity
- Do not use @Builder annotation
- Use @RequiredArgsConstructor instead of @Autowire only in case of *Service and *Controller
- Prioritize domain  packaging like Order domain should contain all the code related to Orders @RestController, @Service @Entity etc...
- Do not use @Repository annotation


# dependencies
- Use Maven for dependency management (pom.xml must exist)
- Java version: 21

# ports
- Application should run on port 8080 by default