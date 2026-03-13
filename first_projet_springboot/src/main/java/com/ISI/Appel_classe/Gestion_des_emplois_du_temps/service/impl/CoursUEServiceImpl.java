package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.service.impl;

import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.request.coursUECreateDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.request.coursUEUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.response.CoursUEDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.mappers.CoursUEMapper;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.CoursUE;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.repository.CoursUERepository;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.service.serviceInterface.CourUEService;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Specialite;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.SpecialiteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CoursUEServiceImpl implements CourUEService {

    private final CoursUERepository coursUERepository;
    private final SpecialiteRepository specialiteRepository;
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
        // Si besoin, ajouter une méthode dans le repository: findByIdCours
        CoursUE cours = coursUERepository.findAll().stream()
                .filter(c -> idCours.equals(c.getIdCours()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cours UE non trouvé avec le code: " + idCours));
        return mapper.toDTO(cours);
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

        CoursUE updated = coursUERepository.save(cours);
        log.info("Cours UE mis à jour avec l'id {}", updated.getId());
        return mapper.toDTO(updated);
    }

    @Override
    public void deleteCours(Long id) {
        if (!coursUERepository.existsById(id)) {
            throw new RuntimeException("Cours UE non trouvé avec l'id: " + id);
        }
        coursUERepository.deleteById(id);
        log.info("Cours UE supprimé avec l'id {}", id);
    }
}
