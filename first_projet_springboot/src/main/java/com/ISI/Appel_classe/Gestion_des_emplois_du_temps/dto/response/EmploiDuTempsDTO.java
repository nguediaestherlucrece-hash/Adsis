package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.response;

import lombok.Data;

@Data
public class EmploiDuTempsDTO {

    private Long id;
    private String idEmploi;
    private String periode;
    private String creneaux;

    /**
     * Informations simplifiees sur la classe.
     */
    private Long classeId;
    private String classeNom;

    /**
     * Informations simplifiees sur le gestionnaire.
     */
    private Long gestionnaireId;
    private String gestionnaireNomComplet;

    /**
     * Nombre de séances planifiees de l'emploi du temps.
     */
    private Integer nombreSeances;
}
