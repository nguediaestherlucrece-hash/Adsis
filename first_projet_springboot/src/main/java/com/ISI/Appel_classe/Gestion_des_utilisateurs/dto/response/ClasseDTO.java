package com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response;

import lombok.Data;

@Data
public class ClasseDTO {
    private Long id;

    private String idClasse;
    private String nomClasse;
    private String anneeAcademique;

    private Long speciliteId; // ID de la filière associée (relation ManyToOne)
}
