# Top Leader Backend

Backend service for the Top Leader platform - a coaching and leadership development platform.

## Tech Stack

- **Java 25**
- **Spring Boot 4.0.1**
- **PostgreSQL 15**
- **Flyway** - database migrations
- **Spring AI 2.0.0-M1** - OpenAI integration
- **Spring Security** - authentication and authorization
- **Spring Data JPA** - persistence
- **Google Cloud Platform** - Storage, Calendar API, OAuth2
- **OpenTelemetry** - observability
- **Swagger/OpenAPI** - API documentation (springdoc-openapi)
- **GraalVM Native Image** - native compilation support

## Project Structure

```
src/main/java/com/topleader/topleader/
├── admin/              # Admin functionality
├── ai/                 # AI integration (OpenAI)
├── authentication/     # Authentication
├── calendar/           # Calendars (Google, Calendly)
├── coach/              # Coach functionality
│   ├── availability/   # Coach availability
│   ├── client/         # Coach clients
│   ├── favorite/       # Favorite coaches
│   ├── note/           # Notes
│   ├── rate/           # Ratings
│   └── session/        # Coaching sessions
├── company/            # Company management
├── configuration/      # Application configuration
├── credit/             # Credit system
├── email/              # Email services (Velocity templates)
├── feedback/           # Feedback
├── history/            # Activity history
├── hr/                 # HR features
├── message/            # Messages
├── myteam/             # Team functionality
├── notification/       # Notifications
├── password/           # Password management
├── report/             # Reports
├── scheduled_session/  # Scheduled sessions
├── user/               # Users
│   ├── assessment/     # User assessments
│   ├── badge/          # Badges
│   ├── session/        # User sessions
│   ├── settings/       # Settings
│   └── userinsight/    # User insights
└── util/               # Utility classes
```

## Requirements

- Java 25
- Maven 3.9+
- Docker (for local database)

## Local Development

### Start Database

```bash
docker-compose up -d
```

Database runs on port `5434` with credentials:
- User: `root`
- Password: `postgres`
- Database: `top_leader`

### Build and Run

```bash
./mvnw spring-boot:run
```

### Tests

```bash
# Unit tests
./mvnw test

# Integration tests (require Docker for Testcontainers)
./mvnw verify
```

## API Documentation

After starting the application, Swagger UI is available at:
```
http://localhost:8080/swagger-ui.html
```

## Build

```bash
# Standard build
./mvnw clean package

# Native image (GraalVM)
./mvnw -Pnative native:compile
```
