package com.ISI.Appel_classe.repositories;

import com.ISI.Appel_classe.models.AssistantPedagogique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssistantPedagogiqueRepository extends JpaRepository<AssistantPedagogique, Long> {
    // Tu peux ajouter des méthodes personnalisées ici plus tard
}
