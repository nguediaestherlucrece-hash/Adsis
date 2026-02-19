package com.Gestion_des_utilisateurs.dto.response;

import lombok.Data;

@Data
public class ClasseDTO {
    private Long id;
    private String idClasse;
    private String nomClasse;
    private String niveau;
    private String anneeAcademique;
}
