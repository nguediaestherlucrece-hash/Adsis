package com.ISI.Appel_classe.repositories;

import com.ISI.Appel_classe.models.Etudiant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EtudiantRepository extends JpaRepository<Etudiant, Long> {
    // Tu peux ajouter des méthodes personnalisées ici plus tard
}
