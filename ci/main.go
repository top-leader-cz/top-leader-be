// Dagger CI/CD pipeline for TopLeader backend.
//
// Provides Build, DeployQa, and DeployProd functions that can be run
// locally or triggered from GitHub Actions via `dagger call`.
package main

import (
	"context"
	"fmt"
	"strings"

	"dagger/topleader-ci/internal/dagger"
)

const (
	region      = "europe-west3"
	projectID   = "topleader-394306"
	imagePath   = "europe-west3-docker.pkg.dev/topleader-394306/top-leader/top-leader-be"
	gcloudImage = "gcr.io/google.com/cloudsdktool/cloud-sdk:alpine"
	jreImage    = "europe-west3-docker.pkg.dev/topleader-394306/top-leader/topleader-jre:latest"
)

type TopleaderCi struct{}

// Build runs Gradle build with tests and coverage verification (min 80%).
func (m *TopleaderCi) Build(ctx context.Context, src *dagger.Directory) error {
	_, err := m.gradleContainer(src).
		WithExec([]string{
			"./gradlew", "build", "jacocoTestReport", "jacocoTestCoverageVerification",
			"--parallel", "--build-cache",
		}).
		Sync(ctx)
	return err
}

// DeployQa builds a Docker image tagged as qa-<shortSha> and deploys to Cloud Run QA.
func (m *TopleaderCi) DeployQa(
	ctx context.Context,
	src *dagger.Directory,
	gcpCredentials *dagger.Secret,
	shortSha string,
) error {
	tags := []string{"latest", fmt.Sprintf("qa-%s", shortSha)}
	imageRef, err := m.buildAndPush(ctx, src, gcpCredentials, tags)
	if err != nil {
		return fmt.Errorf("build and push: %w", err)
	}
	return m.deployToCloudRun(ctx, src, gcpCredentials, "service-qa.yaml", "top-leader-qa", imageRef)
}

// DeployProd builds a Docker image tagged with the release version and deploys to Cloud Run PROD.
func (m *TopleaderCi) DeployProd(
	ctx context.Context,
	src *dagger.Directory,
	gcpCredentials *dagger.Secret,
	version string,
) error {
	imageRef, err := m.buildAndPush(ctx, src, gcpCredentials, []string{version})
	if err != nil {
		return fmt.Errorf("build and push: %w", err)
	}
	return m.deployToCloudRun(ctx, src, gcpCredentials, "service-prod.yaml", "top-leader-prod", imageRef)
}

// buildAndPush builds the JAR, assembles the runtime image, and pushes all given tags.
// Replicates the two-stage Dockerfile in Go so we can control registry auth.
// Returns the image reference with digest (e.g. image@sha256:...) of the first tag.
func (m *TopleaderCi) buildAndPush(
	ctx context.Context,
	src *dagger.Directory,
	gcpCredentials *dagger.Secret,
	tags []string,
) (string, error) {
	token, err := m.gcpAccessToken(ctx, gcpCredentials)
	if err != nil {
		return "", err
	}

	// Stage 1: build the fat JAR using Gradle
	jar := m.gradleContainer(src).
		WithExec([]string{"./gradlew", "clean", "bootJar", "-x", "test", "--no-daemon"}).
		File("/app/build/libs/top-leader.jar")

	// Stage 2: assemble the runtime image on top of the custom JRE (private registry)
	runtime := dag.Container().
		WithRegistryAuth("europe-west3-docker.pkg.dev", "oauth2accesstoken", token).
		From(jreImage).
		WithWorkdir("/app").
		WithFile("app.jar", jar).
		WithEnvVariable("JAVA_HOME", "/opt/java").
		WithEnvVariable("PORT", "8080").
		WithExposedPort(8080).
		WithEntrypoint([]string{
			"/opt/java/bin/java",
			"-Dspring.aot.enabled=false",
			"-XX:MaxRAMPercentage=75.0",
			"-XX:InitialRAMPercentage=75.0",
			"-XX:+UseG1GC",
			"-XX:+UseCompactObjectHeaders",
			"-XX:+UseStringDeduplication",
			"-XX:+TieredCompilation",
			"-XX:MaxMetaspaceSize=128m",
			"-XX:+ExitOnOutOfMemoryError",
			"-XX:G1HeapRegionSize=4m",
			"-jar", "app.jar",
		})

	var firstRef string
	for i, tag := range tags {
		ref := fmt.Sprintf("%s:%s", imagePath, tag)
		pushed, err := runtime.Publish(ctx, ref)
		if err != nil {
			return "", fmt.Errorf("push %s: %w", ref, err)
		}
		if i == 0 {
			firstRef = pushed // includes @sha256:... digest
		}
	}
	return firstRef, nil
}

// deployToCloudRun patches the service YAML with imageRef and runs gcloud run services replace.
func (m *TopleaderCi) deployToCloudRun(
	ctx context.Context,
	src *dagger.Directory,
	gcpCredentials *dagger.Secret,
	serviceYaml string,
	serviceName string,
	imageRef string,
) error {
	yamlPath := fmt.Sprintf("src/main/cloudrun/%s", serviceYaml)
	_, err := m.gcloudContainer(gcpCredentials).
		WithDirectory("/app", src).
		WithWorkdir("/app").
		WithExec([]string{
			"sh", "-c",
			fmt.Sprintf(`sed -i 's|image:.*|image: %s|' %s`, imageRef, yamlPath),
		}).
		WithExec([]string{
			"gcloud", "run", "services", "replace", yamlPath,
			"--region=" + region,
			"--project=" + projectID,
		}).
		WithExec([]string{
			"gcloud", "run", "services", "add-iam-policy-binding", serviceName,
			"--region=" + region,
			"--member=allUsers",
			"--role=roles/run.invoker",
			"--project=" + projectID,
		}).
		Sync(ctx)
	return err
}

// gradleContainer returns a Gradle build container with the source mounted and caches configured.
func (m *TopleaderCi) gradleContainer(src *dagger.Directory) *dagger.Container {
	return dag.Container().
		From("eclipse-temurin:25-jdk").
		WithDirectory("/app", src).
		WithWorkdir("/app").
		WithMountedCache("/root/.gradle", dag.CacheVolume("gradle-deps")).
		WithMountedCache("/app/.gradle", dag.CacheVolume("gradle-project"))
}

// gcpAccessToken authenticates as service account and returns an access token secret.
func (m *TopleaderCi) gcpAccessToken(ctx context.Context, gcpCredentials *dagger.Secret) (*dagger.Secret, error) {
	raw, err := m.gcloudContainer(gcpCredentials).
		WithExec([]string{"gcloud", "auth", "print-access-token"}).
		Stdout(ctx)
	if err != nil {
		return nil, fmt.Errorf("get gcp access token: %w", err)
	}
	return dag.SetSecret("gcp-access-token", strings.TrimSpace(raw)), nil
}

// gcloudContainer returns a gcloud container authenticated via the given service account key.
func (m *TopleaderCi) gcloudContainer(gcpCredentials *dagger.Secret) *dagger.Container {
	return dag.Container().
		From(gcloudImage).
		WithMountedSecret("/gcp-key.json", gcpCredentials).
		WithExec([]string{
			"gcloud", "auth", "activate-service-account",
			"--key-file=/gcp-key.json",
		})
}
