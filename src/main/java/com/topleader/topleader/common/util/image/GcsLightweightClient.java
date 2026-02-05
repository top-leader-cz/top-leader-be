package com.topleader.topleader.common.util.image;

import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

@Slf4j
@Component
@ConditionalOnProperty(name = "gcp.storage.enabled", havingValue = "true", matchIfMissing = false)
public class GcsLightweightClient {

    private final HttpClient httpClient;
    private final GoogleCredentials credentials;

    @Value("${gcp.storage.bucket-name}")
    private String defaultBucketName;

    public GcsLightweightClient() throws IOException {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        this.credentials = GoogleCredentials.getApplicationDefault()
                .createScoped("https://www.googleapis.com/auth/devstorage.read_write");
    }

    private String getAccessToken() throws IOException {
        credentials.refreshIfExpired();
        return credentials.getAccessToken().getTokenValue();
    }

    public String uploadImage(byte[] image, String fileName, Map<String, String> metadata) throws Exception {
        return uploadImage(image, defaultBucketName, fileName, metadata);
    }

    public String uploadImage(byte[] image, String bucketName, String fileName, Map<String, String> metadata) throws Exception {
        var token = getAccessToken();
        var url = String.format(
                "https://storage.googleapis.com/upload/storage/v1/b/%s/o?uploadType=media&name=%s",
                bucketName,
                URLEncoder.encode(fileName, StandardCharsets.UTF_8)
        );

        var requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "image/png")
                .timeout(Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.ofByteArray(image));

        if (metadata != null && !metadata.isEmpty()) {
            var metadataJson = buildMetadataJson(metadata);
            requestBuilder.header("x-goog-meta-data", metadataJson);
        }

        var request = requestBuilder.build();
        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.error("GCS upload failed with status {}: {}", response.statusCode(), response.body());
            throw new RuntimeException("Upload failed: " + response.body());
        }

        log.info("Image uploaded successfully to GCS: gs://{}/{}", bucketName, fileName);
        return String.format("gs://%s/%s", bucketName, fileName);
    }

    public byte[] downloadImage(String gsUrl) throws Exception {
        var parts = gsUrl.replace("gs://", "").split("/", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid GCS URL format: " + gsUrl);
        }

        var bucketName = parts[0];
        var fileName = parts[1];

        return downloadImage(bucketName, fileName);
    }

    public byte[] downloadImage(String bucketName, String fileName) throws Exception {
        var token = getAccessToken();
        var url = String.format(
                "https://storage.googleapis.com/storage/v1/b/%s/o/%s?alt=media",
                bucketName,
                URLEncoder.encode(fileName, StandardCharsets.UTF_8)
        );

        var request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .timeout(Duration.ofSeconds(60))
                .GET()
                .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

        if (response.statusCode() != 200) {
            log.error("GCS download failed with status {}: {}", response.statusCode(), new String(response.body()));
            throw new RuntimeException("Download failed with status: " + response.statusCode());
        }

        log.info("Image downloaded successfully from GCS: gs://{}/{}", bucketName, fileName);
        return response.body();
    }

    private String buildMetadataJson(Map<String, String> metadata) {
        var json = new StringBuilder("{");
        var first = true;
        for (var entry : metadata.entrySet()) {
            if (!first) {
                json.append(",");
            }
            json.append("\"").append(escapeJson(entry.getKey())).append("\":")
                    .append("\"").append(escapeJson(entry.getValue())).append("\"");
            first = false;
        }
        json.append("}");
        return json.toString();
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}