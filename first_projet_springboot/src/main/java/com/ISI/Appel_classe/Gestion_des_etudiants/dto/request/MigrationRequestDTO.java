package com.ISI.Appel_classe.Gestion_des_etudiants.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class MigrationRequestDTO {
    private List<Long> etudiantIds;
    private Long classeDestinationId;
    private String motif;
}
