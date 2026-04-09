package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AnneeAcademiqueDTO {
    private Long id;
    private String idAnneeAcademique;
    private String nomAnneeAcademique;
    private LocalDate anneeDebut;
    private LocalDate anneeFin;
    private boolean actif;
    private int nombreSemestres;
}
