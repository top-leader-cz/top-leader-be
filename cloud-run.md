# Cloud Run Migration Guide

Průvodce migrací TopLeader backend z Google App Engine na Cloud Run pro QA prostředí.

## Přehled změn

| Komponenta | App Engine | Cloud Run |
|------------|------------|-----------|
| **Deploy metoda** | JAR upload | Docker container |
| **Cloud SQL připojení** | Automatické | Cloud SQL Proxy / Unix socket |
| **Secrets** | Environment variables v YAML | Cloud Run secrets |
| **Scaling** | Basic/Automatic scaling | Min/Max instances |
| **Runtime** | Managed Java 25 | Custom Docker image |

## 1. Jib Plugin Setup (Doporučeno)

**Google Jib** je nejlepší způsob, jak vytvářet Docker images pro Java aplikace:

- ✅ **Bez Dockeru** - Jib vytváří images přímo z Gradle
- ✅ **Rychlejší buildy** - Inteligentní layer caching
- ✅ **Menší images** - Optimalizované layers (dependencies, resources, classes odděleně)
- ✅ **Lepší caching** - Dependencies se nemění → layer se použije znovu
- ✅ **Reprodukovatelné buildy** - Stejný kód = stejný image hash

### Přidání Jib do build.gradle.kts

```kotlin
plugins {
    java
    id("org.springframework.boot") version "4.0.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("io.freefair.lombok") version "9.2.0"
    id("org.graalvm.buildtools.native") version "0.10.6"
    id("com.google.cloud.tools.jib") version "3.4.4"  // Přprosi
}

// ... existing configuration ...

// Jib configuration
jib {
    from {
        image = "eclipse-temurin:25-jre-alpine"
        platforms {
            platform {
                architecture = "amd64"
                os = "linux"
            }
        }
    }

    to {
        image = "europe-west3-docker.pkg.dev/topleader-394306/cloud-run/top-leader-be"
        tags = setOf("latest", System.getenv("IMAGE_TAG") ?: "dev")
    }

    container {
        jvmFlags = listOf(
            "-XX:MaxRAMPercentage=75.0",
            "-XX:+UseCompactObjectHeaders",
            "-XX:+UseStringDeduplication",
            "-XX:+DisableExplicitGC",
            "-XX:+TieredCompilation",
            "-XX:TieredStopAtLevel=1",
            "-Djava.security.egd=file:/dev/./urandom"
        )

        ports = listOf("8080")

        user = "1001:1001"  // Non-root user

        creationTime = "USE_CURRENT_TIMESTAMP"

        labels.putAll(mapOf(
            "maintainer" to "topleader",
            "app" to "top-leader-be"
        ))

        environment = mapOf(
            "SPRING_OUTPUT_ANSI_ENABLED" to "NEVER"
        )
    }

    // Layer configuration - optimalizuje caching
    containerizingMode = "packaged"

    // Exclude H2 from container (only needed for AOT)
    extraDirectories {
        paths {
            path {
                setFrom("build/libs")
                includes.add("top-leader.jar")
            }
        }
    }
}

// Task pro build a push Jib image
tasks.register("jibBuildAndPush") {
    group = "jib"
    description = "Build and push Docker image using Jib"
    dependsOn("build")
    finalizedBy("jib")
}
```

### Jib Gradle Tasks

```bash
# Build image a push do Artifact Registry (vyžaduje autentizaci)
gradle jib

# Build image pouze lokálně do Docker daemon
gradle jibDockerBuild

# Build image do tar souboru
gradle jibBuildTar

# Build s custom tagem
IMAGE_TAG=qa-$(git rev-parse --short HEAD) gradle jib
```

## 2. Alternativa: Dockerfile (pokud Jib nefunguje)

<details>
<summary>Klikněte pro Dockerfile alternativu</summary>

Vytvořte `Dockerfile` v root adresáři projektu:

