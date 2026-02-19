package com.Gestion_des_utilisateurs.repository;

import com.Gestion_des_utilisateurs.entity.Surveillant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SurveillantRepository extends JpaRepository<Surveillant, Long> {

    Optional<Surveillant> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Surveillant> findByAffectation(String affectation);
}
