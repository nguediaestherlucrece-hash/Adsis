package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.repository;

import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.CoursUE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface CoursUERepository extends JpaRepository<CoursUE, Long> {

    List<CoursUE> findBySpecialiteId(Long specialiteId);

    @Query("SELECT c FROM CoursUE c WHERE c.specialite.niveau.filiere.id = :filiereId")
    List<CoursUE> findBySpecialiteNiveauFiliereId(@Param("filiereId") Long filiereId);
}

