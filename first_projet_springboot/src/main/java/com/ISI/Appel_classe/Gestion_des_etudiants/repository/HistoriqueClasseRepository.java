package com.ISI.Appel_classe.Gestion_des_etudiants.repository;

import com.ISI.Appel_classe.Gestion_des_etudiants.model.HistoriqueClasse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoriqueClasseRepository extends JpaRepository<HistoriqueClasse, Long> {

    List<HistoriqueClasse> findByEtudiantIdOrderByDateSortieDesc(Long etudiantId);

    List<HistoriqueClasse> findByEtudiantIdAndAnneeAcademiqueId(Long etudiantId, Long anneeId);

    List<HistoriqueClasse> findByClasseId(Long classeId);
}
