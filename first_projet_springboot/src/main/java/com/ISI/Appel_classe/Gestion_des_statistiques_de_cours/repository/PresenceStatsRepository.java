package com.ISI.Appel_classe.Gestion_des_statistiques_de_cours.repository;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Presence;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.Seance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PresenceStatsRepository extends JpaRepository<Presence, Long> {

    @Query("SELECT DISTINCT p FROM Presence p " +
            "JOIN FETCH p.seance s " +
            "LEFT JOIN FETCH s.cours c " +
            "LEFT JOIN FETCH p.justificatif j " +
            "WHERE p.etudiant.id = :etudiantId " +
            "AND (:date IS NULL OR s.date = :date) " +
            "AND (:dateDebut IS NULL OR :dateFin IS NULL OR s.date BETWEEN :dateDebut AND :dateFin) " +
            "AND (:semestreId IS NULL OR s.emploiDuTemps.semestre.id = :semestreId) " +
            "AND (:coursId IS NULL OR s.cours.id = :coursId)")
    Page<Presence> findFilteredPresences(
            @Param("etudiantId") Long etudiantId,
            @Param("date") LocalDate date,
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin,
            @Param("semestreId") Long semestreId,
            @Param("coursId") Long coursId,
            Pageable pageable
    );

    @Query("SELECT DISTINCT p FROM Presence p " +
            "JOIN FETCH p.seance s " +
            "LEFT JOIN FETCH s.cours c " +
            "LEFT JOIN FETCH p.justificatif j " +
            "WHERE p.etudiant.id = :etudiantId " +
            "AND (p.statut = 'retard' OR p.retard = true)")
    Page<Presence> findRetardsByEtudiantId(
            @Param("etudiantId") Long etudiantId,
            Pageable pageable
    );

    @Query("SELECT DISTINCT p FROM Presence p " +
            "JOIN FETCH p.seance s " +
            "LEFT JOIN FETCH s.cours c " +
            "LEFT JOIN FETCH p.justificatif j " +
            "WHERE p.etudiant.id = :etudiantId")
    List<Presence> findAllByEtudiantId(@Param("etudiantId") Long etudiantId);

    @Query("SELECT DISTINCT p FROM Presence p " +
            "JOIN FETCH p.seance s " +
            "LEFT JOIN FETCH s.cours c " +
            "LEFT JOIN FETCH p.justificatif j " +
            "WHERE p.etudiant.classe.id = :classeId " +
            "AND (:dateDebut IS NULL OR :dateFin IS NULL OR s.date BETWEEN :dateDebut AND :dateFin) " +
            "AND (:semestreId IS NULL OR s.emploiDuTemps.semestre.id = :semestreId)")
    List<Presence> findClassPresences(
            @Param("classeId") Long classeId,
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin,
            @Param("semestreId") Long semestreId
    );

    @Query("SELECT DISTINCT s FROM Seance s " +
            "LEFT JOIN FETCH s.cours c " +
            "JOIN s.enseignants e " +
            "WHERE e.id = :enseignantId " +
            "AND (:coursId IS NULL OR s.cours.id = :coursId) " +
            "AND (:dateDebut IS NULL OR :dateFin IS NULL OR s.date BETWEEN :dateDebut AND :dateFin)")
    Page<Seance> findTeacherSeances(
            @Param("enseignantId") Long teacherId,
            @Param("coursId") Long coursId,
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin,
            Pageable pageable
    );

    @Query("SELECT DISTINCT s FROM Seance s " +
            "LEFT JOIN FETCH s.cours c " +
            "JOIN s.enseignants e " +
            "WHERE e.id = :enseignantId " +
            "AND (:coursId IS NULL OR s.cours.id = :coursId) " +
            "AND (:dateDebut IS NULL OR :dateFin IS NULL OR s.date BETWEEN :dateDebut AND :dateFin)")
    List<Seance> findTeacherSeancesList(
            @Param("enseignantId") Long teacherId,
            @Param("coursId") Long coursId,
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin
    );
}