```dockerfile
# Multi-stage build pro optimalizaci velikosti image

# Stage 1: Build stage
FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /app

# Kopírovat Gradle wrapper a build soubory
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Kopírovat zdrojové kódy
COPY src src

# Build aplikace (skip tests - již proběhly v CI)
RUN ./gradlew bootJar -x test --no-daemon --parallel --build-cache

# Stage 2: Runtime stage
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Install dependencies needed for Cloud SQL Proxy (if using sidecar)
RUN apk add --no-cache ca-certificates

# Vytvořit non-root uživatele pro bezpečnost
RUN addgroup -g 1001 -S spring && adduser -u 1001 -S spring -G spring
USER spring:spring

# Kopírovat JAR z build stage
COPY --from=builder /app/build/libs/top-leader.jar app.jar

# Cloud Run očekává port 8080 (definovaný v PORT env var)
EXPOSE 8080

# Optimalizované JVM flagy pro Cloud Run
ENTRYPOINT ["java", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+UseCompactObjectHeaders", \
    "-XX:+UseStringDeduplication", \
    "-XX:+DisableExplicitGC", \
    "-XX:+TieredCompilation", \
    "-XX:TieredStopAtLevel=1", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
```

### .dockerignore

```
# Build artifacts
build/
target/
.gradle/
*.jar
*.war

# IDE
.idea/
.vscode/
*.iml
*.iws
*.ipr

# Version control
.git/
.github/
.gitignore

# Documentation
*.md
docs/

# Tests
src/test/

# Local development
docker-compose.yml
Makefile
```

</details>

## 3. GitHub Actions Workflow změny (s Jib)

Upravte `.github/workflows/deploy.yml` - sekce `deploy-qa`:

```yaml
deploy-qa:
  if: github.event_name == 'push' && startsWith(github.ref, 'refs/tags/qa-deploy')
  runs-on: ubuntu-latest
  env:
    REGION: europe-west3
    SERVICE_NAME: top-leader-qa
    PROJECT_ID: topleader-394306
    IMAGE_NAME: top-leader-be
  steps:
    - uses: actions/checkout@v4

    - name: Set up JDK 25
      uses: actions/setup-java@v4
      with:
        java-version: '25'
        distribution: 'temurin'
        cache: gradle

    - name: Setup Gradle
      uses: gradle/actions/setup-gradle@v4
      with:
        cache-read-only: false

    - name: Cloud Authentication
      uses: google-github-actions/auth@v2
      with:
        credentials_json: ${{ secrets.GCP_CREDENTIALS }}

    - name: Set up Cloud SDK
      uses: google-github-actions/setup-gcloud@v2

    - name: Configure Docker for Artifact Registry
      run: gcloud auth configure-docker ${{ env.REGION }}-docker.pkg.dev

    - name: Generate OpenAPI spec
      run: gradle generateOpenApi

    - name: Build and test
      run: gradle build --parallel --build-cache

    - name: Build and push Docker image with Jib
      env:
        IMAGE_TAG: $(git rev-parse --short "$GITHUB_SHA")
      run: |
        gradle jib \
          -Djib.to.image=${{ env.REGION }}-docker.pkg.dev/${{ env.PROJECT_ID }}/cloud-run/${{ env.IMAGE_NAME }} \
          -Djib.to.tags=latest,$(git rev-parse --short "$GITHUB_SHA")

    - name: Deploy to Cloud Run QA
      run: |
        gcloud run deploy ${{ env.SERVICE_NAME }} \
          --image=${{ env.REGION }}-docker.pkg.dev/${{ env.PROJECT_ID }}/cloud-run/${{ env.IMAGE_NAME }}:$(git rev-parse --short "$GITHUB_SHA") \
          --region=${{ env.REGION }} \
          --platform=managed \
          --allow-unauthenticated \
          --service-account=cloud-run-sa@${{ env.PROJECT_ID }}.iam.gserviceaccount.com \
          --add-cloudsql-instances=${{ env.PROJECT_ID }}:${{ env.REGION }}:top-leader-db \
          --set-env-vars="SPRING_PROFILES_ACTIVE=qa,GCP_JSON_LOGS=CONSOLE_JSON" \
          --set-secrets="SPRING_DATASOURCE_PASSWORD=SPRING_DATASOURCE_PASSWORD_QA:latest,\
SPRING_MAIL_PASSWORD=SPRING_MAIL_PASSWORD:latest,\
TOP_LEADER_CALENDLY_CLIENT_SECRETS=TOP_LEADER_CALENDLY_CLIENT_SECRETS:latest,\
GOOGLE_CLIENT_CLIENT_SECRET=GOOGLE_CLIENT_CLIENT_SECRET:latest,\
SPRING_AI_OPENAI_APIKEY=SPRING_AI_OPENAI_APIKEY:latest,\
GRAFANA_OTLP_TOKEN=GRAFANA_OTLP_TOKEN:latest,\
JOB_TRIGGER_PASSWORD=JOB_TRIGGER_PASSWORD:latest" \
          --cpu=2 \
          --memory=1Gi \
          --min-instances=0 \
          --max-instances=2 \
          --timeout=300 \
          --concurrency=80 \
          --cpu-throttling \
          --session-affinity \
          --port=8080 \
          --tag=qa-$(git rev-parse --short "$GITHUB_SHA") \
          --revision-suffix=$(git rev-parse --short "$GITHUB_SHA") \
          --labels=environment=qa,team=backend,managed-by=github-actions

    - name: Cleanup old revisions (keep last 3)
      run: |
        echo "Fetching QA revisions..."
        REVISIONS=$(gcloud run revisions list \
          --service=${{ env.SERVICE_NAME }} \
          --region=${{ env.REGION }} \
          --platform=managed \
          --sort-by=~creationTimestamp \
          --format="value(name)" | tail -n +4)

        if [ -z "$REVISIONS" ]; then
          echo "No old revisions to delete"
        else
          echo "Deleting old revisions: $REVISIONS"
          for REVISION in $REVISIONS; do
            gcloud run revisions delete $REVISION \
              --region=${{ env.REGION }} \
              --platform=managed \
              --quiet || echo "Failed to delete $REVISION"
          done
        fi
```

