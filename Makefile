# Google Cloud commands
.PHONY: login logs-qa logs-prod openapi build native native-test native-linux deploy-qa deploy-prod run local-login local-whoami local-google-auth local-api local-health local-create-user test test-coverage test-report coverage-verify postman

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

# Testing commands
# Run all tests
test:
	JAVA_HOME=$(HOME)/.sdkman/candidates/java/25 $(HOME)/.sdkman/candidates/gradle/current/bin/gradle test

# Run tests with coverage report
test-coverage:
	JAVA_HOME=$(HOME)/.sdkman/candidates/java/25 $(HOME)/.sdkman/candidates/gradle/current/bin/gradle test jacocoTestReport

# Open coverage report in browser
test-report: test-coverage
	@echo "Opening coverage report..."
	open build/reports/jacoco/test/html/index.html || xdg-open build/reports/jacoco/test/html/index.html

# Verify coverage meets minimum threshold (70%)
coverage-verify:
	JAVA_HOME=$(HOME)/.sdkman/candidates/java/25 $(HOME)/.sdkman/candidates/gradle/current/bin/gradle jacocoTestCoverageVerification

# Generate Postman collection from OpenAPI spec
postman: openapi
	@echo "Converting OpenAPI spec to Postman collection..."
	python3 scripts/openapi_to_postman.py src/main/resources/static/openapi.yaml postman/top-leader.postman_collection.json
	@echo "✅ Postman collection created at: postman/top-leader.postman_collection.json"

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

