package com.ISI.Appel_classe.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "emploi_du_temps")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmploiDuTemps {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String idEmploi;
    private String periode; // Ex: "Semestre 1 2023-2024"
    private String creneaux; // JSON ou texte structuré

    // 🔹 Relations JPA
    @OneToOne(mappedBy = "emploiDuTemps") // Classe reference EmploiDuTemps via emploiDuTemps
    private Classe classe;

    @ManyToOne
    @JoinColumn(name = "gestionnaire_id")
    private AssistantPedagogique gestionnaire;

    @OneToMany(mappedBy = "emploiDuTemps")
    private List<Seance> seances = new ArrayList<>();

    // 🔹 Méthodes de logique métier
    public void planifierSeance() {
        // TODO: logique de planification de séance
    }

    public void modifierSeance() {
        // TODO: logique de modification de séance
    }
}
