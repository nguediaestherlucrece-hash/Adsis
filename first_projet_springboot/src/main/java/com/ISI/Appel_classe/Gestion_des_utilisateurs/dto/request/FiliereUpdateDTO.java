package com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request;

import lombok.Data;

@Data
public class FiliereUpdateDTO {
    private String idFiliere;
    private String nomFiliere;
    private String description;

    private Long cycleId;
}