package com.ISI.Appel_classe.models;

import jakarta.persistence.*;

@Entity
@Table(name = "administrateur")
@DiscriminatorValue("ADMIN")
@PrimaryKeyJoinColumn(name = "utilisateur_id")
public class Administrateur extends Utilisateur {

    private String niveauAcces; // "super-admin", "admin-local"

    // 🔹 Constructeurs
    public Administrateur() {
    }

    public Administrateur(String nom, String prenom, String email, String password, 
                         String statut, String niveauAcces) {
        super(nom, prenom, email, password, "ADMIN", statut);
        this.niveauAcces = niveauAcces;
    }

    // 🔹 Getters et setters
    public String getNiveauAcces() {
        return niveauAcces;
    }

    public void setNiveauAcces(String niveauAcces) {
        this.niveauAcces = niveauAcces;
    }

    // 🔹 Méthodes de logique métier
    public void gererUtilisateurs() {
        // TODO: logique de gestion des utilisateurs
    }

    public void configurerSysteme() {
        // TODO: logique de configuration du système
    }

    public void consulterLogs() {
        // TODO: logique de consultation des logs
    }
}