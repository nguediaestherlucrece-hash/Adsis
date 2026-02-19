package com.Gestion_des_utilisateurs.repository;

import com.Gestion_des_utilisateurs.entity.Etudiant;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
