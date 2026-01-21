package com.topleader.topleader.common.util.image;

import com.topleader.topleader.common.exception.ApiValidationException;
import com.topleader.topleader.common.upload.UploadProperties;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import static com.topleader.topleader.common.exception.ErrorCodeConstants.INVALID_PARAMETER;

@Component
@AllArgsConstructor
public class ImageValidation {

    private final UploadProperties uploadProperties;

    public void validateImageUpload(MultipartFile file) {
        if (!uploadProperties.getAllowedImageTypes().contains(file.getContentType())) {
            throw new ApiValidationException(
                INVALID_PARAMETER,
                "file",
                file.getContentType(),
                "Invalid image type. Allowed: JPEG, PNG, GIF, WEBP, SVG"
            );
        }

        if (file.getSize() > uploadProperties.getMaxImageSizeBytes()) {
            var maxSizeMB = uploadProperties.getMaxImageSizeBytes() / (1024 * 1024);
            throw new ApiValidationException(
                INVALID_PARAMETER,
                "file",
                String.valueOf(file.getSize()),
                String.format("Image too large. Max %d MB", maxSizeMB)
            );
        }

        try {
            var data = file.getBytes();
            if (!isValidImageMagicBytes(data)) {
                throw new ApiValidationException(
                    INVALID_PARAMETER,
                    "file",
                    "magic bytes check failed",
                    "File is not a valid image"
                );
            }
        } catch (ApiValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiValidationException(
                INVALID_PARAMETER,
                "file",
                e.getMessage(),
                "Failed to read image file"
            );
        }
    }

    private boolean isValidImageMagicBytes(byte[] data) {
        if (data.length < 12) {
            return false;
        }

        // JPEG: FF D8 FF
        if (data[0] == (byte)0xFF && data[1] == (byte)0xD8 && data[2] == (byte)0xFF) {
            return true;
        }

        // PNG: 89 50 4E 47 (\x89PNG)
        if (data[0] == (byte)0x89 && data[1] == (byte)0x50 &&
            data[2] == (byte)0x4E && data[3] == (byte)0x47) {
            return true;
        }

        // GIF: 47 49 46 38 (GIF8)
        if (data[0] == (byte)0x47 && data[1] == (byte)0x49 &&
            data[2] == (byte)0x46 && data[3] == (byte)0x38) {
            return true;
        }

        // WebP: RIFF....WEBP (52 49 46 46 ... 57 45 42 50)
        if (data[0] == (byte)0x52 && data[1] == (byte)0x49 &&
            data[2] == (byte)0x46 && data[3] == (byte)0x46 &&
            data[8] == (byte)0x57 && data[9] == (byte)0x45 &&
            data[10] == (byte)0x42 && data[11] == (byte)0x50) {
            return true;
        }

        // SVG: starts with < and contains "svg" in first 1000 bytes
        // Common patterns: <svg or <?xml
        if (data[0] == (byte)0x3C) {
            var limit = Math.min(data.length, 1000);
            var prefix = new String(data, 0, limit);
            if (prefix.toLowerCase().contains("svg")) {
                return true;
            }
        }

        return false;
    }
}
