package com.ISI.Appel_classe.models;

import jakarta.persistence.*;

@Entity
@Table(name = "justificatif")
public class Justificatif {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String idJustificatif;
    private String motif;
    private String dateSoumission;
    private String fichier; // Lien ou chemin du fichier
    private String statut; // "en attente", "validé", "rejeté"

    // 🔹 Constructeurs
    public Justificatif() {
    }

    public Justificatif(String idJustificatif, String motif, String dateSoumission, 
                       String fichier, String statut) {
        this.idJustificatif = idJustificatif;
        this.motif = motif;
        this.dateSoumission = dateSoumission;
        this.fichier = fichier;
        this.statut = statut;
    }

    // 🔹 Getters et setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdJustificatif() {
        return idJustificatif;
    }

    public void setIdJustificatif(String idJustificatif) {
        this.idJustificatif = idJustificatif;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public String getDateSoumission() {
        return dateSoumission;
    }

    public void setDateSoumission(String dateSoumission) {
        this.dateSoumission = dateSoumission;
    }

    public String getFichier() {
        return fichier;
    }

    public void setFichier(String fichier) {
        this.fichier = fichier;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    // 🔹 Méthodes de logique métier
    public void soumettre() {
        // TODO: logique de soumission
    }

    public void valider() {
        // TODO: logique de validation
    }

    public void rejeter() {
        // TODO: logique de rejet
    }
}