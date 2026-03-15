package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class SemestreDTO {
    private Long id;
    private String idSemestre;
    private String nomSemestre;
    private boolean statut;
    private Long anneeAcademiqueId;
    private String anneeNom;
}
