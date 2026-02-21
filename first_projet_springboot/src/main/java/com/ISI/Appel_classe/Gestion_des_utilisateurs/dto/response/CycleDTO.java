package com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response;

import lombok.Data;

@Data
public class CycleDTO {
    private Long id;
    private String idCycle;
    private String nomCycle;
    private Integer anneeDeCreation;
    private Integer anneeDebut;
    private Integer anneeFin;
}
