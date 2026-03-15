package com.ISI.Appel_classe.Gestion_des_utilisateurs.repository;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Utilisateur;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    @Query("SELECT DISTINCT u FROM Utilisateur u " +
            "LEFT JOIN AssistantPedagogique ap ON u.id = ap.id " +
            "LEFT JOIN Enseignant e ON u.id = e.id " +
            "LEFT JOIN Surveillant s ON u.id = s.id " +
            "WHERE (:type IS NULL OR (:type = 'ASSISTANT' AND ap.id IS NOT NULL) " +
            "OR (:type = 'ENSEIGNANT' AND e.id IS NOT NULL) " +
            "OR (:type = 'SURVEILLANT' AND s.id IS NOT NULL)) " +
            "AND (:recherche IS NULL OR LOWER(u.nom) LIKE LOWER(CONCAT('%', :recherche, '%')) " +
            "     OR LOWER(u.prenom) LIKE LOWER(CONCAT('%', :recherche, '%')) " +
            "     OR LOWER(u.email) LIKE LOWER(CONCAT('%', :recherche, '%'))) " +
            "AND (:filiere IS NULL OR ap.filiere.id = :filiere OR s.filiere.id = :filiere)")
    Page<Utilisateur> searchWithFilters(@Param("recherche") String recherche,
                                        @Param("type") String type,
                                        @Param("filiere") Long filiere,
                                        Pageable pageable);

    boolean existsByEmail(String email);
    Utilisateur findByEmail(String email);
}
