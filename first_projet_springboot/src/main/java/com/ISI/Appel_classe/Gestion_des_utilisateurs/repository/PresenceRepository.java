package com.ISI.Appel_classe.Gestion_des_utilisateurs.repository;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Presence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PresenceRepository extends JpaRepository<Presence, Long> {

    List<Presence> findByEtudiantId(Long etudiantId);

    long countByEtudiantIdAndSeance_EmploiDuTemps_ClasseId(Long etudiantId, Long classeId);

    long countByEtudiantIdAndSeance_EmploiDuTemps_ClasseIdAndStatut(Long etudiantId, Long classeId, String statut);

    long countByEtudiantIdAndSeance_EmploiDuTemps_SemestreId(Long etudiantId, Long semestreId);

    long countByEtudiantIdAndSeance_EmploiDuTemps_SemestreIdAndStatut(Long etudiantId, Long semestreId, String statut);
    
    // Nouvelles méthodes pour la gestion des appels
    List<Presence> findByAppelId(Long appelId);
    
    Optional<Presence> findByAppelIdAndEtudiantId(Long appelId, Long etudiantId);
    
    long countByAppelIdAndStatut(Long appelId, String statut);
}
