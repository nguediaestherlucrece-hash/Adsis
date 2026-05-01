package com.ISI.Appel_classe.Gestion_des_appels_etudiant.model;

import com.ISI.Appel_classe.Gestion_des_etudiants.model.Etudiant;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Appel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "code_presence")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodePresence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String codePIN;

    @Column(unique = true)
    private String qrToken;

    private String type; // "PIN", "QR", "BOTH"

    private LocalDateTime dateCreation;

    private LocalDateTime dateExpiration;

    private String statut; // "actif", "utilisé", "expiré"

    private boolean utilise;

    // Relations
    @ManyToOne
    @JoinColumn(name = "etudiant_id")
    private Etudiant etudiant;

    @ManyToOne
    @JoinColumn(name = "seance_id")
    private com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.Seance seance;

    @ManyToOne
    @JoinColumn(name = "appel_id", nullable = true)
    private Appel appel; // Appel de l'enseignant auquel se rapporte ce code si applicable

    /**
     * Vérifie si le code est expiré
     */
    public boolean estExpire() {
        return LocalDateTime.now().isAfter(dateExpiration) || "expiré".equals(statut);
    }

    /**
     * Marque le code comme utilisé
     */
    public void marquerUtilise() {
        this.utilise = true;
        this.statut = "utilisé";
    }

    /**
     * Vérifie si le code est valide et utilisable
     */
    public boolean estValide() {
        return !estExpire() && !utilise && "actif".equals(statut);
    }
}
