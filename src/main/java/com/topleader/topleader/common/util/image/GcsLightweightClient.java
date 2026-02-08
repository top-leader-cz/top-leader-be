package com.topleader.topleader.common.util.image;

import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(name = "gcp.storage.enabled", havingValue = "true", matchIfMissing = false)
public class GcsLightweightClient {

    private final HttpClient httpClient;
    private final GoogleCredentials credentials;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gcp.storage.bucket-name}")
    private String defaultBucketName;

    public GcsLightweightClient() throws IOException {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        this.credentials = GoogleCredentials.getApplicationDefault()
                .createScoped("https://www.googleapis.com/auth/devstorage.read_write");
    }

    public List<String> listObjects(String bucketName) throws Exception {
        var token = getAccessToken();
        var result = new ArrayList<String>();
        String pageToken = null;

        do {
            var url = String.format(
                    "https://storage.googleapis.com/storage/v1/b/%s/o?fields=items(name),nextPageToken&maxResults=1000",
                    bucketName
            );
            if (pageToken != null) {
                url += "&pageToken=" + URLEncoder.encode(pageToken, StandardCharsets.UTF_8);
            }

            var request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();

            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("GCS list objects failed with status {}: {}", response.statusCode(), response.body());
                throw new RuntimeException("List objects failed: " + response.body());
            }

            var root = objectMapper.readTree(response.body());
            var items = root.get("items");
            if (items != null && items.isArray()) {
                for (var item : items) {
                    result.add(item.get("name").asText());
                }
            }

            var nextPageTokenNode = root.get("nextPageToken");
            pageToken = (nextPageTokenNode != null && !nextPageTokenNode.isNull()) ? nextPageTokenNode.asText() : null;
        } while (pageToken != null);

        log.info("Listed {} objects from GCS bucket: {}", result.size(), bucketName);
        return result;
    }

    public List<String> listObjects() throws Exception {
        return listObjects(defaultBucketName);
    }

    private String getAccessToken() throws IOException {
        credentials.refreshIfExpired();
        return credentials.getAccessToken().getTokenValue();
    }

    public String uploadImage(byte[] image, String fileName) throws Exception {
        return uploadImage(image, defaultBucketName, fileName);
    }

    public String uploadImage(byte[] image, String bucketName, String fileName) throws Exception {
        var token = getAccessToken();
        var url = String.format(
                "https://storage.googleapis.com/upload/storage/v1/b/%s/o?uploadType=media&name=%s",
                bucketName,
                URLEncoder.encode(fileName, StandardCharsets.UTF_8)
        );

        var request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "image/png")
                .timeout(Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.ofByteArray(image))
                .build();

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

        return response.body();
    }

}