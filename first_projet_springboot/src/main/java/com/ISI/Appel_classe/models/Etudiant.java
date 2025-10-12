package com.ISI.Appel_classe.models;

import jakarta.persistence.*;

@Entity
@Table(name = "etudiant")
@DiscriminatorValue("ETUDIANT")
@PrimaryKeyJoinColumn(name = "utilisateur_id")
public class Etudiant extends Utilisateur {

    private String matricule;
    private String dateNaissance;
    private String telephone;

    // 🔹 Constructeurs
    public Etudiant() {
    }

    public Etudiant(String nom, String prenom, String email, String password, 
                   String statut, String matricule, String dateNaissance, String telephone) {
        super(nom, prenom, email, password, "ETUDIANT", statut);
        this.matricule = matricule;
        this.dateNaissance = dateNaissance;
        this.telephone = telephone;
    }

    // 🔹 Getters et setters
    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }

    public String getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(String dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    // 🔹 Méthodes de logique métier
    public void consulterHistorique() {
        // TODO: logique de consultation de l'historique
    }

    public void soumettreJustificatif() {
        // TODO: logique de soumission de justificatif
    }
}