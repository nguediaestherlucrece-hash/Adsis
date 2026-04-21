package com.ISI.Appel_classe.Gestion_des_etudiants.repository;

import com.ISI.Appel_classe.Gestion_des_etudiants.model.Etudiant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EtudiantRepository extends JpaRepository<Etudiant, Long> {

    /**
     * Recherche un étudiant par son matricule
     */
    Optional<Etudiant> findByMatricule(String matricule);

    /**
     * Recherche un étudiant par son email (hérité de Utilisateur)
     */
    Optional<Etudiant> findByEmail(String email);

    /**
     * Vérifie si un email existe déjà
     */
    boolean existsByEmail(String email);

    /**
     * Vérifie si un matricule existe déjà
     */
    boolean existsByMatricule(String matricule);

    /**
     * Récupère tous les étudiants d'une classe
     */
    List<Etudiant> findByClasseId(Long classeId);
    
    /**
     * Récupère tous les étudiants de la classe actuelle
     */
    @Query("SELECT e FROM Etudiant e WHERE e.classe.id = :classeId")
    List<Etudiant> findByClasseActuelleId(@Param("classeId") Long classeId);
}
