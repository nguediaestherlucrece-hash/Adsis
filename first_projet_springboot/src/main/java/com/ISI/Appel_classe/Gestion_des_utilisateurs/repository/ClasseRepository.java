package com.Gestion_des_utilisateurs.repository;

import com.Gestion_des_utilisateurs.entity.Classe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClasseRepository extends JpaRepository<Classe, Long> {
}
