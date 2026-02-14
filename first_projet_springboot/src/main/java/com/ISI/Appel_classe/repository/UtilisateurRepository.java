package com.ISI.Appel_classe.repository;

import com.ISI.Appel_classe.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    // Tu peux ajouter des méthodes personnalisées ici plus tard
    Utilisateur findByEmail(String email);
}

