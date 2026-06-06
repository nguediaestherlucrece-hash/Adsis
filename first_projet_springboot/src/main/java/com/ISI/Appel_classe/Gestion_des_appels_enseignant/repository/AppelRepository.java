package com.ISI.Appel_classe.Gestion_des_appels_enseignant.repository;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Appel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppelRepository extends JpaRepository<Appel, Long> {

    Optional<Appel> findByIdAppel(String idAppel);

    @Query("SELECT a FROM Appel a WHERE a.seance.id = :seanceId AND a.statut = 'ouvert'")
    Optional<Appel> findAppelOuvertBySeanceId(@Param("seanceId") Long seanceId);

    @Query("SELECT a FROM Appel a WHERE a.seance.id = :seanceId ORDER BY a.dateHeureDebut DESC")
    List<Appel> findBySeanceIdOrderByDateHeureDebutDesc(@Param("seanceId") Long seanceId);

    /** Tous les appels d'une séance (ouverts + clôturés), ordre chronologique ASC */
    @Query("SELECT a FROM Appel a WHERE a.seance.id = :seanceId ORDER BY a.dateHeureDebut ASC")
    List<Appel> findAllBySeanceIdOrderByDateHeureDebutAsc(@Param("seanceId") Long seanceId);

    /** Uniquement les appels clôturés d'une séance, ordre chronologique ASC */
    @Query("SELECT a FROM Appel a WHERE a.seance.id = :seanceId AND a.statut = 'clôturé' ORDER BY a.dateHeureDebut ASC")
    List<Appel> findAppelsClotures(@Param("seanceId") Long seanceId);

    /**
     * Compte le nombre d'appels lancés pour cette séance avant (ou à) la date/heure donnée.
     * Utilisé pour déterminer si un appel est le 1er (rang = 1) → calcul du retard.
     */
    @Query("SELECT COUNT(a) FROM Appel a WHERE a.seance.id = :seanceId AND a.dateHeureDebut <= :dateHeureDebut")
    long countAppelsAvant(@Param("seanceId") Long seanceId, @Param("dateHeureDebut") LocalDateTime dateHeureDebut);
}
