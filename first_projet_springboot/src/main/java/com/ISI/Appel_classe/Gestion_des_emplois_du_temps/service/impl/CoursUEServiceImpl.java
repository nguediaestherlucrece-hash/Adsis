package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.service.impl;

import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.request.coursUECreateDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.request.coursUEUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.response.CoursUEDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.mappers.CoursUEMapper;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.CoursUE;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.repository.CoursUERepository;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.service.serviceInterface.CourUEService;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Enseignant;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Specialite;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.EnseignantRepository;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.SpecialiteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CoursUEServiceImpl implements CourUEService {

    private final CoursUERepository coursUERepository;
    private final SpecialiteRepository specialiteRepository;
    private final EnseignantRepository enseignantRepository;
    private final CoursUEMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<CoursUEDTO> getAllCours() {
        return coursUERepository.findAll()
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CoursUEDTO getCoursById(Long id) {
        CoursUE cours = coursUERepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cours UE non trouvé avec l'id: " + id));
        return mapper.toDTO(cours);
    }

    @Override
    @Transactional(readOnly = true)
    public CoursUEDTO getCoursByCode(String idCours) {
        CoursUE cours = coursUERepository.findAll().stream()
                .filter(c -> idCours.equals(c.getIdCours()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cours UE non trouvé avec le code: " + idCours));
        return mapper.toDTO(cours);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoursUEDTO> getCoursBySpecialite(Long specialiteId) {
        return coursUERepository.findBySpecialiteId(specialiteId)
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CoursUEDTO createCours(coursUECreateDTO dto) {
        CoursUE cours = new CoursUE();
        cours.setIdCours(dto.getIdCours());
        cours.setCodeUE(dto.getCodeUE());
        cours.setIntitule(dto.getIntitule());
        cours.setVolumeHoraire(dto.getVolumeHoraire());

        Specialite specialite = specialiteRepository.findById(dto.getSpecialiteId())
                .orElseThrow(() -> new RuntimeException("Spécialité non trouvée"));
        cours.setSpecialite(specialite);

        CoursUE saved = coursUERepository.save(cours);

        // Associer les enseignants (côté propriétaire de la relation ManyToMany)
        if (dto.getEnseignantIds() != null && !dto.getEnseignantIds().isEmpty()) {
            for (Long enseignantId : dto.getEnseignantIds()) {
                Enseignant enseignant = enseignantRepository.findById(enseignantId)
                        .orElseThrow(() -> new RuntimeException("Enseignant non trouvé avec l'id: " + enseignantId));
                enseignant.getCoursEnseignes().add(saved);
                enseignantRepository.save(enseignant);
            }
        }

        log.info("Cours UE créé avec l'id {}", saved.getId());
        return mapper.toDTO(saved);
    }

    @Override
    public CoursUEDTO updateCours(Long id, coursUEUpdateDTO dto) {
        CoursUE cours = coursUERepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cours UE non trouvé avec l'id: " + id));

        cours.setIdCours(dto.getIdCours());
        cours.setCodeUE(dto.getCodeUE());
        cours.setIntitule(dto.getIntitule());
        cours.setVolumeHoraire(dto.getVolumeHoraire());

        Specialite specialite = specialiteRepository.findById(dto.getSpecialiteId())
                .orElseThrow(() -> new RuntimeException("Spécialité non trouvée"));
        cours.setSpecialite(specialite);

        // Mettre à jour les enseignants
        if (dto.getEnseignantIds() != null) {
            // Retirer le cours des anciens enseignants
            List<Enseignant> anciensEnseignants = new ArrayList<>(cours.getEnseignants());
            for (Enseignant ancien : anciensEnseignants) {
                ancien.getCoursEnseignes().remove(cours);
                enseignantRepository.save(ancien);
            }

            // Ajouter les nouveaux enseignants
            for (Long enseignantId : dto.getEnseignantIds()) {
                Enseignant enseignant = enseignantRepository.findById(enseignantId)
                        .orElseThrow(() -> new RuntimeException("Enseignant non trouvé avec l'id: " + enseignantId));
                enseignant.getCoursEnseignes().add(cours);
                enseignantRepository.save(enseignant);
            }
        }

        CoursUE updated = coursUERepository.save(cours);
        log.info("Cours UE mis à jour avec l'id {}", updated.getId());
        return mapper.toDTO(updated);
    }

    @Override
    public void deleteCours(Long id) {
        CoursUE cours = coursUERepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cours UE non trouvé avec l'id: " + id));

        // Retirer le cours de tous les enseignants (nettoyage join table enseignant_cours)
        List<Enseignant> enseignantsAssocies = new ArrayList<>(cours.getEnseignants());
        for (Enseignant ens : enseignantsAssocies) {
            ens.getCoursEnseignes().remove(cours);
            enseignantRepository.save(ens);
        }

        // Nullifier la référence enseignant dans les séances avant suppression cascade
        for (var seance : cours.getSeances()) {
            seance.setEnseignant(null);
        }

        coursUERepository.delete(cours);
        log.info("Cours UE supprimé avec l'id {}", id);
    }
}
