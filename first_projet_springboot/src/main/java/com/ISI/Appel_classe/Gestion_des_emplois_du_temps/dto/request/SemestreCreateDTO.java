package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class SemestreCreateDTO {
    private String idSemestre;
    private String nomSemestre;
    private boolean statut;

    private Long anneeAcademiqueId;
}
