package com.ISI.Appel_classe.Gestion_des_utilisateurs.repository;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Enseignant;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.enums.Statut;
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

    List<Enseignant> findByStatutAndAccountNonLocked(Statut statut, Boolean accountNonLocked);
}
