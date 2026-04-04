package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
public class AnneeAcademiqueUpdateDTO {
    private String idAnneeAcademique;
    private String nomAnneeAcademique;
    private LocalDate anneeDebut;
    private LocalDate anneeFin;
    private boolean actif;
}
