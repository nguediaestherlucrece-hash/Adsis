package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.repository;

import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.Seance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeanceRepository extends JpaRepository<Seance, Long> {
    Optional<Seance> findByIdSeance(String idSeance);
    List<Seance> findByEmploiDuTempsId(Long emploiDuTempsId);
    
    @Query("SELECT DISTINCT s FROM Seance s " +
           "JOIN s.enseignants e " +
           "WHERE e.id = :enseignantId " +
           "AND s.date = :date " +
           "ORDER BY s.heureDebut")
    List<Seance> findByEnseignantIdAndDate(@Param("enseignantId") Long enseignantId, 
                                            @Param("date") LocalDate date);
    
    @Query("SELECT DISTINCT s FROM Seance s " +
           "JOIN s.enseignants e " +
           "WHERE e.id = :enseignantId " +
           "AND s.date BETWEEN :dateDebut AND :dateFin " +
           "ORDER BY s.date, s.heureDebut")
    List<Seance> findByEnseignantIdAndDateBetween(@Param("enseignantId") Long enseignantId,
                                                    @Param("dateDebut") LocalDate dateDebut,
                                                    @Param("dateFin") LocalDate dateFin);

    /**
     * Récupère les séances d'une classe sur une plage de dates
     * Utilisé pour l'emploi du temps et les prochains cours de l'étudiant
     */
    @Query("SELECT s FROM Seance s " +
           "WHERE s.emploiDuTemps.classe.id = :classeId " +
           "AND s.date BETWEEN :dateDebut AND :dateFin " +
           "ORDER BY s.date, s.heureDebut")
    List<Seance> findByClasseIdAndDateBetween(@Param("classeId") Long classeId,
                                              @Param("dateDebut") LocalDate dateDebut,
                                              @Param("dateFin") LocalDate dateFin);

    @Query("SELECT DISTINCT s FROM Seance s " +
           "JOIN s.surveillants sur " +
           "WHERE sur.id = :surveillantId " +
           "AND s.date = :date " +
           "ORDER BY s.heureDebut")
    List<Seance> findBySurveillantIdAndDate(@Param("surveillantId") Long surveillantId,
                                             @Param("date") LocalDate date);

    @Query("SELECT DISTINCT s FROM Seance s " +
           "JOIN s.surveillants sur " +
           "WHERE sur.id = :surveillantId " +
           "AND s.date BETWEEN :dateDebut AND :dateFin " +
           "ORDER BY s.date, s.heureDebut")
    List<Seance> findBySurveillantIdAndDateBetween(@Param("surveillantId") Long surveillantId,
                                                    @Param("dateDebut") LocalDate dateDebut,
                                                    @Param("dateFin") LocalDate dateFin);

    @Query("SELECT DISTINCT s FROM Seance s " +
           "JOIN s.emploiDuTemps edt " +
           "JOIN edt.classe c " +
           "JOIN c.specialite sp " +
           "JOIN sp.niveau n " +
           "JOIN n.filiere f " +
           "WHERE f.id = :filiereId " +
           "AND s.date BETWEEN :dateDebut AND :dateFin " +
           "ORDER BY s.date, s.heureDebut")
    List<Seance> findByFiliereIdAndDateBetween(@Param("filiereId") Long filiereId,
                                               @Param("dateDebut") LocalDate dateDebut,
                                               @Param("dateFin") LocalDate dateFin);
}
