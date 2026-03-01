package com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request;

import lombok.Data;

@Data
public class CycleCreateDTO {
    private String idCycle;
    private String nomCycle;
    private Integer anneeDeCreation;
    private Integer anneeDebut;
    private Integer anneeFin;
}