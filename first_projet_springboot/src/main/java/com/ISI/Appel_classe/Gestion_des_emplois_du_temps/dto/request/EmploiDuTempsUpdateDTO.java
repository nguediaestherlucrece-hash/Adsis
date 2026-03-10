package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.request;

import lombok.Data;

@Data
public class EmploiDuTempsUpdateDTO {

    private String idEmploi;
    private String periode;
    private String creneaux;

    private Long classeId;
    private Long gestionnaireId;
}
