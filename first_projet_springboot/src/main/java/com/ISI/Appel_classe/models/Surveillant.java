package com.ISI.Appel_classe.models;

import jakarta.persistence.*;

@Entity
@Table(name = "surveillant")
@DiscriminatorValue("SURVEILLANT")
@PrimaryKeyJoinColumn(name = "utilisateur_id")
public class Surveillant extends Utilisateur {

    private String affectation;

    // 🔹 Constructeurs
    public Surveillant() {
    }

    public Surveillant(String nom, String prenom, String email, String password, 
                      String statut, String affectation) {
        super(nom, prenom, email, password, "SURVEILLANT", statut);
        this.affectation = affectation;
    }

    // 🔹 Getters et setters
    public String getAffectation() {
        return affectation;
    }

    public void setAffectation(String affectation) {
        this.affectation = affectation;
    }

    // 🔹 Méthodes de logique métier
    public void lancerSeance() {
        // TODO: logique de lancement de séance
    }

    public void controlerPresence() {
        // TODO: logique de contrôle de présence
    }
}