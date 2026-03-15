package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class EmploiDuTempsUpdateDTO {

    private String idEmploi;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Integer nombreJours;

    private Long semestreId;
    private Long classeId;
    private Long gestionnaireId;
}

