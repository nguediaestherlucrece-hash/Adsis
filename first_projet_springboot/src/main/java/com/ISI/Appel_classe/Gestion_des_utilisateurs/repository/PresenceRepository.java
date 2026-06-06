package com.ISI.Appel_classe.Gestion_des_utilisateurs.repository;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Presence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PresenceRepository extends JpaRepository<Presence, Long> {

    List<Presence> findByEtudiantId(Long etudiantId);

    /**
     * Charge toutes les présences d'un étudiant avec les relations
     * seance, seance.cours et presencesHoraires en une seule requête
     * (JOIN FETCH) pour éviter les NPE dues au lazy loading hors transaction.
     */
    @Query("SELECT DISTINCT p FROM Presence p "
         + "LEFT JOIN FETCH p.seance s "
         + "LEFT JOIN FETCH s.cours "
         + "LEFT JOIN FETCH p.presencesHoraires "
         + "LEFT JOIN FETCH p.justificatif "
         + "WHERE p.etudiant.id = :etudiantId")
    List<Presence> findByEtudiantIdFetchSeanceCours(@Param("etudiantId") Long etudiantId);

    long countByEtudiantIdAndSeance_EmploiDuTemps_ClasseId(Long etudiantId, Long classeId);

    long countByEtudiantIdAndSeance_EmploiDuTemps_ClasseIdAndStatut(Long etudiantId, Long classeId, String statut);

    long countByEtudiantIdAndSeance_EmploiDuTemps_SemestreId(Long etudiantId, Long semestreId);

    long countByEtudiantIdAndSeance_EmploiDuTemps_SemestreIdAndStatut(Long etudiantId, Long semestreId, String statut);

    // Gestion des appels
    List<Presence> findByAppelId(Long appelId);

    Optional<Presence> findByAppelIdAndEtudiantId(Long appelId, Long etudiantId);

    Optional<Presence> findBySeanceIdAndEtudiantId(Long seanceId, Long etudiantId);

    long countByAppelIdAndStatut(Long appelId, String statut);

    /** Toutes les présences d'une séance, tous appels confondus */
    List<Presence> findBySeanceId(Long seanceId);

    /**
     * Présences positives (présent / retard / présent_partiel) enregistrées
     * sur des appels antérieurs au nouvel appel en cours de démarrage.
     * Utilisé pour pré-charger la grille de l'enseignant lors du 2e appel.
     */
    @Query("SELECT p FROM Presence p " +
           "WHERE p.seance.id = :seanceId " +
           "AND p.appel.dateHeureDebut < :heureDebutNouvelAppel")
    List<Presence> findPresencesAvant(
            @Param("seanceId") Long seanceId,
            @Param("heureDebutNouvelAppel") LocalDateTime heureDebutNouvelAppel);
    /**
     * Dernière présence enregistrée pour un étudiant sur une séance (tous appels confondus).
     * Utilisé pour afficher le dernier statut connu quand aucun appel n'est ouvert.
     */
    Optional<Presence> findTopBySeanceIdAndEtudiantIdOrderByIdDesc(Long seanceId, Long etudiantId);
}
