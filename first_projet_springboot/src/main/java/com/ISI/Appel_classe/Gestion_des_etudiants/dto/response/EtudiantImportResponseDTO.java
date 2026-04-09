package com.ISI.Appel_classe.Gestion_des_etudiants.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO de réponse pour l'importation d'étudiants depuis un fichier Excel
 * Contient les informations de succès et les détails sur les étudiants créés
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtudiantImportResponseDTO {

    /**
     * Nombre total d'étudiants qui ont été traités
     */
    private int totalImported;

    /**
     * Nombre d'étudiants créés avec succès
     */
    private int successCount;

    /**
     * Nombre d'étudiants qui ont échoué
     */
    private int failureCount;

    /**
     * Message de statut général
     */
    private String message;

    /**
     * Liste des étudiants créés avec succès
     */
    @Builder.Default
    private List<EtudiantDTO> createdEtudiants = new ArrayList<>();

    /**
     * Liste des erreurs lors de l'import
     */
    @Builder.Default
    private List<ImportError> errors = new ArrayList<>();

    /**
     * Classe interne pour représenter une erreur lors de l'import
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ImportError {
        /**
         * Numéro de ligne dans le fichier Excel (1-indexed)
         */
        private int lineNumber;

        /**
         * Email de l'étudiant qui a échoué
         */
        private String email;

        /**
         * Message d'erreur détaillé
         */
        private String errorMessage;
    }
}
