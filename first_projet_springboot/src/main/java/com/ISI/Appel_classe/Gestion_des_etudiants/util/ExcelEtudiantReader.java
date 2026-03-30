package com.ISI.Appel_classe.Gestion_des_etudiants.util;

import com.ISI.Appel_classe.Gestion_des_etudiants.dto.request.EtudiantCreateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.enums.Statut;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Classe utilitaire pour lire et parser les fichiers Excel contenant la liste des étudiants
 * Supports les formats .xlsx et .xls
 */
@Slf4j
@Component
public class ExcelEtudiantReader {

    /**
     * Lit un fichier Excel et retourne une liste de DTO EtudiantCreateDTO
     *
     * Format du fichier Excel attendu (en-têtes dans la première ligne):
     * - Colonne A: Nom (obligatoire)
     * - Colonne B: Prénom (obligatoire)
     * - Colonne C: Email (obligatoire)
     * - Colonne D: Password (obligatoire)
     * - Colonne E: Matricule (obligatoire)
     * - Colonne F: Date Naissance (optionnel) - Format: YYYY-MM-DD
     * - Colonne G: Téléphone (optionnel)
     * - Colonne H: Classe ID (obligatoire)
     * - Colonne I: Statut (optionnel) - Valeurs: ACTIF, INACTIF
     *
     * @param file Le fichier uploadé
     * @return Une liste de EtudiantCreateDTO
     * @throws BadRequestException Si le fichier est invalide ou mal formaté
     */
    public List<EtudiantCreateDTO> readEtudiants(MultipartFile file) {
        log.info("Début de la lecture du fichier Excel: {}", file.getOriginalFilename());

        List<EtudiantCreateDTO> etudiants = new ArrayList<>();

        // Vérifier que le fichier n'est pas vide
        if (file.isEmpty()) {
            throw new BadRequestException("Le fichier Excel est vide");
        }

        // Vérifier l'extension du fichier
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            throw new BadRequestException("Le fichier doit être au format Excel (.xlsx ou .xls)");
        }

        try (InputStream inputStream = file.getInputStream()) {
            // Créer le workbook en fonction du format du fichier
            Workbook workbook = createWorkbook(inputStream, filename);

            if (workbook == null) {
                throw new BadRequestException("Impossible de lire le fichier Excel");
            }

            // Récupérer la première feuille
            Sheet sheet = workbook.getSheetAt(0);

            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                throw new BadRequestException("Le fichier Excel est vide ou ne contient pas de feuille");
            }

            // Sauter la première ligne (en-têtes)
            int rowIndex = 1;
            int rowCount = 0;

            for (Row row : sheet) {
                rowCount++;
                if (rowIndex++ == 1 || row == null) {
                    continue; // Sauter la première ligne (en-têtes)
                }

                // Vérifier que la ligne n'est pas vide
                if (isRowEmpty(row)) {
                    log.debug("Ligne {} vide, ignorée", rowCount);
                    continue;
                }

                try {
                    EtudiantCreateDTO etudiant = parseRow(row, rowCount);
                    etudiants.add(etudiant);
                    log.debug("Étudiant parsé à la ligne {}: {}", rowCount, etudiant.getEmail());
                } catch (Exception e) {
                    log.warn("Erreur lors du parsing de la ligne {}: {}", rowCount, e.getMessage());
                    throw new BadRequestException(
                            "Erreur à la ligne " + rowCount + ": " + e.getMessage()
                    );
                }
            }

            workbook.close();

            if (etudiants.isEmpty()) {
                throw new BadRequestException(
                        "Aucun étudiant trouvé dans le fichier Excel. Vérifiez que le fichier contient au moins un étudiant après les en-têtes."
                );
            }

