package com.Gestion_des_utilisateurs.repository;

import com.Gestion_des_utilisateurs.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    /**
     * Vérifie si un email existe déjà dans la base,
     * indépendamment du type d'utilisateur (Etudiant, Assistant, etc.)
     */
    boolean existsByEmail(String email);

    /**
     * Recherche un utilisateur par email
     */
    Utilisateur findByEmail(String email);
}
