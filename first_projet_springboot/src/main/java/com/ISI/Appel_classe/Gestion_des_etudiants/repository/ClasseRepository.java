package com.ISI.Appel_classe.Gestion_des_etudiants.repository;

import com.ISI.Appel_classe.Gestion_des_etudiants.model.Classe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClasseRepository extends JpaRepository<Classe, Long> {
}
