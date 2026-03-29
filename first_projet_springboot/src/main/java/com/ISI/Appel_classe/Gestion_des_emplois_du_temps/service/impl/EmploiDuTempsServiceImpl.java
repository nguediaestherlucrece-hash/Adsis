package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.service.impl;

import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.request.EmploiDuTempsCreateDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.request.EmploiDuTempsUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.response.EmploiDuTempsDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.mappers.EmploiDuTempsMapper;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.EmploiDuTemps;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.Seance;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.Semestre;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.repository.EmploiDuTempsRepository;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.repository.SemestreRepository;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.service.serviceInterface.EmploiDuTempsService;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.AssistantPedagogique;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Classe;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.AssistantPedagogiqueRepository;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.ClasseRepository;
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
public class EmploiDuTempsServiceImpl implements EmploiDuTempsService {

    private final EmploiDuTempsRepository emploiDuTempsRepository;
    private final SemestreRepository semestreRepository;
    private final ClasseRepository classeRepository;
    private final AssistantPedagogiqueRepository assistantPedagogiqueRepository;
    private final EmploiDuTempsMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<EmploiDuTempsDTO> getAllEmplois() {
        return emploiDuTempsRepository.findAll()
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EmploiDuTempsDTO getEmploiById(Long id) {
        EmploiDuTemps emploi = emploiDuTempsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Emploi du temps non trouvé avec l'id: " + id));
        return mapper.toDTO(emploi);
    }

    @Override
    @Transactional(readOnly = true)
    public EmploiDuTempsDTO getEmploiByCode(String idEmploi) {
        EmploiDuTemps emploi = emploiDuTempsRepository.findByIdEmploi(idEmploi)
                .orElseThrow(() -> new RuntimeException("Emploi du temps non trouvé avec le code: " + idEmploi));
        return mapper.toDTO(emploi);
    }

    @Override
    public EmploiDuTempsDTO createEmploi(EmploiDuTempsCreateDTO dto) {
        EmploiDuTemps emploi = new EmploiDuTemps();
        emploi.setIdEmploi(dto.getIdEmploi());
        emploi.setDateDebut(dto.getDateDebut());
        emploi.setDateFin(dto.getDateFin());
        emploi.setNombreJours(dto.getNombreJours() != null ? dto.getNombreJours() : 6);

        if (dto.getSemestreId() != null) {
            Semestre semestre = semestreRepository.findById(dto.getSemestreId())
                    .orElseThrow(() -> new RuntimeException("Semestre non trouvé avec l'id: " + dto.getSemestreId()));
            emploi.setSemestre(semestre);
        }

        if (dto.getClasseId() != null) {
            Classe classe = classeRepository.findById(dto.getClasseId())
                    .orElseThrow(() -> new RuntimeException("Classe non trouvée avec l'id: " + dto.getClasseId()));
            emploi.setClasse(classe);
        }

        if (dto.getGestionnaireId() != null) {
            AssistantPedagogique gestionnaire = assistantPedagogiqueRepository.findById(dto.getGestionnaireId())
                    .orElseThrow(() -> new RuntimeException("Assistant pédagogique non trouvé avec l'id: " + dto.getGestionnaireId()));
            emploi.setGestionnaire(gestionnaire);
        }

        EmploiDuTemps saved = emploiDuTempsRepository.save(emploi);
        log.info("Emploi du temps créé avec l'id {}", saved.getId());
        return mapper.toDTO(saved);
    }

    @Override
    public EmploiDuTempsDTO updateEmploi(Long id, EmploiDuTempsUpdateDTO dto) {
        EmploiDuTemps emploi = emploiDuTempsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Emploi du temps non trouvé avec l'id: " + id));

        emploi.setIdEmploi(dto.getIdEmploi());
        emploi.setDateDebut(dto.getDateDebut());
        emploi.setDateFin(dto.getDateFin());
        if (dto.getNombreJours() != null) {
            emploi.setNombreJours(dto.getNombreJours());
        }

        if (dto.getSemestreId() != null) {
            Semestre semestre = semestreRepository.findById(dto.getSemestreId())
                    .orElseThrow(() -> new RuntimeException("Semestre non trouvé avec l'id: " + dto.getSemestreId()));
            emploi.setSemestre(semestre);
        } else {
            emploi.setSemestre(null);
        }

        if (dto.getClasseId() != null) {
            Classe classe = classeRepository.findById(dto.getClasseId())
                    .orElseThrow(() -> new RuntimeException("Classe non trouvée avec l'id: " + dto.getClasseId()));
            emploi.setClasse(classe);
        } else {
            emploi.setClasse(null);
        }

        if (dto.getGestionnaireId() != null) {
            AssistantPedagogique gestionnaire = assistantPedagogiqueRepository.findById(dto.getGestionnaireId())
                    .orElseThrow(() -> new RuntimeException("Assistant pédagogique non trouvé avec l'id: " + dto.getGestionnaireId()));
            emploi.setGestionnaire(gestionnaire);
        } else {
            emploi.setGestionnaire(null);
        }

        EmploiDuTemps updated = emploiDuTempsRepository.save(emploi);
        log.info("Emploi du temps mis à jour avec l'id {}", updated.getId());
        return mapper.toDTO(updated);
    }

    @Override
    public void deleteEmploi(Long id) {
        EmploiDuTemps emploi = emploiDuTempsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Emploi du temps non trouvé avec l'id: " + id));

        // Nettoyer les FK des séances avant suppression cascade
        for (Seance seance : emploi.getSeances()) {
            seance.setEnseignant(null);
            seance.getSurveillants().clear();
        }

        emploiDuTempsRepository.delete(emploi);
        log.info("Emploi du temps supprimé avec l'id {}", id);
    }
}
