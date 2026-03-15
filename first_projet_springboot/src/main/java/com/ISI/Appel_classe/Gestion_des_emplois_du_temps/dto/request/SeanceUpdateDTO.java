package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class SeanceUpdateDTO {
    private String idSeance;
    private LocalDate date;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private boolean statut;

    private Long coursId;
    private Long enseignantId;
    private Long emploiDuTempsId;
}
