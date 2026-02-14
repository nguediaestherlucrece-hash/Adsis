package com.ISI.Appel_classe.repository;

import com.ISI.Appel_classe.entity.Enseignant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnseignantRepository extends JpaRepository<Enseignant, Long> {

    Optional<Enseignant> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Enseignant> findBySpecialite(String specialite);

    List<Enseignant> findByGrade(String grade);
}
