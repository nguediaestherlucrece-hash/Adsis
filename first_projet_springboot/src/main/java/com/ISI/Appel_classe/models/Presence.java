package com.ISI.Appel_classe.models;

import jakarta.persistence.*;

@Entity
@Table(name = "presence")
public class Presence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String idPresence;
    private String horodatage;
    private String statut; // "présent", "absent", "retard"
    private String modeEnregistrement; // "QR", "PIN", "manuel"
    private String localisation; // Optionnel

    // 🔹 Constructeurs
    public Presence() {
    }

    public Presence(String idPresence, String horodatage, String statut, 
                   String modeEnregistrement, String localisation) {
        this.idPresence = idPresence;
        this.horodatage = horodatage;
        this.statut = statut;
        this.modeEnregistrement = modeEnregistrement;
        this.localisation = localisation;
    }

    // 🔹 Getters et setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdPresence() {
        return idPresence;
    }

    public void setIdPresence(String idPresence) {
        this.idPresence = idPresence;
    }

    public String getHorodatage() {
        return horodatage;
    }

    public void setHorodatage(String horodatage) {
        this.horodatage = horodatage;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getModeEnregistrement() {
        return modeEnregistrement;
    }

    public void setModeEnregistrement(String modeEnregistrement) {
        this.modeEnregistrement = modeEnregistrement;
    }

    public String getLocalisation() {
        return localisation;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }

    // 🔹 Méthodes de logique métier
    public void enregistrerPresence() {
        // TODO: logique d'enregistrement de présence
    }

    public void modifierStatut() {
        // TODO: logique de modification de statut
    }
}