# Postman Collection

This directory contains the Postman collection for TopLeader API.

## Generate Collection

```bash
make postman
```

This will:
1. Generate OpenAPI spec at `src/main/resources/static/openapi.yaml`
2. Convert it to Postman collection at `postman/top-leader.postman_collection.json`

## Import into Postman

1. Open Postman
2. Click **Import** button
3. Select `top-leader.postman_collection.json`
4. Collection will appear in your workspace

## Configure Environment

Set the `baseUrl` variable:

**Local Development:**
```
baseUrl = http://localhost:8080
```

**QA Environment:**
```
baseUrl = https://qa.topleader.com
```

**Production:**
```
baseUrl = https://api.topleader.com
```

## Authentication

Most endpoints require authentication. You'll need to:

1. Login via `/api/login` endpoint
2. Copy the session cookie
3. Add to subsequent requests

Or use Postman's cookie management to handle this automatically.

## Collection Structure

The collection is organized by API tags:
- **User** - User management endpoints
- **Session** - Coaching session endpoints
- **Credit** - Credit management
- **Admin** - Admin operations
- **Feedback** - Feedback endpoints
- etc.

Each request includes:
- Example request body (where applicable)
- Query parameters with descriptions
- Expected response format

## Tips

1. **Save responses** - Use Postman examples to save successful responses
2. **Use variables** - Create environment variables for common values
3. **Create test scenarios** - Chain requests together with Postman tests
4. **Share with team** - Export collection and share with QA/developers

## Regenerate Collection

The collection should be regenerated whenever the API changes:

```bash
# After adding new endpoints or modifying existing ones
make postman
```

This ensures the Postman collection stays in sync with the actual API.
