package com.smartbook.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

@Service
public class LogoStorageService {

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/png",
            "image/jpeg",
            "image/webp"
    );

    private final Path uploadDirectory =
            Path.of("uploads", "logos").toAbsolutePath().normalize();

    public String storeLogo(MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please select a logo file.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "Logo size must not exceed 2 MB."
            );
        }

        String contentType = file.getContentType();

        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                    "Only PNG, JPG and WebP images are supported."
            );
        }

        String extension = switch (contentType) {
            case "image/png" -> ".png";
            case "image/jpeg" -> ".jpg";
            case "image/webp" -> ".webp";
            default -> throw new IllegalArgumentException(
                    "Unsupported image format."
            );
        };

        Files.createDirectories(uploadDirectory);

        String fileName = UUID.randomUUID() + extension;
        Path destination = uploadDirectory.resolve(fileName).normalize();

        if (!destination.startsWith(uploadDirectory)) {
            throw new IllegalArgumentException("Invalid file path.");
        }

        file.transferTo(destination);

        return "/uploads/logos/" + fileName;
    }
}