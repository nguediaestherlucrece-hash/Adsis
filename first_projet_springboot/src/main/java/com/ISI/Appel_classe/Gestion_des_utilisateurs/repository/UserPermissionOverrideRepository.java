package com.ISI.Appel_classe.Gestion_des_utilisateurs.repository;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.UserPermissionOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPermissionOverrideRepository extends JpaRepository<UserPermissionOverride, Long> {
    List<UserPermissionOverride> findByUtilisateurId(Long utilisateurId);

    void deleteAllByUtilisateurId(Long utilisateurId);
}
