# ADR-002: Server-Side Sessions over JWT

**Status:** Accepted
**Date:** 2024-01-15
**Decision Makers:** Development Team

## Context

We needed to choose an authentication mechanism for the TopLeader platform. The main options were:
1. JWT (JSON Web Tokens) - stateless, client-side tokens
2. Server-side sessions - stateful, stored in database
3. OAuth 2.0 with external provider only

## Decision

We chose **Spring Session JDBC** (server-side sessions stored in PostgreSQL).

## Rationale

### Simplicity
- No token lifecycle management (refresh tokens, rotation)
- No JWT signing/verification overhead
- No token blacklisting for logout
- Session invalidation is instant and reliable

### Security
- Tokens can be revoked immediately (logout works instantly)
- No token theft risk from localStorage/cookies
- Session data stays on server (not exposed to client)
- Smaller attack surface

### Already Have a Database
- PostgreSQL is already part of our stack
- No additional infrastructure (Redis) needed
- Transactions and backups included

### Monolith-Friendly
- Single application = no need for stateless tokens
- No service-to-service authentication needed
- Session affinity is simple with single app

### Debugging
- Easy to inspect sessions in database
- Can query active sessions, last activity
- Simple to implement "logout all devices"

## Consequences

### Positive
- Simpler codebase
- Instant logout
- Easy session management (view, revoke)
- No token size overhead in requests

### Negative
- Database lookup per request (mitigated by caching)
- Horizontal scaling requires shared session store (we use JDBC)
- Not suitable for mobile apps with offline mode (not our use case)

### Mitigations
- Spring Session with JDBC handles distributed sessions automatically
- Database connection pooling minimizes overhead
- Session caching can be added if needed

## Alternatives Considered

### JWT Tokens
Rejected because:
- Added complexity (refresh tokens, blacklisting)
- Logout doesn't work until token expires
- Token theft is harder to mitigate
- Larger payload in every request

### Redis Sessions
Rejected because:
- Additional infrastructure to manage
- Already have PostgreSQL
- Redis not needed for our scale

### OAuth 2.0 Only (External Provider)
Partially adopted:
- Google OAuth for calendar integration
- But primary auth is still our own sessions

## Implementation Details

```yaml
spring:
  session:
    store-type: jdbc
    jdbc:
      initialize-schema: never  # Flyway handles schema
```

Session table is created by Flyway migration and managed by Spring Session.

## References

- [Stop Using JWT for Sessions](http://cryto.net/~joepie91/blog/2016/06/13/stop-using-jwt-for-sessions/)
- [Spring Session Documentation](https://docs.spring.io/spring-session/reference/)
