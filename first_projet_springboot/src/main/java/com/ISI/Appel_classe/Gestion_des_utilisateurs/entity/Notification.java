package com.Gestion_des_utilisateurs.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notification")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String idNotification;
    private String message;
    private String destinataire;
    private String type; // "alerte absence", "rappel", "validation justificatif"
    private String statut; // "envoyée", "en attente"

    // 🔹 Méthodes de logique métier
    public void envoyer() {
        // TODO: logique d'envoi
    }

    public void marquerCommeLue() {
        // TODO: logique de marquage comme lu
    }
}
