package com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request;

import lombok.Data;

@Data
public class ClasseCreateDTO {

    private String idClasse;
    private String nomClasse;
    private String anneeAcademique;

    private Long specialiteId;
}
