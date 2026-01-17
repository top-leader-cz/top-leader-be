# Google Cloud commands
.PHONY: login logs-qa logs-prod openapi build deploy-qa run local-login local-whoami local-google-auth local-api local-health local-create-user

# Login to Google Cloud and set project
login:
	gcloud auth login topleaderplatform@gmail.com
	gcloud config set project topleader-394306

# Fetch QA logs (last 1 hour, errors only)
logs-qa:
	gcloud logging read 'resource.type="gae_app" AND resource.labels.module_id="qa" AND severity>=ERROR' --limit=50 --format="table(timestamp,severity,textPayload)"

# Fetch QA logs with AI/OpenAI filter
logs-qa-ai:
	gcloud logging read 'resource.type="gae_app" AND resource.labels.module_id="qa" AND (textPayload=~"openai|OpenAI|ChatModel|AiClient" OR textPayload=~"Exception|Error")' --limit=100 --format="json"

# Fetch prod logs
logs-prod:
	gcloud logging read 'resource.type="gae_app" AND resource.labels.module_id="default" AND severity>=ERROR' --limit=50 --format="table(timestamp,severity,textPayload)"

# Generate OpenAPI spec
openapi:
	$(HOME)/.sdkman/candidates/gradle/current/bin/gradle generateOpenApi

# Build application locally
build:
	$(HOME)/.sdkman/candidates/gradle/current/bin/gradle build --parallel --build-cache

# Deploy to QA (local build + GitHub Actions verification)
deploy-qa:
	git tag -d qa-deploy 2>/dev/null || true
	git push origin :refs/tags/qa-deploy 2>/dev/null || true
	git tag qa-deploy
	git push origin qa-deploy

# =============================================================================
# LOCAL DEVELOPMENT
# =============================================================================

# Run application locally
run:
	$(HOME)/.sdkman/candidates/gradle/current/bin/gradle bootRun

# Login locally (saves session cookie to /tmp/cookies.txt)
# Usage: make local-login USER=user@example.com PASS=password
local-login:
	@echo "Logging in as $(USER)..."
	@curl -v -c /tmp/cookies.txt -X POST "http://localhost:8080/login" \
		-H "Content-Type: application/x-www-form-urlencoded" \
		-d "username=$(USER)&password=$(PASS)" 2>&1 | grep -E "(HTTP|Set-Cookie|Location)"
	@echo "\nSession saved to /tmp/cookies.txt"

# Check if logged in
local-whoami:
	@curl -s -b /tmp/cookies.txt "http://localhost:8080/api/latest/user-info" | head -100

# Start Google OAuth flow (opens browser)
local-google-auth:
	@echo "Opening Google OAuth in browser..."
	@open "http://localhost:8080/login/google"

# Test API endpoint (with session)
# Usage: make local-api ENDPOINT=/api/latest/user-info
local-api:
	@curl -s -b /tmp/cookies.txt "http://localhost:8080$(ENDPOINT)" | python3 -m json.tool 2>/dev/null || curl -s -b /tmp/cookies.txt "http://localhost:8080$(ENDPOINT)"

# Health check
local-health:
	@curl -s "http://localhost:8080/actuator/health" | python3 -m json.tool

# Create test user in local DB (password: test123)
# BCrypt hash for "test123"
DB_CONTAINER ?= my-postgres-db
DB_USER ?= root
local-create-user:
	@echo "Creating test user: test@test.com / test123"
	@docker exec -i $(DB_CONTAINER) psql -U $(DB_USER) -d top_leader -c " \
		INSERT INTO users (username, first_name, last_name, password, time_zone, status) \
		VALUES ('test@test.com', 'Test', 'User', '\$$2a\$$12\$$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4beRb6R3PLbQFmSy', 'Europe/Prague', 'AUTHORIZED') \
		ON CONFLICT (username) DO NOTHING;"
	@echo "Done! Login with: make local-login USER=test@test.com PASS=test123"
