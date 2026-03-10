package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.request;

import lombok.Data;

@Data
public class coursUEUpdateDTO {

    private String idCours;
    private String codeUE;
    private String intitule;
    private Integer volumeHoraire;
}
