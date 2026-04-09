package com.ISI.Appel_classe.Gestion_des_utilisateurs.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "administrateur")
@DiscriminatorValue("ADMIN")
@PrimaryKeyJoinColumn(name = "utilisateur_id")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Administrateur extends Utilisateur {

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
