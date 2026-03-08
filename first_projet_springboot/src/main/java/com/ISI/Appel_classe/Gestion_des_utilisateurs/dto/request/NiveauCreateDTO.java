package com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request;

import lombok.Data;

@Data
public class NiveauCreateDTO {

    private String idNiveau;
    private String nom;

    private Long cycleId;
}
