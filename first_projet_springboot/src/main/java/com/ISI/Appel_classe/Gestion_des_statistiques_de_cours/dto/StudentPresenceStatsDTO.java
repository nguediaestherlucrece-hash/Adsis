package com.ISI.Appel_classe.Gestion_des_statistiques_de_cours.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentPresenceStatsDTO {
    private double tauxPresenceGlobal;
    private int totalSeancesAbsentesJustifiees;
    private int totalSeancesAbsentesNonJustifiees;
    private double totalHeuresAbsentesJustifiees;
    private double totalHeuresAbsentesNonJustifiees;
    private int totalRetards;
    private Map<String, Double> tauxPresenceParMatiere; // Code UE -> Taux de présence (%)
    private List<PresenceHistoryDTO> historiqueRetards;
}
