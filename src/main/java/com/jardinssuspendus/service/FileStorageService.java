package com.jardinssuspendus.service;

import com.jardinssuspendus.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final List<String> ALLOWED_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/webp"
    );
    private static final long MAX_SIZE_BYTES = 10 * 1024 * 1024; // 10 MB

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String storeFile(MultipartFile file) {
        // Validation du fichier
        validateFile(file);

        try {
            // Créer le dossier si nécessaire
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            // Générer un nom de fichier unique et sécurisé
            String originalFilename = StringUtils.cleanPath(
                    file.getOriginalFilename() != null ? file.getOriginalFilename() : "image"
            );
            String extension = getExtension(originalFilename);
            String filename   = UUID.randomUUID().toString() + extension;

            // Copier le fichier
            Path targetLocation = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return filename;

        } catch (IOException ex) {
            throw new RuntimeException("Impossible de stocker le fichier : " + ex.getMessage(), ex);
        }
    }

    public void deleteFile(String filename) {
        if (filename == null || filename.isBlank()) return;
        try {
            Path filePath = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(filename);
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            // On log mais on ne bloque pas l'opération
            System.err.println("Impossible de supprimer le fichier " + filename + " : " + ex.getMessage());
        }
    }

    public Path getFilePath(String filename) {
        return Paths.get(uploadDir).toAbsolutePath().normalize().resolve(filename);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Le fichier est vide");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new BadRequestException("Le fichier dépasse la taille maximale autorisée (10 MB)");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Format de fichier non autorisé. Formats acceptés : JPEG, PNG, WebP");
        }
    }

    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex >= 0 ? filename.substring(dotIndex).toLowerCase() : ".jpg";
    }
}