**Vysvětlení parametrů:**

| Parametr | Hodnota | Popis |
|----------|---------|-------|
| `--cpu` | 2 | 2 vCPU (doporučeno pro Spring Boot) |
| `--memory` | 1Gi | 1 GB RAM (odpovídá App Engine B4) |
| `--min-instances` | 0 | Škáluje na 0 při nulové zátěži (úspora nákladů) |
| `--max-instances` | 2 | Maximálně 2 instance (stejně jako App Engine) |
| `--timeout` | 300 | 5 minut timeout pro dlouhé requesty |
| `--concurrency` | 80 | Až 80 současných requestů na instanci |
| `--cpu-throttling` | enabled | CPU throttling mimo request handling (úspora nákladů) |
| `--session-affinity` | enabled | Sticky sessions (důležité pro Spring Session JDBC) |
| `--allow-unauthenticated` | - | Veřejný přístup (autentizace v aplikaci) |

## 4. Cloud SQL připojení

### Metoda 1: Unix Socket (Doporučeno)

Cloud Run automaticky poskytuje Unix socket pro Cloud SQL při použití `--add-cloudsql-instances`.

**application-qa.yml změny:**

```yaml
spring:
  datasource:
    # Cloud Run poskytuje Unix socket: /cloudsql/PROJECT_ID:REGION:INSTANCE
    url: jdbc:postgresql:///${DB_NAME}?cloudSqlInstance=${CLOUD_SQL_CONNECTION_NAME}&socketFactory=com.google.cloud.sql.postgres.SocketFactory
    username: postgres
    password: ${SPRING_DATASOURCE_PASSWORD}

  # Cloud SQL connection name (nastaveno jako env var v Cloud Run)
  cloud:
    gcp:
      sql:
        instance-connection-name: ${CLOUD_SQL_CONNECTION_NAME:topleader-394306:europe-west3:top-leader-db}
        database-name: ${DB_NAME:top_leader}
```

**Environment variables v Cloud Run:**

```bash
CLOUD_SQL_CONNECTION_NAME=topleader-394306:europe-west3:top-leader-db
DB_NAME=top_leader
```

### Metoda 2: Cloud SQL Proxy Sidecar (Alternativa)

Pokud potřebujete explicitní proxy:

```yaml
# cloud-run-service.yaml
apiVersion: serving.knative.dev/v1
kind: Service
spec:
  template:
    spec:
      containers:
      - name: top-leader
        image: europe-west3-docker.pkg.dev/topleader-394306/cloud-run/top-leader-be:latest
        ports:
        - containerPort: 8080

      # Cloud SQL Proxy sidecar
      - name: cloud-sql-proxy
        image: gcr.io/cloud-sql-connectors/cloud-sql-proxy:2.14.0
        args:
          - "topleader-394306:europe-west3:top-leader-db"
          - "--private-ip"
          - "--port=5432"
        securityContext:
          runAsNonRoot: true
```

