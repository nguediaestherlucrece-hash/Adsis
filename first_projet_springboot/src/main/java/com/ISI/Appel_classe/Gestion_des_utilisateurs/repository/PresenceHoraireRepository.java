package com.ISI.Appel_classe.Gestion_des_utilisateurs.repository;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.PresenceHoraire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PresenceHoraireRepository extends JpaRepository<PresenceHoraire, Long> {

    List<PresenceHoraire> findByPresenceId(Long presenceId);

    // Pour les calculs de malus futurs
    @Query("SELECT ph FROM PresenceHoraire ph " +
           "WHERE ph.presence.etudiant.id = :etudiantId " +
           "AND ph.presence.seance.cours.id = :coursId " +
           "AND ph.present = false")
    List<PresenceHoraire> findHeuresAbsentesByEtudiantAndCours(
        @Param("etudiantId") Long etudiantId,
        @Param("coursId") Long coursId);

    // Compter les heures manquées par matière
    @Query("SELECT COUNT(ph) FROM PresenceHoraire ph " +
           "WHERE ph.presence.etudiant.id = :etudiantId " +
           "AND ph.presence.seance.cours.id = :coursId " +
           "AND ph.present = false")
    long countHeuresAbsentesByEtudiantAndCours(
        @Param("etudiantId") Long etudiantId,
        @Param("coursId") Long coursId);
}
