package com.ISI.Appel_classe.Gestion_des_appels_enseignant.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreneauPresenceDTO {
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private boolean present;
}
