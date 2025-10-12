package com.ISI.Appel_classe.models;

import jakarta.persistence.*;

@Entity
@Table(name = "assistant_pedagogique")
@DiscriminatorValue("ASSISTANT")
@PrimaryKeyJoinColumn(name = "utilisateur_id")
public class AssistantPedagogique extends Utilisateur {

    private String departement;

    // 🔹 Constructeurs
    public AssistantPedagogique() {
    }

    public AssistantPedagogique(String nom, String prenom, String email, String password, 
                               String statut, String departement) {
        super(nom, prenom, email, password, "ASSISTANT", statut);
        this.departement = departement;
    }

    // 🔹 Getters et setters
    public String getDepartement() {
        return departement;
    }

    public void setDepartement(String departement) {
        this.departement = departement;
    }

    // 🔹 Méthodes de logique métier
    public void gererEmploiDuTemps() {
        // TODO: logique de gestion d'emploi du temps
    }

    public void validerJustificatif() {
        // TODO: logique de validation de justificatif
    }

    public void genererRapport() {
        // TODO: logique de génération de rapport
    }
}