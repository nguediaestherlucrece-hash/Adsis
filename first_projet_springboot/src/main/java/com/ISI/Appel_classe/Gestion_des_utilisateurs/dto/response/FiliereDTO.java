package com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response;

import lombok.Data;

@Data
public class FiliereDTO {
    private Long id;
    private String idFiliere;
    private String nomFiliere;
    private String description;


    private Long cycleId;
}