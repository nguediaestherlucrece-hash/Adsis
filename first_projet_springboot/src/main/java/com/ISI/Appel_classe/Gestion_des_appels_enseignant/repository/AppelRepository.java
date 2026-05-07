package com.ISI.Appel_classe.Gestion_des_appels_enseignant.repository;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Appel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppelRepository extends JpaRepository<Appel, Long> {
    
    Optional<Appel> findByIdAppel(String idAppel);
    
    @Query("SELECT a FROM Appel a WHERE a.seance.id = :seanceId AND a.statut = 'ouvert'")
    Optional<Appel> findAppelOuvertBySeanceId(@Param("seanceId") Long seanceId);
    
    @Query("SELECT a FROM Appel a WHERE a.seance.id = :seanceId ORDER BY a.dateHeureDebut DESC")
    List<Appel> findBySeanceIdOrderByDateHeureDebutDesc(@Param("seanceId") Long seanceId);
}
