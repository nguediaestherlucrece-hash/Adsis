package com.ISI.Appel_classe.Gestion_des_justificatifs.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class SaveDocument {

    private static final String UPLOAD_DIR = "uploads/justificatifs/";
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    public String saveDocument(MultipartFile file) throws Exception {
        // Vérifier la taille
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new Exception("Le fichier dépasse la taille maximale de 10 MB");
        }

        // Vérifier le type MIME
        String contentType = file.getContentType();
        if (!isAllowedMimeType(contentType)) {
            throw new Exception("Type de fichier non autorisé. Acceptés: PDF, images (PNG, JPG, JPEG), documents scannés");
        }

        // Créer le dossier s'il n'existe pas
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // Générer un nom unique
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(UPLOAD_DIR, fileName);

        // Sauvegarder le fichier
        Files.write(filePath, file.getBytes());

        return UPLOAD_DIR + fileName;
    }

    private boolean isAllowedMimeType(String contentType) {
        if (contentType == null) return false;
        return contentType.equals("application/pdf") ||
                contentType.startsWith("image/png") ||
                contentType.startsWith("image/jpeg") ||
                contentType.startsWith("image/jpg");
    }
}
