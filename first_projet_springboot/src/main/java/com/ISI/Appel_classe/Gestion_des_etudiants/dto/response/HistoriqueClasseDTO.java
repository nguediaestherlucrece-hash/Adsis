package com.ISI.Appel_classe.Gestion_des_etudiants.dto.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class HistoriqueClasseDTO {
    private Long id;
    private Long etudiantId;

    // Classe info
    private Long classeId;
    private String classeNom;

    // Repères temporels globaux
    private Long anneeAcademiqueId;
    private String anneeNom;
    private Long semestreId;
    private String semestreNom;

    // Dates
    private LocalDate dateEntree;
    private LocalDate dateSortie;
    private String motif;

    // Récapitulatif de présence pour cette classe
    private long totalSeances;
    private long presencesCount;
    private double tauxPresence;
}
