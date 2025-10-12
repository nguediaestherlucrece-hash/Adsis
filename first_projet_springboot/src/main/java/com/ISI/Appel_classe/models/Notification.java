package com.ISI.Appel_classe.models;

import jakarta.persistence.*;

@Entity
@Table(name = "notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String idNotification;
    private String message;
    private String destinataire;
    private String type; // "alerte absence", "rappel", "validation justificatif"
    private String statut; // "envoyée", "en attente"

    // 🔹 Constructeurs
    public Notification() {
    }

    public Notification(String idNotification, String message, String destinataire, 
                       String type, String statut) {
        this.idNotification = idNotification;
        this.message = message;
        this.destinataire = destinataire;
        this.type = type;
        this.statut = statut;
    }

    // 🔹 Getters et setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdNotification() {
        return idNotification;
    }

    public void setIdNotification(String idNotification) {
        this.idNotification = idNotification;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDestinataire() {
        return destinataire;
    }

    public void setDestinataire(String destinataire) {
        this.destinataire = destinataire;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    // 🔹 Méthodes de logique métier
    public void envoyer() {
        // TODO: logique d'envoi
    }

    public void marquerCommeLue() {
        // TODO: logique de marquage comme lu
    }
}