## 5. Secret Management

### Vytvoření secretů v Secret Manager

```bash
# Vytvořit secrets (pouze jednou)
echo -n "your-db-password" | gcloud secrets create SPRING_DATASOURCE_PASSWORD_QA \
    --data-file=- \
    --replication-policy="automatic" \
    --project=topleader-394306

echo -n "your-mail-password" | gcloud secrets create SPRING_MAIL_PASSWORD \
    --data-file=- \
    --replication-policy="automatic" \
    --project=topleader-394306

# Opakujte pro všechny secrets:
# - TOP_LEADER_CALENDLY_CLIENT_SECRETS
# - GOOGLE_CLIENT_CLIENT_SECRET
# - SPRING_AI_OPENAI_APIKEY
# - GRAFANA_OTLP_TOKEN
# - JOB_TRIGGER_PASSWORD
```

### Aktualizace secretů

```bash
# Aktualizovat existující secret
echo -n "new-password" | gcloud secrets versions add SPRING_DATASOURCE_PASSWORD_QA \
    --data-file=- \
    --project=topleader-394306
```

### Service Account oprávnění

Cloud Run service account musí mít přístup ke secretům:

```bash
# Vytvořit service account (pokud neexistuje)
gcloud iam service-accounts create cloud-run-sa \
    --display-name="Cloud Run Service Account" \
    --project=topleader-394306

# Přiřadit oprávnění na secrets
gcloud secrets add-iam-policy-binding SPRING_DATASOURCE_PASSWORD_QA \
    --member="serviceAccount:cloud-run-sa@topleader-394306.iam.gserviceaccount.com" \
    --role="roles/secretmanager.secretAccessor" \
    --project=topleader-394306

# Opakujte pro všechny secrets
```

## 6. Artifact Registry Setup

Vytvořte repository pro Docker images:

```bash
# Vytvořit Artifact Registry repository
gcloud artifacts repositories create cloud-run \
    --repository-format=docker \
    --location=europe-west3 \
    --description="Cloud Run container images" \
    --project=topleader-394306

# Ověřit vytvoření
gcloud artifacts repositories list \
    --location=europe-west3 \
    --project=topleader-394306
```

## 7. Makefile změny

Upravte `Makefile`:

```makefile
# Cloud Run deployment variables
REGION := europe-west3
PROJECT_ID := topleader-394306
SERVICE_NAME := top-leader-qa
IMAGE_NAME := top-leader-be
ARTIFACT_REPO := cloud-run

# Deploy to Cloud Run QA (local deployment - pro testing)
deploy-qa-local: build
	@echo "Building Docker image..."
	@IMAGE_TAG="$(REGION)-docker.pkg.dev/$(PROJECT_ID)/$(ARTIFACT_REPO)/$(IMAGE_NAME):qa-local"; \
	docker build -t $$IMAGE_TAG .; \
	echo "Pushing to Artifact Registry..."; \
	docker push $$IMAGE_TAG; \
	echo "Deploying to Cloud Run..."; \
	gcloud run deploy $(SERVICE_NAME) \
		--image=$$IMAGE_TAG \
		--region=$(REGION) \
		--platform=managed \
		--project=$(PROJECT_ID)

# Deploy to Cloud Run QA (via GitHub Actions tag)
deploy-qa: build
	git tag -d qa-deploy 2>/dev/null || true
	git push origin :refs/tags/qa-deploy 2>/dev/null || true
	git tag qa-deploy
	git push origin qa-deploy

# Cloud Run logs (QA)
logs-qa-cloudrun:
	gcloud logging read 'resource.type="cloud_run_revision" AND resource.labels.service_name="$(SERVICE_NAME)" AND severity>=ERROR' \
		--limit=50 \
		--format="table(timestamp,severity,textPayload)" \
		--project=$(PROJECT_ID)

# Cloud Run service info
info-qa:
	gcloud run services describe $(SERVICE_NAME) \
		--region=$(REGION) \
		--platform=managed \
		--project=$(PROJECT_ID)

# Cloud Run revisions
revisions-qa:
	gcloud run revisions list \
		--service=$(SERVICE_NAME) \
		--region=$(REGION) \
		--platform=managed \
		--project=$(PROJECT_ID)
```

