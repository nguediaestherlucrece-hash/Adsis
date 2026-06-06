package com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request;

import lombok.Data;

@Data
public class NiveauUpdateDTO {

    private String idNiveau;
    private String nom;
    private Long filiereId;
    private Integer ordre;
}
