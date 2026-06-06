package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeanceEnseignantDTO {
    
    private Long id;
    private String idSeance;
    private LocalDate date;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private boolean statut;
    private String couleur;
    
    // Informations du cours
    private Long coursId;
    private String coursCode;
    private String coursIntitule;
    
    // Informations de la classe
    private Long classeId;
    private String classeNom;
    
    // Informations de l'appel
    private Long appelId;
    private String appelStatut;
    private Integer nombrePresents;
    private Integer nombreAbsents;
    private Integer nombreRetards;
    private Integer nombreTotal;

    @JsonProperty("isEvent")
    private boolean isEvent;
    private String nomEvenement;
}