## 8. Postup migrace

### Krok 1: Příprava infrastruktury

**DOPORUČENO: Použít Terraform (Infrastructure as Code)**

```bash
cd terraform

# Initialize (one-time)
terraform init

# Review infrastructure changes
terraform plan

# Create Artifact Registry, Service Account, IAM permissions
terraform apply \
  -target=google_artifact_registry_repository.cloud_run \
  -target=google_service_account.cloud_run_qa \
  -target=google_project_iam_member.cloud_run_sql_client \
  -target=google_secret_manager_secret_iam_member.cloud_run_secrets
```

**Proč Terraform?**
- ✅ Infrastructure as Code (version control)
- ✅ Idempotent (bezpečné opakované spuštění)
- ✅ Drift detection (`terraform plan`)
- ✅ Snadný rollback (`terraform destroy`)

Detaily viz: [`terraform/WHY_TERRAFORM.md`](terraform/WHY_TERRAFORM.md)

**ALTERNATIVA: Manuální setup pomocí gcloud (ne doporučeno)**

<details>
<summary>Klikněte pro manuální gcloud příkazy</summary>

```bash
# 1. Vytvořit Artifact Registry repository
gcloud artifacts repositories create cloud-run \
    --repository-format=docker \
    --location=europe-west3 \
    --project=topleader-394306

# 2. Vytvořit service account
gcloud iam service-accounts create cloud-run-sa \
    --display-name="Cloud Run Service Account" \
    --project=topleader-394306

# 3. Přiřadit oprávnění pro Cloud SQL
gcloud projects add-iam-policy-binding topleader-394306 \
    --member="serviceAccount:cloud-run-sa@topleader-394306.iam.gserviceaccount.com" \
    --role="roles/cloudsql.client"

# 4. Přiřadit oprávnění pro Secret Manager (pro každý secret)
for SECRET in SPRING_DATASOURCE_PASSWORD_QA SPRING_MAIL_PASSWORD TOP_LEADER_CALENDLY_CLIENT_SECRETS GOOGLE_CLIENT_CLIENT_SECRET SPRING_AI_OPENAI_APIKEY GRAFANA_OTLP_TOKEN JOB_TRIGGER_PASSWORD; do
  gcloud secrets add-iam-policy-binding $SECRET \
    --member="serviceAccount:cloud-run-sa@topleader-394306.iam.gserviceaccount.com" \
    --role="roles/secretmanager.secretAccessor" \
    --project=topleader-394306
done
```

**Poznámka:** Manuální setup není doporučený, protože změny nejsou ve version control a mohou způsobit drift. Vždy preferujte Terraform.

</details>

### Krok 2: Vytvoření souborů

```bash
# 1. Vytvořit Dockerfile (viz výše)
# 2. Vytvořit .dockerignore (viz výše)
# 3. Upravit .github/workflows/deploy.yml (viz výše)
# 4. Upravit Makefile (viz výše)
# 5. Upravit application-qa.yml (Cloud SQL connection)
```

### Krok 3: Testování lokálně

```bash
# 1. Build Docker image
docker build -t top-leader-qa-test .

# 2. Spustit lokálně s environment variables
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=qa \
  -e SPRING_DATASOURCE_URL='jdbc:postgresql://host.docker.internal:5434/top_leader' \
  -e SPRING_DATASOURCE_PASSWORD='postgres' \
  top-leader-qa-test

# 3. Otestovat endpoint
curl http://localhost:8080/actuator/health
```

### Krok 4: Deploy do Cloud Run

