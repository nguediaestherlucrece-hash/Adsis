package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.repository;

import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.EmploiDuTemps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmploiDuTempsRepository extends JpaRepository<EmploiDuTemps, Long> {

    Optional<EmploiDuTemps> findByIdEmploi(String idEmploi);
}