            log.info("✅ {} étudiant(s) lus avec succès du fichier Excel", etudiants.size());
            return etudiants;

        } catch (IOException e) {
            log.error("Erreur lors de la lecture du fichier Excel: {}", e.getMessage());
            throw new BadRequestException("Erreur lors de la lecture du fichier Excel: " + e.getMessage());
        }
    }

    /**
     * Parse une ligne du fichier Excel et retourne un EtudiantCreateDTO
     */
    private EtudiantCreateDTO parseRow(Row row, int rowCount) {
        EtudiantCreateDTO dto = new EtudiantCreateDTO();

        // Colonne A: Nom (obligatoire)
        String nom = getCellStringValue(row, 0);
        if (nom == null || nom.trim().isEmpty()) {
            throw new IllegalArgumentException("Nom obligatoire (colonne A) - valeur manquante");
        }
        dto.setNom(nom.trim());

        // Colonne B: Prénom (obligatoire)
        String prenom = getCellStringValue(row, 1);
        if (prenom == null || prenom.trim().isEmpty()) {
            throw new IllegalArgumentException("Prénom obligatoire (colonne B) - valeur manquante");
        }
        dto.setPrenom(prenom.trim());

        // Colonne C: Email (obligatoire)
        String email = getCellStringValue(row, 2);
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email obligatoire (colonne C) - valeur manquante");
        }
        dto.setEmail(email.trim());

        // Colonne D: Password (obligatoire)
        String password = getCellStringValue(row, 3);
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Mot de passe obligatoire (colonne D) - valeur manquante");
        }
        dto.setPassword(password.trim());

        // Colonne E: Matricule (obligatoire)
        String matricule = getCellStringValue(row, 4);
        if (matricule == null || matricule.trim().isEmpty()) {
            throw new IllegalArgumentException("Matricule obligatoire (colonne E) - valeur manquante");
        }
        dto.setMatricule(matricule.trim());

        // Colonne F: Date Naissance (optionnel)
        String dateNaissance = getCellStringValue(row, 5);
        if (dateNaissance != null && !dateNaissance.trim().isEmpty()) {
            dto.setDateNaissance(dateNaissance.trim());
        }

        // Colonne G: Téléphone (optionnel)
        String telephone = getCellStringValue(row, 6);
        if (telephone != null && !telephone.trim().isEmpty()) {
            dto.setTelephone(telephone.trim());
        }

        // Colonne H: Classe ID (obligatoire)
        Long classeId = getCellLongValue(row, 7);
        if (classeId == null) {
            throw new IllegalArgumentException("ID Classe obligatoire (colonne H) - valeur manquante");
        }
        dto.setClasseId(classeId);

        // Colonne I: Statut (optionnel)
        String statut = getCellStringValue(row, 8);
        if (statut != null && !statut.trim().isEmpty()) {
            try {
                dto.setStatut(Statut.valueOf(statut.trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("Statut invalide: {}. Utilisation de ACTIF par défaut", statut);
                dto.setStatut(Statut.ACTIF);
            }
        }

        // Assigner le rôle ETUDIANT par défaut
        dto.setRoleNames(new HashSet<>(Collections.singletonList("ETUDIANT")));

        return dto;
    }

    /**
     * Récupère la valeur d'une cellule sous forme de String
     */
    private String getCellStringValue(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                // Si c'est un nombre, le convertir en String
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case BLANK:
                return null;
            default:
                return null;
        }
    }

    /**
     * Récupère la valeur d'une cellule sous forme de Long
     */
    private Long getCellLongValue(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return null;
        }

        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return (long) cell.getNumericCellValue();
                case STRING:
                    String value = cell.getStringCellValue().trim();
                    if (value.isEmpty()) {
                        return null;
                    }
                    return Long.parseLong(value);
                default:
                    return null;
            }
        } catch (NumberFormatException e) {
            log.warn("Impossible de convertir la cellule en Long: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Vérifie si une ligne est vide
     */
    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }

        for (int columnIndex = 0; columnIndex < row.getLastCellNum(); columnIndex++) {
            Cell cell = row.getCell(columnIndex);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    /**
     * Crée un Workbook en fonction du format du fichier
     */
    private Workbook createWorkbook(InputStream inputStream, String filename) throws IOException {
        if (filename.endsWith(".xlsx")) {
            return new XSSFWorkbook(inputStream);
        } else if (filename.endsWith(".xls")) {
            return new HSSFWorkbook(inputStream);
        }
        return null;
    }
}
