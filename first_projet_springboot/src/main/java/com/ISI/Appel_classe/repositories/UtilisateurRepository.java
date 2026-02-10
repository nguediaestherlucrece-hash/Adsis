package com.ISI.Appel_classe.repositories;

import com.ISI.Appel_classe.models.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    // Tu peux ajouter des méthodes personnalisées ici plus tard
}

