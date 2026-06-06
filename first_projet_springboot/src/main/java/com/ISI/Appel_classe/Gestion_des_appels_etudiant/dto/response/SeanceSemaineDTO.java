package com.ISI.Appel_classe.Gestion_des_appels_etudiant.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class SeanceSemaineDTO {
    private Long seanceId;
    private String jour;
    private LocalDate date;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private String matiere;
    private String enseignant;
    
    @JsonProperty("isEvent")
    private boolean isEvent;
    private String nomEvenement;
    private String couleur;
}
