package com.ISI.Appel_classe.repository;

import com.ISI.Appel_classe.entity.AssistantPedagogique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssistantPedagogiqueRepository extends JpaRepository<AssistantPedagogique, Long> {

    Optional<AssistantPedagogique> findByEmail(String email);

    boolean existsByEmail(String email);

    List<AssistantPedagogique> findByDepartement(String departement);
}
