package com.ISI.Appel_classe.Gestion_des_utilisateurs.model;

import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.Seance;
import com.ISI.Appel_classe.Gestion_des_etudiants.model.Etudiant;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.CascadeType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "presence")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Presence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String idPresence;

    private LocalDateTime horodatage;
    private String statut; // "présent", "absent", "retard", "présent_partiel"
    private String modeEnregistrement; // "QR", "PIN", "manuel"
    private String localisation; // Optionnel
    private boolean retard; // Flag global retard pour la séance

    // 🔹 Relations JPA
    @ManyToOne
    @JoinColumn(name = "etudiant_id")
    private Etudiant etudiant;

    @ManyToOne
    @JoinColumn(name = "seance_id")
    private Seance seance;

    @ManyToOne
    @JoinColumn(name = "appel_id")
    private Appel appel;

    @OneToOne(mappedBy = "presence", cascade = CascadeType.ALL)
    private Justificatif justificatif;

    @OneToMany(mappedBy = "presence", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PresenceHoraire> presencesHoraires = new ArrayList<>();

    // 🔹 Méthodes utilitaires

    /**
     * Compte le nombre d'heures absentes (créneaux où present=false)
     */
    public int getHeuresAbsentes() {
        return (int) presencesHoraires.stream()
            .filter(ph -> !ph.isPresent())
            .count();
    }

    /**
     * Compte le nombre d'heures présentes (créneaux où present=true)
     */
    public int getHeuresPresentes() {
        return (int) presencesHoraires.stream()
            .filter(PresenceHoraire::isPresent)
            .count();
    }

    /**
     * Recalcule le statut global automatiquement basé sur les créneaux horaires.
     * - Toutes les heures présentes → "présent"
     * - Toutes les heures absentes → "absent"
     * - Mix → "présent_partiel"
     */
    public void recalculerStatut() {
        if (presencesHoraires.isEmpty()) return;

        long totalPresent = presencesHoraires.stream().filter(PresenceHoraire::isPresent).count();
        long total = presencesHoraires.size();

        if (totalPresent == total) {
            this.statut = "présent";
        } else if (totalPresent == 0) {
            this.statut = "absent";
        } else {
            this.statut = "présent_partiel";
        }
    }
}
