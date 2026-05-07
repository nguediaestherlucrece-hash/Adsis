package com.ISI.Appel_classe.Gestion_des_utilisateurs.repository;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    @Query("SELECT u FROM Utilisateur u " +
            "WHERE TYPE(u) IN (Enseignant, Surveillant, AssistantPedagogique, Administrateur) " +
            "AND (:type IS NULL OR :type = '' " +
            "    OR (:type = 'ENSEIGNANT'            AND TYPE(u) = Enseignant) " +
            "    OR (:type = 'SURVEILLANT'            AND TYPE(u) = Surveillant) " +
            "    OR (:type = 'ASSISTANT_PEDAGOGIQUE'  AND TYPE(u) = AssistantPedagogique) " +
            "    OR (:type = 'ADMIN'                  AND TYPE(u) = Administrateur)) " +
            "AND (:recherche IS NULL OR :recherche = '' " +
            "    OR LOWER(u.nom)    LIKE LOWER(CONCAT('%', :recherche, '%')) " +
            "    OR LOWER(u.prenom) LIKE LOWER(CONCAT('%', :recherche, '%')) " +
            "    OR LOWER(u.email)  LIKE LOWER(CONCAT('%', :recherche, '%')))")
    Page<Utilisateur> searchWithFilters(@Param("recherche") String recherche,
                                        @Param("type") String type,
                                        Pageable pageable);

    boolean existsByEmail(String email);
    Utilisateur findByEmail(String email);
}
