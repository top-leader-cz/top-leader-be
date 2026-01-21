# Configuration variables
REGION := europe-west3
PROJECT_ID := topleader-394306
SERVICE_NAME := top-leader-qa
IMAGE_NAME := top-leader-be
ARTIFACT_REPO := cloud-run
GRADLE_HOME := $(HOME)/.sdkman/candidates/gradle/current

# Google Cloud commands
.PHONY: login logs-qa logs-qa-ai logs-prod openapi build native native-test native-linux deploy-qa deploy-prod
.PHONY: logs-qa-cloudrun logs-qa-cloudrun-ai info-qa-cloudrun revisions-qa rollback-qa jib-local setup-cloud-run-qa

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

# Build native image with GraalVM (quick mode ~2-3 min)
native:
	JAVA_HOME=$(HOME)/.sdkman/candidates/java/25g $(HOME)/.sdkman/candidates/gradle/current/bin/gradle nativeCompile --no-configuration-cache --build-cache

# Run native tests (compiles and runs tests in native image)
native-test:
	JAVA_HOME=$(HOME)/.sdkman/candidates/java/25g $(HOME)/.sdkman/candidates/gradle/current/bin/gradle nativeTest --no-configuration-cache --build-cache

# Build native image optimized for Linux x86-64 (for deployment)
native-linux:
	JAVA_HOME=$(HOME)/.sdkman/candidates/java/25g $(HOME)/.sdkman/candidates/gradle/current/bin/gradle nativeCompile --no-configuration-cache --build-cache -Pnative.march=x86-64-v3

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

# --- Cloud Run Commands ---

# Setup Cloud Run infrastructure (Artifact Registry, Service Account, IAM)
setup-cloud-run-qa:
	./scripts/setup-cloud-run-qa.sh

# Build and push Docker image locally using Jib
jib-local: build
	@echo "Building and pushing Docker image using Jib..."
	$(GRADLE_HOME)/bin/gradle jib \
		-Djib.to.tags=latest,local \
		-Djib.console=plain \
		--no-configuration-cache

# Fetch Cloud Run QA error logs
logs-qa-cloudrun:
	gcloud logging read 'resource.type="cloud_run_revision" AND resource.labels.service_name="$(SERVICE_NAME)" AND severity>=ERROR' \
		--limit=50 \
		--format="table(timestamp,severity,textPayload)" \
		--project=$(PROJECT_ID)

# Fetch Cloud Run QA AI-related logs
logs-qa-cloudrun-ai:
	gcloud logging read 'resource.type="cloud_run_revision" AND resource.labels.service_name="$(SERVICE_NAME)" AND (textPayload=~"openai|OpenAI|ChatModel|AiClient" OR textPayload=~"Exception|Error")' \
		--limit=100 \
		--format=json \
		--project=$(PROJECT_ID)

# Show Cloud Run service information
info-qa-cloudrun:
	gcloud run services describe $(SERVICE_NAME) \
		--region=$(REGION) \
		--platform=managed \
		--project=$(PROJECT_ID)

# List Cloud Run revisions
revisions-qa:
	gcloud run revisions list \
		--service=$(SERVICE_NAME) \
		--region=$(REGION) \
		--platform=managed \
		--sort-by=~metadata.creationTimestamp \
		--project=$(PROJECT_ID)

# Rollback to previous Cloud Run revision
rollback-qa:
	@echo "Available revisions for $(SERVICE_NAME):"
	@gcloud run revisions list \
		--service=$(SERVICE_NAME) \
		--region=$(REGION) \
		--platform=managed \
		--format="table(metadata.name,status.traffic,status.conditions[0].status)" \
		--project=$(PROJECT_ID)
	@echo ""
	@read -p "Enter revision name to rollback to: " REVISION; \
	gcloud run services update-traffic $(SERVICE_NAME) \
		--to-revisions=$$REVISION=100 \
		--region=$(REGION) \
		--platform=managed \
		--project=$(PROJECT_ID)

