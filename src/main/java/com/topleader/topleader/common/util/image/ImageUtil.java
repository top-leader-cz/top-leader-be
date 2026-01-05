/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.common.util.image;

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import lombok.extern.slf4j.Slf4j;


/**
 * @author Daniel Slavik
 */
@Slf4j
public final class ImageUtil {

    private ImageUtil() {
        //util class
    }

    public static byte[] compressImage(byte[] data) {

        final var deflater = new Deflater();
        deflater.setLevel(Deflater.BEST_COMPRESSION);
        deflater.setInput(data);
        deflater.finish();

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length)) {
            byte[] tmp = new byte[4 * 1024];
            while (!deflater.finished()) {
                int size = deflater.deflate(tmp);
                outputStream.write(tmp, 0, size);
            }
            return outputStream.toByteArray();
        } catch (Exception e) {
            final var errorMessage = "Unable to compressed an image.";
            log.error(errorMessage, e);
            throw new IllegalStateException(errorMessage, e);
        }
    }

    public static byte[] decompressImage(byte[] data) {
        final var inflater = new Inflater();
        inflater.setInput(data);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length)) {
            byte[] tmp = new byte[4 * 1024];
            while (!inflater.finished()) {
                int count = inflater.inflate(tmp);
                outputStream.write(tmp, 0, count);
            }
            return outputStream.toByteArray();
        } catch (Exception e) {
            final var errorMessage = "Unable to decompressed an image.";
            log.error(errorMessage, e);
            throw new IllegalStateException(errorMessage, e);
        }
    }
}
