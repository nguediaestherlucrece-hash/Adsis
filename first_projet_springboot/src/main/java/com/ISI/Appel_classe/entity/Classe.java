package com.ISI.Appel_classe.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "classe")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Classe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String idClasse;
    private String nomClasse;
    private String niveau;
    private String anneeAcademique;

    // 🔹 Relations JPA
    @ManyToOne
    @JoinColumn(name = "filiere_id")
    private Filiere filiere;

    @ManyToOne
    @JoinColumn(name = "etablissement")
    private Etablissement etablissement;

    @OneToMany(mappedBy = "classe", cascade =CascadeType.ALL)
    private List<Etudiant> etudiants = new ArrayList<>();
    @ManyToMany
    @JoinTable(name = "classe_cours", joinColumns = @JoinColumn(name = "classe_id"), inverseJoinColumns = @JoinColumn(name = "cours_id"))
    private List<CoursUE> cours = new ArrayList<>();
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "emploi_du_temps_id", referencedColumnName = "id")
    private EmploiDuTemps emploiDuTemps;
    // 🔹 Méthodes métier (conservées)

    public void ajouterEtudiant(Etudiant etudiant) {
        if (etudiants == null)
            etudiants = new ArrayList<>();
        etudiants.add(etudiant);
        etudiant.setClasse(this);
    }
}
