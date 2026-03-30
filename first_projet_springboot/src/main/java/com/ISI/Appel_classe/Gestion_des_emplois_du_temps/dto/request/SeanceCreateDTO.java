package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.request;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class SeanceCreateDTO {
    private String idSeance;
    private LocalDate date;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private boolean statut;

    private Long coursId;
    private List<Long> enseignantIds;
    private Long emploiDuTempsId;
    private String couleur;
    private String groupId;
}
