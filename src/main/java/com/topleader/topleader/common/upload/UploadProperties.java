package com.topleader.topleader.common.upload;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@Data
@ConfigurationProperties(prefix = "top-leader.upload")
public class UploadProperties {

    private Set<String> allowedImageTypes;

    private long maxImageSizeBytes;
}