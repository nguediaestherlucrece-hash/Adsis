package com.ISI.Appel_classe.Gestion_des_utilisateurs.repository;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByName(String name);

    Set<Permission> findByNameIn(Set<String> names);
}
