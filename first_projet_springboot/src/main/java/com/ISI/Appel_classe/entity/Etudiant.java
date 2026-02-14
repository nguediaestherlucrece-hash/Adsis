package com.ISI.Appel_classe.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "etudiant")
@DiscriminatorValue("ETUDIANT")
@PrimaryKeyJoinColumn(name = "utilisateur_id")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Etudiant extends Utilisateur {

    private String matricule;
    private String dateNaissance; // À convertir en LocalDate plus tard si nécessaire
    private String telephone;

    // 🔹 Relations JPA
    @ManyToOne
    @JoinColumn(name = "classe_id")
    private Classe classe;

    @ManyToMany(mappedBy = "etudiants")
    private List<Parent> parents = new ArrayList<>();

    @OneToMany(mappedBy = "etudiant", cascade = CascadeType.ALL)
    private List<Presence> presences = new ArrayList<>();

    // 🔹 Méthodes de logique métier (conservées)
    public void consulterHistorique() {
        // TODO: logique de consultation d'historique
    }

    public void soumettreJustificatif() {
        // TODO: logique de soumission de justificatif
    }
}
