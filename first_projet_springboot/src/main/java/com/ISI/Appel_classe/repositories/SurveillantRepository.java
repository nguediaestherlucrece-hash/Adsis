package com.ISI.Appel_classe.repositories;

import com.ISI.Appel_classe.models.Surveillant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveillantRepository extends JpaRepository<Surveillant, Long> {
    // Tu peux ajouter des méthodes personnalisées ici plus tard
}
