package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.repository;

import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.AnneeAcademique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface AnneeAcademiqueRepository extends JpaRepository<AnneeAcademique, Long> {
    Optional<AnneeAcademique> findByIdAnneeAcademique(String idAnneeAcademique);

    Optional<AnneeAcademique> findByActifTrue();

    boolean existsByAnneeDebutAfter(LocalDate date);

    @Modifying
    @Query("UPDATE AnneeAcademique a SET a.actif = false")
    void deactivateAll();
}