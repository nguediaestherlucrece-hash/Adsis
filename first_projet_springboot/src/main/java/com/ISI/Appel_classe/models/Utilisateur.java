package com.ISI.Appel_classe.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "utilisateur")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "type_utilisateur", discriminatorType = DiscriminatorType.STRING)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String prenom;

    @Column(unique = true)
    private String email;

    private String password;
    private String role;

    @Enumerated(EnumType.STRING)
    private Statut statut;

    // 🔹 Méthodes de logique métier
    public void seConnecter() {
        // TODO: logique de connexion
    }

    public void seDeconnecter() {
        // TODO: logique de déconnexion
    }

    public void modifierCompte() {
        // TODO: logique de modification
    }
}