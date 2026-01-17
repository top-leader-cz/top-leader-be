# Google Cloud commands
.PHONY: login logs-qa logs-prod openapi build deploy-qa deploy-prod run local-login local-whoami local-google-auth local-api local-health local-create-user

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
	JAVA_HOME=$(HOME)/.sdkman/candidates/java/25 $(HOME)/.sdkman/candidates/gradle/current/bin/gradle build --parallel --build-cache

# Deploy to QA (local build + GitHub Actions verification)
deploy-qa: build
	git tag -d qa-deploy 2>/dev/null || true
	git push origin :refs/tags/qa-deploy 2>/dev/null || true
	git tag qa-deploy
	git push origin qa-deploy

# Deploy to PROD (creates release-v*.*.* tag, same pattern as frontend)
deploy-prod: build
	@LATEST=$$(git tag -l "release-v*.*.*" | sort -V | tail -n1); \
	if [ -z "$$LATEST" ]; then \
		NEW_TAG="release-v0.0.1"; \
	else \
		MAJOR=$$(echo $$LATEST | sed 's/release-v//' | cut -d. -f1); \
		MINOR=$$(echo $$LATEST | sed 's/release-v//' | cut -d. -f2); \
		PATCH=$$(echo $$LATEST | sed 's/release-v//' | cut -d. -f3); \
		NEW_PATCH=$$((PATCH + 1)); \
		NEW_TAG="release-v$$MAJOR.$$MINOR.$$NEW_PATCH"; \
	fi; \
	echo "Creating tag: $$NEW_TAG"; \
	git tag "$$NEW_TAG"; \
	git push origin "$$NEW_TAG"

