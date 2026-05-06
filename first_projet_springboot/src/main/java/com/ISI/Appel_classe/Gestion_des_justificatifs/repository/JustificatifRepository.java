package com.ISI.Appel_classe.Gestion_des_justificatifs.repository;

import com.ISI.Appel_classe.Gestion_des_justificatifs.enums.StatutValidation;
import com.ISI.Appel_classe.Gestion_des_justificatifs.model.Justificatif;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;


@Repository
public interface JustificatifRepository extends JpaRepository<Justificatif, Long>{

    // Recherche par Presence
    Justificatif findByPresenceId(Long presenceId);

    // Recherche par Etudiant (via la relation Presence -> Etudiant)
    @Query("SELECT j FROM Justificatif j WHERE j.presence.etudiant.id = :etudiantId ORDER BY j.dateSoumission DESC")
    Page<Justificatif> findByEtudiantId(@Param("etudiantId") Long etudiantId, Pageable pageable);

    // Recherche par Etudiant et Statut
    @Query("SELECT j FROM Justificatif j WHERE j.presence.etudiant.id = :etudiantId AND j.statutValidation = :statut ORDER BY j.dateSoumission DESC")
    List<Justificatif> findByEtudiantIdAndStatut(@Param("etudiantId") Long etudiantId, @Param("statut") StatutValidation statut);

    // Recherche par plage de date de soumission
    @Query("SELECT j FROM Justificatif j WHERE j.dateSoumission BETWEEN :dateDebut AND :dateFin ORDER BY j.dateSoumission DESC")
    Page<Justificatif> findByDateSoumissionBetween(@Param("dateDebut") LocalDate dateDebut, @Param("dateFin") LocalDate dateFin, Pageable pageable);

    // Recherche par Etudiant et plage de date de soumission
    @Query("SELECT j FROM Justificatif j WHERE j.presence.etudiant.id = :etudiantId AND j.dateSoumission BETWEEN :dateDebut AND :dateFin ORDER BY j.dateSoumission DESC")
    Page<Justificatif> findByEtudiantIdAndDateSoumissionBetween(
            @Param("etudiantId") Long etudiantId,
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin,
            Pageable pageable
    );

    // Recherche avec filtres multiples pour l'assistant
    @Query("SELECT j FROM Justificatif j WHERE " +
            "(:etudiantId IS NULL OR j.presence.etudiant.id = :etudiantId) AND " +
            "(:dateSoumissionDebut IS NULL OR j.dateSoumission >= :dateSoumissionDebut) AND " +
            "(:dateSoumissionFin IS NULL OR j.dateSoumission <= :dateSoumissionFin) AND " +
            "(:dateValidationDebut IS NULL OR j.dateValidation >= :dateValidationDebut) AND " +
            "(:dateValidationFin IS NULL OR j.dateValidation <= :dateValidationFin) AND " +
            "(:statut IS NULL OR j.statutValidation = :statut) " +
            "ORDER BY j.dateSoumission DESC")
    Page<Justificatif> findAllWithFilters(
            @Param("etudiantId") Long etudiantId,
            @Param("dateSoumissionDebut") LocalDate dateSoumissionDebut,
            @Param("dateSoumissionFin") LocalDate dateSoumissionFin,
            @Param("dateValidationDebut") LocalDate dateValidationDebut,
            @Param("dateValidationFin") LocalDate dateValidationFin,
            @Param("statut") StatutValidation statut,
            Pageable pageable
    );

    // Compter les justificatifs en attente
    long countByStatutValidation(StatutValidation statut);

    // Recherche par Validateur (Assistant)
    @Query("SELECT j FROM Justificatif j WHERE j.validateur.id = :validateurId ORDER BY j.dateSoumission DESC")
    Page<Justificatif> findByValidateurId(@Param("validateurId") Long validateurId, Pageable pageable);
}
