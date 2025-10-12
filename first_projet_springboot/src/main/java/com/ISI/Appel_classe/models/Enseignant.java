package com.ISI.Appel_classe.models;

import jakarta.persistence.*;

@Entity
@Table(name = "enseignant")
@DiscriminatorValue("ENSEIGNANT")
@PrimaryKeyJoinColumn(name = "utilisateur_id")
public class Enseignant extends Utilisateur {

    private String specialite;
    private String grade;

    // 🔹 Constructeurs
    public Enseignant() {
    }

    public Enseignant(String nom, String prenom, String email, String password, 
                     String statut, String specialite, String grade) {
        super(nom, prenom, email, password, "ENSEIGNANT", statut);
        this.specialite = specialite;
        this.grade = grade;
    }

    // 🔹 Getters et setters
    public String getSpecialite() {
        return specialite;
    }

    public void setSpecialite(String specialite) {
        this.specialite = specialite;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    // 🔹 Méthodes de logique métier
    public void lancerSeance() {
        // TODO: logique de lancement de séance
    }

    public void enregistrerPresence() {
        // TODO: logique d'enregistrement de présence
    }

    public void consulterStatistiquesCours() {
        // TODO: logique de consultation des statistiques
    }
}