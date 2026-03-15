package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.service.impl;

import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.request.SeanceCreateDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.request.SeanceUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.response.SeanceDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.mappers.SeanceMapper;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.CoursUE;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.EmploiDuTemps;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.Seance;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.repository.CoursUERepository;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.repository.EmploiDuTempsRepository;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.repository.SeanceRepository;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.service.serviceInterface.SeanceService;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Enseignant;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Surveillant;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.EnseignantRepository;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.SurveillantRepository;
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
public class SeanceServiceImpl implements SeanceService {

    private final SeanceRepository seanceRepository;
    private final CoursUERepository coursUERepository;
    private final EnseignantRepository enseignantRepository;
    private final SurveillantRepository surveillantRepository;
    private final EmploiDuTempsRepository emploiDuTempsRepository;
    private final SeanceMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<SeanceDTO> getAllSeances() {
        return seanceRepository.findAll()
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SeanceDTO getSeanceById(Long id) {
        Seance seance = seanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Séance non trouvée avec l'id: " + id));
        return mapper.toDTO(seance);
    }

    @Override
    @Transactional(readOnly = true)
    public SeanceDTO getSeanceByCode(String idSeance) {
        Seance seance = seanceRepository.findByIdSeance(idSeance)
                .orElseThrow(() -> new RuntimeException("Séance non trouvée avec le code: " + idSeance));
        return mapper.toDTO(seance);
    }

    @Override
    public SeanceDTO createSeance(SeanceCreateDTO dto) {
        Seance seance = new Seance();
        seance.setIdSeance(dto.getIdSeance());
        seance.setDate(dto.getDate());
        seance.setHeureDebut(dto.getHeureDebut());
        seance.setHeureFin(dto.getHeureFin());
        seance.setStatut(dto.isStatut());

        if (dto.getCoursId() != null) {
            CoursUE cours = coursUERepository.findById(dto.getCoursId())
                    .orElseThrow(() -> new RuntimeException("Cours non trouvé avec l'id: " + dto.getCoursId()));
            seance.setCours(cours);
        }

        if (dto.getEnseignantId() != null) {
            Enseignant enseignant = enseignantRepository.findById(dto.getEnseignantId())
                    .orElseThrow(() -> new RuntimeException("Enseignant non trouvé avec l'id: " + dto.getEnseignantId()));
            seance.setEnseignant(enseignant);
        }

        if (dto.getEmploiDuTempsId() != null) {
            EmploiDuTemps emploi = emploiDuTempsRepository.findById(dto.getEmploiDuTempsId())
                    .orElseThrow(() -> new RuntimeException("Emploi du temps non trouvé avec l'id: " + dto.getEmploiDuTempsId()));
            seance.setEmploiDuTemps(emploi);
        }

        Seance saved = seanceRepository.save(seance);
        log.info("Séance créée avec l'id {}", saved.getId());
        return mapper.toDTO(saved);
    }

    @Override
    public SeanceDTO updateSeance(Long id, SeanceUpdateDTO dto) {
        Seance seance = seanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Séance non trouvée avec l'id: " + id));

        seance.setIdSeance(dto.getIdSeance());
        seance.setDate(dto.getDate());
        seance.setHeureDebut(dto.getHeureDebut());
        seance.setHeureFin(dto.getHeureFin());
        seance.setStatut(dto.isStatut());

        if (dto.getCoursId() != null) {
            CoursUE cours = coursUERepository.findById(dto.getCoursId())
                    .orElseThrow(() -> new RuntimeException("Cours non trouvé avec l'id: " + dto.getCoursId()));
            seance.setCours(cours);
        } else {
            seance.setCours(null);
        }

        if (dto.getEnseignantId() != null) {
            Enseignant enseignant = enseignantRepository.findById(dto.getEnseignantId())
                    .orElseThrow(() -> new RuntimeException("Enseignant non trouvé avec l'id: " + dto.getEnseignantId()));
            seance.setEnseignant(enseignant);
        } else {
            seance.setEnseignant(null);
        }

        if (dto.getEmploiDuTempsId() != null) {
            EmploiDuTemps emploi = emploiDuTempsRepository.findById(dto.getEmploiDuTempsId())
                    .orElseThrow(() -> new RuntimeException("Emploi du temps non trouvé avec l'id: " + dto.getEmploiDuTempsId()));
            seance.setEmploiDuTemps(emploi);
        } else {
            seance.setEmploiDuTemps(null);
        }

        Seance updated = seanceRepository.save(seance);
        log.info("Séance mise à jour avec l'id {}", updated.getId());
        return mapper.toDTO(updated);
    }

    @Override
    public void deleteSeance(Long id) {
        if (!seanceRepository.existsById(id)) {
            throw new RuntimeException("Séance non trouvée avec l'id: " + id);
        }
        seanceRepository.deleteById(id);
        log.info("Séance supprimée avec l'id {}", id);
    }
}
