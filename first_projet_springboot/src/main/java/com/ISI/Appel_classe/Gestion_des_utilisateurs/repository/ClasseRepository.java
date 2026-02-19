package com.ISI.Appel_classe.Gestion_des_utilisateurs.repository;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Classe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClasseRepository extends JpaRepository<Classe, Long> {
}
