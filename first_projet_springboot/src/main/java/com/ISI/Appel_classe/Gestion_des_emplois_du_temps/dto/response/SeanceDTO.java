package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class SeanceDTO {
    private Long id;
    private String idSeance;
    private LocalDate date;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private boolean statut;

    private Long coursId;
    private Long enseignantId;
    private Long emploiDuTempsId;

}
