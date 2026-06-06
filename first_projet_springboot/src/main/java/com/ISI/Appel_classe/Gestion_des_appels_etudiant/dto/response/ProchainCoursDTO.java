package com.ISI.Appel_classe.Gestion_des_appels_etudiant.dto.response;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ProchainCoursDTO {
    private Long seanceId;
    private String matiere;
    private LocalDate date;
    private String jour;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private String enseignant;
    /** true si c'est un événement spécial (sans cours associé) → ne pas afficher dans l'onglet Appel */
    private boolean evenement;
}
