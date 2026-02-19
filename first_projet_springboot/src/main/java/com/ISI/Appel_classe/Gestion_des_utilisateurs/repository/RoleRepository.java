package com.Gestion_des_utilisateurs.repository;

import com.Gestion_des_utilisateurs.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);

    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.id = :id")
    Optional<Role> findByIdWithPermissions(Long id);
}
