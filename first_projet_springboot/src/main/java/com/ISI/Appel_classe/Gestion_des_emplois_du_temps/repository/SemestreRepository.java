package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.repository;

import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.Semestre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SemestreRepository extends JpaRepository<Semestre, Long> {
    Optional<Semestre> findByIdSemestre(String idSemestre);
}