```bash
# Metoda 1: Přes GitHub Actions (doporučeno)
make deploy-qa

# Metoda 2: Manuální deploy
gcloud auth configure-docker europe-west3-docker.pkg.dev
docker build -t europe-west3-docker.pkg.dev/topleader-394306/cloud-run/top-leader-be:manual .
docker push europe-west3-docker.pkg.dev/topleader-394306/cloud-run/top-leader-be:manual

gcloud run deploy top-leader-qa \
  --image=europe-west3-docker.pkg.dev/topleader-394306/cloud-run/top-leader-be:manual \
  --region=europe-west3 \
  --platform=managed \
  --project=topleader-394306 \
  --allow-unauthenticated \
  --service-account=cloud-run-sa@topleader-394306.iam.gserviceaccount.com \
  --add-cloudsql-instances=topleader-394306:europe-west3:top-leader-db \
  --set-env-vars="SPRING_PROFILES_ACTIVE=qa,GCP_JSON_LOGS=CONSOLE_JSON,CLOUD_SQL_CONNECTION_NAME=topleader-394306:europe-west3:top-leader-db,DB_NAME=top_leader" \
  --set-secrets="SPRING_DATASOURCE_PASSWORD=SPRING_DATASOURCE_PASSWORD_QA:latest" \
  --cpu=2 \
  --memory=1Gi \
  --min-instances=0 \
  --max-instances=2 \
  --timeout=300 \
  --session-affinity
```

### Krok 5: Nastavení custom domény

**DOPORUČENO: Použít Terraform (automatické)**

```bash
cd terraform

# Review změn
terraform plan

# Aplikovat domain mapping + DNS
terraform apply -target=google_cloud_run_domain_mapping.qa -target=google_dns_record_set.qa_cloudrun
```

Terraform automaticky:
- ✅ Vytvoří Cloud Run domain mapping
- ✅ Nastaví DNS CNAME záznam
- ✅ Provisne SSL certifikát

Detaily viz: [`terraform/CLOUD_RUN_MIGRATION.md`](terraform/CLOUD_RUN_MIGRATION.md)

**ALTERNATIVA: Manuální setup (ne doporučeno)**

<details>
<summary>Klikněte pro manuální postup</summary>

```bash
# 1. Mapovat doménu na Cloud Run
gcloud run domain-mappings create \
  --service=top-leader-qa \
  --domain=qa.topleaderplatform.io \
  --region=europe-west3 \
  --project=topleader-394306

# 2. Získat info pro DNS
gcloud run domain-mappings describe \
  --domain=qa.topleaderplatform.io \
  --region=europe-west3 \
  --project=topleader-394306

# 3. Přidat DNS záznam v Cloud DNS
gcloud dns record-sets create qa.topleaderplatform.io. \
  --type=CNAME \
  --ttl=300 \
  --rrdatas=ghs.googlehosted.com. \
  --zone=topleaderplatform-io
```

**Poznámka:** Manuální setup není doporučený, protože změny nejsou ve version control a mohou být náhodně přepsány. Vždy preferujte Terraform.

</details>

## 9. Monitoring a Logging

### Cloud Run specifické metriky

```bash
# Zobrazit metriky služby
gcloud run services describe top-leader-qa \
  --region=europe-west3 \
  --platform=managed \
  --project=topleader-394306

# Sledovat logy real-time
gcloud run services logs read top-leader-qa \
  --region=europe-west3 \
  --platform=managed \
  --project=topleader-394306 \
  --follow

# Filtrovat error logy
gcloud logging read 'resource.type="cloud_run_revision" AND resource.labels.service_name="top-leader-qa" AND severity>=ERROR' \
  --limit=50 \
  --format=json \
  --project=topleader-394306
```

### Grafana OTLP integrace

Cloud Run automaticky exportuje metriky do Grafana, pokud je nastavena `GRAFANA_OTLP_TOKEN` env variable.

Metriky dostupné v Grafana:
- Request count
- Request latency (p50, p95, p99)
- Error rate
- Container CPU utilization
- Container memory utilization
- Instance count

## 10. Cost Comparison

| Služba | App Engine (B4) | Cloud Run (2 CPU, 1Gi) |
|--------|-----------------|------------------------|
| **Idle cost** | Platíte za min. 1 instanci | $0 (scale to zero) |
| **Active cost/hour** | ~$0.20/instance | ~$0.12/instance |
| **Memory** | 1 GB | 1 GB |
| **CPU** | Shared | 2 vCPU |
| **Cold start** | ~5s | ~3s (s container caching) |
| **Max instances** | 2 | 2 |

**Odhad úspory pro QA:**
- App Engine: ~$144/měsíc (min. 1 instance running 24/7)
- Cloud Run: ~$20/měsíc (scale to zero během noci/víkendů)
- **Úspora: ~85%** díky scale-to-zero

## 11. Troubleshooting

