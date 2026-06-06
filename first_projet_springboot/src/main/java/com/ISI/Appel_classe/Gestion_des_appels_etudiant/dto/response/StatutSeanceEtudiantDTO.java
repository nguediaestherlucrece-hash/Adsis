package com.ISI.Appel_classe.Gestion_des_appels_etudiant.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Statut d'un appel pour un étudiant donné sur une séance.
 * Retourné par GET /api/appels-etudiant/statut-seance/{seanceId}.
 *
 * Logique d'affichage côté frontend :
 *  - appelOuvert = false → boutons désactivés « En attente d'un appel »
 *  - appelOuvert = true, dejaPresent = false → boutons QR/PIN actifs
 *  - appelOuvert = true, dejaPresent = true  → boutons bloqués + résumé détaillé
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatutSeanceEtudiantDTO {

    /** Un appel est-il actuellement ouvert pour cette séance ? */
    private boolean appelOuvert;

    /** ID de l'appel ouvert (null si aucun) */
    private Long appelId;

    /**
     * L'étudiant a-t-il déjà répondu à l'appel COURANT ?
     * (Repart à false à chaque nouvel appel lancé par l'enseignant)
     */
    private boolean dejaPresent;

    /** Heure de réponse formatée "HH:mm" (null si pas encore répondu) */
    private String heurePresence;

    /** Statut calculé : "présent", "retard", "présent_partiel", "absent" */
    private String statut;

    /** Nombre de créneaux d'1h où l'étudiant est présent */
    private int heuresPresentes;

    /** Nombre de créneaux d'1h où l'étudiant est absent */
    private int heuresAbsentes;

    /** Flag retard calculé automatiquement */
    private boolean retard;

    /**
     * Message lisible pour l'étudiant.
     * Exemples :
     *  "✅ Présent · Enregistré à 08h10"
     *  "⚠️ Arrivé en retard · Enregistré à 08h25 · 3h présent(es)"
     *  "⚡ Présence partielle · 1h absent(e) · 3h présent(es)"
     */
    private String messageStatut;
}
