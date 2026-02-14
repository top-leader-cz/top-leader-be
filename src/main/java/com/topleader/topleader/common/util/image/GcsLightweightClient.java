package com.topleader.topleader.common.util.image;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class GcsLightweightClient {

    private static final String METADATA_TOKEN_URL =
            "http://metadata.google.internal/computeMetadata/v1/instance/service-accounts/default/token";

    private final RestClient restClient;

    @Value("${gcp.storage.bucket-name}")
    private String defaultBucketName;

    private String cachedToken;
    private Instant tokenExpiry = Instant.MIN;

    public List<String> listObjects(String bucketName) {
        var token = getAccessToken();
        var result = new ArrayList<String>();
        String pageToken = null;

        do {
            var url = "https://storage.googleapis.com/storage/v1/b/%s/o?fields=items(name),nextPageToken&maxResults=1000"
                    .formatted(bucketName);
            if (pageToken != null) {
                url += "&pageToken=" + URLEncoder.encode(pageToken, StandardCharsets.UTF_8);
            }

            var root = restClient.get()
                    .uri(url)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(GcsListResponse.class);

            Optional.ofNullable(root)
                    .map(GcsListResponse::items)
                    .stream()
                    .flatMap(List::stream)
                    .map(GcsObject::name)
                    .forEach(result::add);

            pageToken = Optional.ofNullable(root)
                    .map(GcsListResponse::nextPageToken)
                    .orElse(null);
        } while (pageToken != null);

        log.info("Listed {} objects from GCS bucket: {}", result.size(), bucketName);
        return result;
    }

    public List<String> listObjects() throws Exception {
        return listObjects(defaultBucketName);
    }

    private synchronized String getAccessToken() {
        if (Instant.now().isBefore(tokenExpiry)) {
            return cachedToken;
        }

        var response = restClient.get()
                .uri(METADATA_TOKEN_URL)
                .header("Metadata-Flavor", "Google")
                .retrieve()
                .body(MetadataTokenResponse.class);

        cachedToken = response.accessToken();
        tokenExpiry = Instant.now().plusSeconds(response.expiresIn() - 60);

        var masked = cachedToken.substring(0, 3) + "***" + cachedToken.substring(cachedToken.length() - 3);
        log.info("GCS access token refreshed: {}", masked);

        return cachedToken;
    }

    public String uploadImage(byte[] image, String fileName) throws Exception {
        return uploadImage(image, defaultBucketName, fileName);
    }

    public String uploadImage(byte[] image, String bucketName, String fileName) throws Exception {
        var token = getAccessToken();
        var url = "https://storage.googleapis.com/upload/storage/v1/b/%s/o?uploadType=media&name=%s"
                .formatted(bucketName, URLEncoder.encode(fileName, StandardCharsets.UTF_8));

        restClient.post()
                .uri(url)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.IMAGE_PNG)
                .body(image)
                .retrieve()
                .toBodilessEntity();

        log.info("Image uploaded successfully to GCS: gs://{}/{}", bucketName, fileName);
        return "gs://%s/%s".formatted(bucketName, fileName);
    }

    public byte[] downloadImage(String gsUrl) throws Exception {
        var parts = gsUrl.replace("gs://", "").split("/", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid GCS URL format: " + gsUrl);
        }
        return downloadImage(parts[0], parts[1]);
    }

    public byte[] downloadImage(String bucketName, String fileName) throws Exception {
        var token = getAccessToken();
        var url = "https://storage.googleapis.com/storage/v1/b/%s/o/%s?alt=media"
                .formatted(bucketName, URLEncoder.encode(fileName, StandardCharsets.UTF_8));

        return restClient.get()
                .uri(url)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(byte[].class);
    }

    public record MetadataTokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("expires_in") int expiresIn,
            @JsonProperty("token_type") String tokenType
    ) {}

    public record GcsListResponse(List<GcsObject> items, String nextPageToken) {}

    public record GcsObject(String name) {}
}
