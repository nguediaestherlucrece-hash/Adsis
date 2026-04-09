package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class CoursUEDTO {

    private Long id;
    private String idCours;
    private String codeUE;
    private String intitule;
    private Integer volumeHoraire;
    private Integer credit;

    private Long specialiteId;
    private String specialiteNom;
    private List<EnseignantSimpleDTO> enseignants;
}

