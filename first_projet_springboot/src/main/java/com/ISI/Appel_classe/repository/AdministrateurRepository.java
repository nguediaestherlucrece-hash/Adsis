package com.ISI.Appel_classe.repository;

import com.ISI.Appel_classe.entity.Administrateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdministrateurRepository extends JpaRepository<Administrateur, Long> {
    // Tu peux ajouter des méthodes personnalisées ici plus tard
}
