package com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request;

import lombok.Data;

@Data
public class SpecialiteCreateDTO {

    private String idSpecialite;
    private String nom;

    private Long filiereId;
}

