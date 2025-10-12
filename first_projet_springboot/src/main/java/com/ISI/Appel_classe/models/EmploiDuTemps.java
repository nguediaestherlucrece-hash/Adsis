package com.ISI.Appel_classe.models;

import jakarta.persistence.*;

@Entity
@Table(name = "emploi_du_temps")
public class EmploiDuTemps {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String idEmploi;
    private String periode; // "semaine", "semestre"
    private String creneaux; // Liste des séances

    // 🔹 Constructeurs
    public EmploiDuTemps() {
    }

    public EmploiDuTemps(String idEmploi, String periode, String creneaux) {
        this.idEmploi = idEmploi;
        this.periode = periode;
        this.creneaux = creneaux;
    }

    // 🔹 Getters et setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdEmploi() {
        return idEmploi;
    }

    public void setIdEmploi(String idEmploi) {
        this.idEmploi = idEmploi;
    }

    public String getPeriode() {
        return periode;
    }

    public void setPeriode(String periode) {
        this.periode = periode;
    }

    public String getCreneaux() {
        return creneaux;
    }

    public void setCreneaux(String creneaux) {
        this.creneaux = creneaux;
    }

    // 🔹 Méthodes de logique métier
    public void planifierSeance() {
        // TODO: logique de planification de séance
    }

    public void modifierSeance() {
        // TODO: logique de modification de séance
    }
}