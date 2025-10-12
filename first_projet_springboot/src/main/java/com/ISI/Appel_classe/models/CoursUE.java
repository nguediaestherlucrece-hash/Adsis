package com.ISI.Appel_classe.models;

import jakarta.persistence.*;

@Entity
@Table(name = "cours_ue")
public class CoursUE {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String idCours;
    private String codeUE;
    private String intitule;
    private String volumeHoraire;

    // 🔹 Constructeurs
    public CoursUE() {
    }

    public CoursUE(String idCours, String codeUE, String intitule, String volumeHoraire) {
        this.idCours = idCours;
        this.codeUE = codeUE;
        this.intitule = intitule;
        this.volumeHoraire = volumeHoraire;
    }

    // 🔹 Getters et setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdCours() {
        return idCours;
    }

    public void setIdCours(String idCours) {
        this.idCours = idCours;
    }

    public String getCodeUE() {
        return codeUE;
    }

    public void setCodeUE(String codeUE) {
        this.codeUE = codeUE;
    }

    public String getIntitule() {
        return intitule;
    }

    public void setIntitule(String intitule) {
        this.intitule = intitule;
    }

    public String getVolumeHoraire() {
        return volumeHoraire;
    }

    public void setVolumeHoraire(String volumeHoraire) {
        this.volumeHoraire = volumeHoraire;
    }

    // 🔹 Méthodes de logique métier
    public void associerClasse() {
        // TODO: logique d'association avec une classe
    }

    public void associerEnseignant() {
        // TODO: logique d'association avec un enseignant
    }
}