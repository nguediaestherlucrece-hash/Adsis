package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.response;

import lombok.Data;

@Data
public class CoursUEDTO {

    private Long id;
    private String idCours;
    private String codeUE;
    private String intitule;
    private Integer volumeHoraire;

    private Long specialiteId;
}
