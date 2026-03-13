package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.request;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class EmploiDuTempsUpdateDTO {

    private String idEmploi;
    private LocalDate dateDebut;
    private LocalTime dateFin;

    private Long semestreId;
    private Long classeId;
    private Long gestionnaireId;
}
