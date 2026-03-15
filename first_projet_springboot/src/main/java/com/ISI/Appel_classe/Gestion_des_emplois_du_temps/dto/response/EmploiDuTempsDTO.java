package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class EmploiDuTempsDTO {

    private Long id;
    private String idEmploi;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Integer nombreJours;

    private Long semestreId;
    private String semestreNom;
    private String anneeNom;
    private Long classeId;
    private String classeNom;
    private Long gestionnaireId;

}

