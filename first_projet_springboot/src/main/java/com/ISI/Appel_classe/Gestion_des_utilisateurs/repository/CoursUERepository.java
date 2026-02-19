package com.Gestion_des_utilisateurs.repository;

import com.Gestion_des_utilisateurs.entity.CoursUE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CoursUERepository extends JpaRepository<CoursUE, Long> {

    Optional<CoursUE> findByCodeUE(String codeUE);

    boolean existsByCodeUE(String codeUE);
}
