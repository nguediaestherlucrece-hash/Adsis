package com.ISI.Appel_classe.models;

import jakarta.persistence.*;

@Entity
@Table(name = "seance")
public class Seance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String idSeance;
    private String date;
    private String heureDebut;
    private String heureFin;
    private String statut; // "ouverte", "clôturée"

    // 🔹 Constructeurs
    public Seance() {
    }

    public Seance(String idSeance, String date, String heureDebut, 
                 String heureFin, String statut) {
        this.idSeance = idSeance;
        this.date = date;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.statut = statut;
    }

    // 🔹 Getters et setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdSeance() {
        return idSeance;
    }

    public void setIdSeance(String idSeance) {
        this.idSeance = idSeance;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(String heureDebut) {
        this.heureDebut = heureDebut;
    }

    public String getHeureFin() {
        return heureFin;
    }

    public void setHeureFin(String heureFin) {
        this.heureFin = heureFin;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    // 🔹 Méthodes de logique métier
    public void ouvrirSeance() {
        // TODO: logique d'ouverture de séance
    }

    public void cloturerSeance() {
        // TODO: logique de clôture de séance
    }
}