### Problem: Container fails to start

```bash
# Zobrazit startup logy
gcloud run services logs read top-leader-qa \
  --region=europe-west3 \
  --limit=100 \
  --project=topleader-394306

# Zkontrolovat health check
gcloud run services describe top-leader-qa \
  --region=europe-west3 \
  --format=yaml \
  --project=topleader-394306
```

### Problem: Cloud SQL connection timeout

```bash
# Ověřit Cloud SQL instance je dostupná
gcloud sql instances describe top-leader-db \
  --project=topleader-394306

# Zkontrolovat service account permissions
gcloud projects get-iam-policy topleader-394306 \
  --flatten="bindings[].members" \
  --filter="bindings.members:cloud-run-sa@topleader-394306.iam.gserviceaccount.com"

# Otestovat připojení manuálně
gcloud sql connect top-leader-db \
  --user=postgres \
  --database=top_leader \
  --project=topleader-394306
```

### Problem: Secret not found

```bash
# Zobrazit všechny secrets
gcloud secrets list --project=topleader-394306

# Ověřit IAM bindings pro konkrétní secret
gcloud secrets get-iam-policy SPRING_DATASOURCE_PASSWORD_QA \
  --project=topleader-394306

# Přidat oprávnění pokud chybí
gcloud secrets add-iam-policy-binding SPRING_DATASOURCE_PASSWORD_QA \
  --member="serviceAccount:cloud-run-sa@topleader-394306.iam.gserviceaccount.com" \
  --role="roles/secretmanager.secretAccessor" \
  --project=topleader-394306
```

### Problem: Out of memory

```bash
# Zvýšit memory limit
gcloud run services update top-leader-qa \
  --memory=2Gi \
  --region=europe-west3 \
  --project=topleader-394306

# Zkontrolovat JVM memory usage v logách
gcloud logging read 'resource.type="cloud_run_revision" AND resource.labels.service_name="top-leader-qa" AND textPayload=~"OutOfMemory|GC"' \
  --limit=50 \
  --project=topleader-394306
```

## 12. Rollback

Pokud potřebujete vrátit předchozí verzi:

```bash
# Zobrazit všechny revize
gcloud run revisions list \
  --service=top-leader-qa \
  --region=europe-west3 \
  --project=topleader-394306

# Přepnout na předchozí revizi
gcloud run services update-traffic top-leader-qa \
  --to-revisions=top-leader-qa-{PREVIOUS_REVISION}=100 \
  --region=europe-west3 \
  --project=topleader-394306

# Nebo rollback na App Engine (pokud stále běží)
# Zastavit Cloud Run traffic
gcloud run services update-traffic top-leader-qa \
  --to-revisions=top-leader-qa-{ANY_REVISION}=0 \
  --region=europe-west3 \
  --project=topleader-394306
```

## 13. Production Migration

Po úspěšné QA migraci můžete migrovat i produkci:

1. **Duplikovat QA setup** s production secrets
2. **Zvýšit resources**:
   - `--cpu=4` (4 vCPU)
   - `--memory=2Gi` (2 GB RAM)
   - `--min-instances=1` (vždy 1 instance running)
   - `--max-instances=10` (autoscaling do 10 instancí)
3. **Přidat health check monitoring**
4. **Nastavit alerting** v Grafana
5. **Otestovat load testing** před přepnutím traffic

## Další kroky

1. ✅ Vytvořit Dockerfile
2. ✅ Vytvořit .dockerignore
3. ✅ Upravit GitHub Actions workflow
4. ✅ Nastavit Artifact Registry
5. ✅ Vytvořit service account
6. ✅ Vytvořit secrets v Secret Manager
7. ✅ Upravit application-qa.yml (Cloud SQL connection)
8. ✅ Upravit Makefile
9. ✅ Otestovat lokální Docker build
10. ✅ Deploy do Cloud Run
11. ✅ Nastavit custom doménu
12. ✅ Monitoring a alerting

---

**Poznámky:**
- Cloud Run je levnější než App Engine (scale-to-zero)
- Cloud Run má rychlejší deployment (~2-3 min vs ~5-10 min)
- Cloud Run poskytuje lepší control nad resources
- Session affinity je podporována (důležité pro Spring Session JDBC)
- Rollback je jednodušší (instant traffic switching)