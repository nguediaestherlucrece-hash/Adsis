package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class coursUECreateDTO {

    private String idCours;
    private String codeUE;
    private String intitule;
    private Integer volumeHoraire;

    private Long specialiteId;
    private List<Long> enseignantIds;
}

