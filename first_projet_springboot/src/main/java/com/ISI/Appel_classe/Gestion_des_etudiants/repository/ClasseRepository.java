package com.ISI.Appel_classe.Gestion_des_etudiants.repository;

import com.ISI.Appel_classe.Gestion_des_etudiants.model.Classe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface ClasseRepository extends JpaRepository<Classe, Long> {

    @Query("SELECT c FROM Classe c WHERE c.specialite.niveau.filiere.id = :filiereId")
    List<Classe> findBySpecialiteNiveauFiliereId(@Param("filiereId") Long filiereId);
}
