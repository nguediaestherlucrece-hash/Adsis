package com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response;

import lombok.Data;

@Data
public class SpecialiteDTO {

    private Long id;

    private String idSpecialite;
    private String nom;

    private Long filiereId;
}
