package com.ISI.Appel_classe.models;

import jakarta.persistence.*;

@Entity
@Table(name = "parent")
@DiscriminatorValue("PARENT")
@PrimaryKeyJoinColumn(name = "utilisateur_id")
public class Parent extends Utilisateur {

    private String lienParental; // "père", "mère", "tuteur"
    private String enfantsAssocies; // Liste des étudiants associés

    // 🔹 Constructeurs
    public Parent() {
    }

    public Parent(String nom, String prenom, String email, String password, 
                 String statut, String lienParental, String enfantsAssocies) {
        super(nom, prenom, email, password, "PARENT", statut);
        this.lienParental = lienParental;
        this.enfantsAssocies = enfantsAssocies;
    }

    // 🔹 Getters et setters
    public String getLienParental() {
        return lienParental;
    }

    public void setLienParental(String lienParental) {
        this.lienParental = lienParental;
    }

    public String getEnfantsAssocies() {
        return enfantsAssocies;
    }

    public void setEnfantsAssocies(String enfantsAssocies) {
        this.enfantsAssocies = enfantsAssocies;
    }

    // 🔹 Méthodes de logique métier
    public void consulterHistoriqueEnfant() {
        // TODO: logique de consultation de l'historique de l'enfant
    }

    public void soumettreJustificatifEnfant() {
        // TODO: logique de soumission de justificatif pour l'enfant
    }
}