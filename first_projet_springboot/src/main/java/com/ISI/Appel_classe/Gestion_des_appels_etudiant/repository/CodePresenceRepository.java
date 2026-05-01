package com.ISI.Appel_classe.Gestion_des_appels_etudiant.repository;

import com.ISI.Appel_classe.Gestion_des_appels_etudiant.model.CodePresence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CodePresenceRepository extends JpaRepository<CodePresence, Long> {

    Optional<CodePresence> findByCodePIN(String codePIN);

    Optional<CodePresence> findByQrToken(String qrToken);

    @Query("SELECT c FROM CodePresence c WHERE c.etudiant.id = :etudiantId AND c.seance.id = :seanceId AND c.statut = 'actif' AND c.dateExpiration > CURRENT_TIMESTAMP")
    List<CodePresence> findCodesActifsByEtudiantAndSeance(@Param("etudiantId") Long etudiantId, @Param("seanceId") Long seanceId);

    @Query("SELECT c FROM CodePresence c WHERE c.dateExpiration < :dateExpiration AND c.statut = 'actif'")
    List<CodePresence> findCodesExpires(@Param("dateExpiration") LocalDateTime dateExpiration);

    @Query("SELECT c FROM CodePresence c WHERE c.etudiant.id = :etudiantId ORDER BY c.dateCreation DESC LIMIT 1")
    Optional<CodePresence> findLatestCodeByEtudiant(@Param("etudiantId") Long etudiantId);
}
