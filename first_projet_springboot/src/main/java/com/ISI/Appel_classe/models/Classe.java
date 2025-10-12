package com.ISI.Appel_classe.models;

import jakarta.persistence.*;

@Entity
@Table(name = "classe")
public class Classe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String idClasse;
    private String nomClasse;
    private String niveau;
    private String anneeAcademique;

    // 🔹 Constructeurs
    public Classe() {
    }

    public Classe(String idClasse, String nomClasse, String niveau, String anneeAcademique) {
        this.idClasse = idClasse;
        this.nomClasse = nomClasse;
        this.niveau = niveau;
        this.anneeAcademique = anneeAcademique;
    }

    // 🔹 Getters et setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdClasse() {
        return idClasse;
    }

    public void setIdClasse(String idClasse) {
        this.idClasse = idClasse;
    }

    public String getNomClasse() {
        return nomClasse;
    }

    public void setNomClasse(String nomClasse) {
        this.nomClasse = nomClasse;
    }

    public String getNiveau() {
        return niveau;
    }

    public void setNiveau(String niveau) {
        this.niveau = niveau;
    }

    public String getAnneeAcademique() {
        return anneeAcademique;
    }

    public void setAnneeAcademique(String anneeAcademique) {
        this.anneeAcademique = anneeAcademique;
    }

    // 🔹 Méthodes de logique métier
    public void ajouterEtudiant() {
        // TODO: logique d'ajout d'étudiant
    }

    public void affecterEnseignant() {
        // TODO: logique d'affectation d'enseignant
    }
}