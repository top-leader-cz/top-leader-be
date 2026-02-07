# Load local configuration (gitignored)
-include Makefile.local

# Configuration variables (defaults - override in Makefile.local)
REGION ?= your-region
PROJECT_ID ?= your-project-id
SERVICE_NAME ?= your-service-name
GCLOUD_ACCOUNT ?= your-email@example.com
IMAGE_NAME := top-leader-be
ARTIFACT_REPO := cloud-run
GRADLE_HOME := $(HOME)/.sdkman/candidates/gradle/current

# Google Cloud commands
.PHONY: login logs-qa logs-qa-ai logs-prod openapi build native native-linux deploy-qa deploy-prod
.PHONY: info-qa revisions-qa rollback-qa redeploy-qa setup-cloud-run-qa jre-build jre-push
.PHONY: info-prod revisions-prod rollback-prod redeploy-prod

# Login to Google Cloud and set project
login:
	gcloud auth login $(GCLOUD_ACCOUNT)
	gcloud config set project $(PROJECT_ID)

# Fetch QA logs (Cloud Run, errors only)
logs-qa:
	gcloud logging read 'resource.type="cloud_run_revision" AND resource.labels.service_name="$(SERVICE_NAME)" AND severity>=ERROR' \
		--limit=50 \
		--format="table(timestamp,severity,textPayload)" \
		--project=$(PROJECT_ID)

# Fetch prod logs (Cloud Run)
logs-prod:
	gcloud logging read 'resource.type="cloud_run_revision" AND resource.labels.service_name="top-leader-prod" AND severity>=ERROR' \
		--limit=50 \
		--format="table(timestamp,severity,textPayload)" \
		--project=$(PROJECT_ID)

# Generate OpenAPI spec
openapi:
	$(HOME)/.sdkman/candidates/gradle/current/bin/gradle generateOpenApi

# Build application locally
build:
	JAVA_HOME=$(HOME)/.sdkman/candidates/java/25 $(HOME)/.sdkman/candidates/gradle/current/bin/gradle build -x processTestAot --parallel --build-cache

# Testing commands
# Run all tests
test:
	JAVA_HOME=$(HOME)/.sdkman/candidates/java/25 $(HOME)/.sdkman/candidates/gradle/current/bin/gradle test

# Run tests and generate coverage report
test-coverage:
	JAVA_HOME=$(HOME)/.sdkman/candidates/java/25 $(HOME)/.sdkman/candidates/gradle/current/bin/gradle test jacocoTestReport
	@echo "Coverage report generated at: build/reports/jacoco/test/html/index.html"

# Run tests with coverage and open report in browser
test-with-coverage: test-coverage
	@echo "Opening coverage report..."
	@open build/reports/jacoco/test/html/index.html || xdg-open build/reports/jacoco/test/html/index.html 2>/dev/null || echo "Please open build/reports/jacoco/test/html/index.html manually"

# Verify coverage meets minimum threshold (80%) - FAILS if below threshold
coverage-verify:
	JAVA_HOME=$(HOME)/.sdkman/candidates/java/25 $(HOME)/.sdkman/candidates/gradle/current/bin/gradle test jacocoTestReport jacocoTestCoverageVerification
	@echo "✅ Coverage verification passed - minimum 80% instruction coverage achieved"

# Build native image with GraalVM (quick mode ~2-3 min)
native:
	JAVA_HOME=$(HOME)/.sdkman/candidates/java/25g $(HOME)/.sdkman/candidates/gradle/current/bin/gradle nativeCompile --no-configuration-cache --build-cache

# Build native image optimized for Linux x86-64 (for deployment)
native-linux:
	JAVA_HOME=$(HOME)/.sdkman/candidates/java/25g $(HOME)/.sdkman/candidates/gradle/current/bin/gradle nativeCompile --no-configuration-cache --build-cache -Pnative.march=x86-64-v3

# Deploy to QA (local build + GitHub Actions verification)
deploy-qa: build
	git tag -d qa-deploy 2>/dev/null || true
	git push origin :refs/tags/qa-deploy 2>/dev/null || true
	git tag qa-deploy
	git push origin qa-deploy

# Deploy to PROD via GitHub Actions (creates release tag, builds in CI)
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

# Direct deploy to PROD (local Docker build + push + Cloud Run deploy, bypasses CI)
deploy-prod-direct:
	docker build --platform linux/amd64 -t $(REGION)-docker.pkg.dev/$(PROJECT_ID)/top-leader/$(IMAGE_NAME):latest .
	docker push $(REGION)-docker.pkg.dev/$(PROJECT_ID)/top-leader/$(IMAGE_NAME):latest
	gcloud run services replace src/main/cloudrun/service-prod.yaml \
		--region=$(REGION) \
		--project=$(PROJECT_ID)

# Build custom JRE image for linux/amd64
jre-build:
	docker build --platform linux/amd64 -f Dockerfile.jre -t $(REGION)-docker.pkg.dev/$(PROJECT_ID)/top-leader/topleader-jre:latest .

# Push custom JRE image to Artifact Registry
jre-push: jre-build
	docker push $(REGION)-docker.pkg.dev/$(PROJECT_ID)/top-leader/topleader-jre:latest

# --- Cloud Run Commands ---

# Setup Cloud Run infrastructure (Artifact Registry, Service Account, IAM)
setup-cloud-run-qa:
	./scripts/setup-cloud-run-qa.sh

# Show QA Cloud Run service information
info-qa:
	gcloud run services describe $(SERVICE_NAME) \
		--region=$(REGION) \
		--project=$(PROJECT_ID)

# List Cloud Run revisions
revisions-qa:
	gcloud run revisions list \
		--service=$(SERVICE_NAME) \
		--region=$(REGION) \
		--platform=managed \
		--sort-by=~metadata.creationTimestamp \
		--project=$(PROJECT_ID)

# Redeploy QA Cloud Run (force new revision to pick up secret changes)
redeploy-qa:
	gcloud run services update $(SERVICE_NAME) \
		--region=$(REGION) \
		--project=$(PROJECT_ID) \
		--update-env-vars="FORCE_REDEPLOY=$$(date +%s)"

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

# --- PROD Cloud Run Commands ---

# Show PROD Cloud Run service information
info-prod:
	gcloud run services describe top-leader-prod \
		--region=$(REGION) \
		--project=$(PROJECT_ID)

# List PROD Cloud Run revisions
revisions-prod:
	gcloud run revisions list \
		--service=top-leader-prod \
		--region=$(REGION) \
		--platform=managed \
		--sort-by=~metadata.creationTimestamp \
		--project=$(PROJECT_ID)

# Redeploy PROD Cloud Run (force new revision to pick up secret changes)
redeploy-prod:
	gcloud run services update top-leader-prod \
		--region=$(REGION) \
		--project=$(PROJECT_ID) \
		--update-env-vars="FORCE_REDEPLOY=$$(date +%s)"

# Rollback PROD to previous Cloud Run revision
rollback-prod:
	@echo "Available revisions for top-leader-prod:"
	@gcloud run revisions list \
		--service=top-leader-prod \
		--region=$(REGION) \
		--platform=managed \
		--format="table(metadata.name,status.traffic,status.conditions[0].status)" \
		--project=$(PROJECT_ID)
	@echo ""
	@read -p "Enter revision name to rollback to: " REVISION; \
	gcloud run services update-traffic top-leader-prod \
		--to-revisions=$$REVISION=100 \
		--region=$(REGION) \
		--platform=managed \
		--project=$(PROJECT_ID)

