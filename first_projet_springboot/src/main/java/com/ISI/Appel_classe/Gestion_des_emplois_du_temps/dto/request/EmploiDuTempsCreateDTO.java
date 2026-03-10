package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.request;

import lombok.Data;

@Data
public class EmploiDuTempsCreateDTO {

    private String idEmploi;
    private String periode;
    private String creneaux;

    /**
     * Identifiant de la classe associée à cet emploi du temps.
     */
    private Long classeId;

    /**
     * Identifiant de l'assistant pédagogique (gestionnaire) responsable.
     */
    private Long gestionnaireId;
}
