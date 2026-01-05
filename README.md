# TopLeader Backend

Backend application for the TopLeader platform - a coaching and mentoring platform that connects coaches with users, manages scheduling, feedback, credits, and team collaboration.

## Tech Stack

- **Java 25** with Spring Boot 4.0.1
- **PostgreSQL 15** database
- **Spring Data JPA** with Hibernate 7.x
- **Spring Security** (session-based authentication)
- **Spring AI** with OpenAI integration
- **Spring Session JDBC** for distributed sessions
- **Flyway** for database migrations
- **Lombok** 1.18.42 for boilerplate reduction
- **Maven** for build management
- **Docker Compose** for local development
- **Virtual Threads** enabled (Java 21+ feature)
- **OpenTelemetry** for observability

## Prerequisites

- **Java 25** (managed via SDKMAN recommended)
- **Maven 3.x**
- **Docker & Docker Compose** (for PostgreSQL)
- **Git**

## Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd top-leader-be
```

### 2. Start PostgreSQL Database

The project uses Docker Compose to run PostgreSQL locally:

```bash
docker-compose up -d
```

This starts PostgreSQL 15 on port `5434` with:
- Database: `top_leader`
- Username: `root`
- Password: `postgres`

### 3. Configure Environment

The application uses `src/main/resources/application.yml` for configuration. For local development, the defaults work out of the box.

Required external service configurations (add to environment or application.yml):
- **OpenAI API Key** (for AI features)
- **Google OAuth** credentials (for calendar integration)
- **Calendly** credentials (for scheduling integration)
- **GCP Storage** credentials (for file storage)

### 4. Build the Application

```bash
JAVA_HOME=~/.sdkman/candidates/java/25 mvn clean compile
```

### 5. Run Database Migrations

Flyway migrations run automatically on application startup. Current migration version: **V0.0.1.84**

### 6. Run the Application

```bash
JAVA_HOME=~/.sdkman/candidates/java/25 mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Documentation

Interactive API documentation is available via Swagger UI when the application is running:

**Swagger UI:** `http://localhost:8080/swagger-ui.html`

## Project Structure

```
src/main/java/com/topleader/topleader/
├── admin/                  # Admin views and management
├── ai/                     # AI integrations (OpenAI)
├── calendar/               # Calendar integrations (Google, Calendly)
├── coach/                  # Coach profiles and availability
├── company/                # Company entity and management
├── configuration/          # Security, async, and app configs
├── credit/                 # Credit system management
├── email/                  # Email sending (SMTP)
├── exception/              # Custom exceptions
├── feedback/               # Feedback forms and responses
├── feedback_notification/  # Feedback notification handling
├── history/                # History tracking
├── hr/                     # HR user management
├── ical/                   # iCal format handling
├── message/                # User messaging system
├── myteam/                 # Team management
├── notification/           # Notification system
├── password/               # Password management
├── session/                # Session management
├── user/                   # Core user entity and features
└── util/                   # Utilities and converters

src/main/resources/
├── db/migration/0.0.1/     # Flyway database migrations (86 files)
├── application.yml         # Main configuration file
└── templates/              # Email templates (Velocity)
```

## Key Features

### Authentication & Authorization
- Session-based authentication with Spring Security
- Role-based access control (RBAC)
- Roles: `RESPONDENT`, `USER`, `MANAGER`, `COACH`, `HR`, `ADMIN`

### User Management
- User profiles and preferences
- Team management
- HR admin capabilities
- Coach-coachee relationships

### Session Management
- Scheduled coaching sessions
- Calendar integrations (Google Calendar, Calendly)
- iCal format support
- Session history and tracking

### Credit System
- Credit allocation and management
- Company package management
- User allocation tracking

### Feedback System
- Feedback forms and responses
- Feedback notifications
- Summary generation

### AI Integration
- OpenAI integration via Spring AI
- AI-powered features
- Image generation support

### External Integrations
- Google OAuth and Calendar API
- Calendly scheduling API
- GCP Cloud Storage
- SMTP email sending

## Testing

### Run All Tests

```bash
JAVA_HOME=~/.sdkman/candidates/java/25 mvn test
```

### Run Specific Test Class

```bash
JAVA_HOME=~/.sdkman/candidates/java/25 mvn test -Dtest=ClassName
```

### Run Integration Tests

```bash
JAVA_HOME=~/.sdkman/candidates/java/25 mvn verify
```

The test suite uses:
- **Spring Boot Test** with `@SpringBootTest`
- **Testcontainers** for PostgreSQL
- **GreenMail** for email testing
- **WireMock** for external API mocking
- **JSON Unit** for JSON assertions

Test data is loaded from `src/test/resources/sql/` scripts.

## Build & Deployment

### Build Without Tests

```bash
JAVA_HOME=~/.sdkman/candidates/java/25 mvn clean package -DskipTests
```

The build produces `target/top-leader.jar`

### Docker Image

The project includes Spring Boot buildpack support for creating OCI images.

## Database Migrations

Flyway migrations are located in `src/main/resources/db/migration/0.0.1/`

Naming convention: `V0.0.1.{number}__{description}.sql`

Current latest migration: **V0.0.1.84__scheduled_session_updated_by.sql**

Total migrations: **86 files**

Migrations run automatically on application startup.

## Configuration

Key configuration properties in `application.yml`:

- **Database**: PostgreSQL on `localhost:5434`
- **Server Port**: `8080`
- **Virtual Threads**: Enabled
- **Docker Compose**: Auto-enabled for local development
- **Flyway**: Auto-migration enabled
- **JPA**: Validate mode with lazy initialization
- **Session**: JDBC-backed sessions
- **Actuator**: Health, metrics, and Prometheus endpoints exposed

## Monitoring & Observability

- **Spring Boot Actuator** endpoints: `/actuator/health`, `/actuator/metrics`, `/actuator/prometheus`
- **OpenTelemetry** integration for traces and metrics
- **Grafana Cloud** support (configured via environment)

## Code Style & Patterns

- Use `var` for local variables
- Records for DTOs
- Lombok annotations (`@Data`, `@Accessors(chain = true)`)
- Jakarta validation (`@NotNull`, `@Valid`)
- Entity pattern with `@Entity` and audit fields
- Repository pattern with `JpaRepository`
- Controller pattern with `@RestController` and `/api/latest/*` paths
- Secured endpoints with `@Secured` annotations
- Prefer streams over loops
- No JavaDoc above methods

## License

[Add license information here]

## Contributing

[Add contributing guidelines here]