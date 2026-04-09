package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.service.impl;

import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.request.SemestreCreateDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.request.SemestreUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.response.SemestreDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.mappers.SemestreMapper;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.AnneeAcademique;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.Semestre;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.repository.AnneeAcademiqueRepository;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.repository.SemestreRepository;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.service.serviceInterface.SemestreService;
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
public class SemestreServiceImpl implements SemestreService {

    private final SemestreRepository semestreRepository;
    private final AnneeAcademiqueRepository anneeAcademiqueRepository;
    private final SemestreMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<SemestreDTO> getAllSemestres() {
        return semestreRepository.findAll()
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SemestreDTO getSemestreById(Long id) {
        Semestre semestre = semestreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Semestre non trouvé avec l'id: " + id));
        return mapper.toDTO(semestre);
    }

    @Override
    @Transactional(readOnly = true)
    public SemestreDTO getSemestreByCode(String idSemestre) {
        Semestre semestre = semestreRepository.findByIdSemestre(idSemestre)
                .orElseThrow(() -> new RuntimeException("Semestre non trouvé avec le code: " + idSemestre));
        return mapper.toDTO(semestre);
    }

    @Override
    public SemestreDTO createSemestre(SemestreCreateDTO dto) {
        Semestre semestre = new Semestre();
        semestre.setIdSemestre(dto.getIdSemestre());
        semestre.setNomSemestre(dto.getNomSemestre());
        semestre.setStatut(dto.isStatut());

        if (dto.getAnneeAcademiqueId() != null) {
            AnneeAcademique annee = anneeAcademiqueRepository.findById(dto.getAnneeAcademiqueId())
                    .orElseThrow(() -> new RuntimeException("Année académique non trouvée avec l'id: " + dto.getAnneeAcademiqueId()));
            semestre.setAnnee(annee);
        }

        Semestre saved = semestreRepository.save(semestre);
        log.info("Semestre créé avec l'id {}", saved.getId());
        return mapper.toDTO(saved);
    }

    @Override
    public SemestreDTO updateSemestre(Long id, SemestreUpdateDTO dto) {
        Semestre semestre = semestreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Semestre non trouvé avec l'id: " + id));

        semestre.setIdSemestre(dto.getIdSemestre());
        semestre.setNomSemestre(dto.getNomSemestre());
        semestre.setStatut(dto.isStatut());

        if (dto.getAnneeAcademiqueId() != null) {
            AnneeAcademique annee = anneeAcademiqueRepository.findById(dto.getAnneeAcademiqueId())
                    .orElseThrow(() -> new RuntimeException("Année académique non trouvée avec l'id: " + dto.getAnneeAcademiqueId()));
            semestre.setAnnee(annee);
        } else {
            semestre.setAnnee(null);
        }

        Semestre updated = semestreRepository.save(semestre);
        log.info("Semestre mis à jour avec l'id {}", updated.getId());
        return mapper.toDTO(updated);
    }

    @Override
    public void deleteSemestre(Long id) {
        if (!semestreRepository.existsById(id)) {
            throw new RuntimeException("Semestre non trouvé avec l'id: " + id);
        }
        semestreRepository.deleteById(id);
        log.info("Semestre supprimé avec l'id {}", id);
    }

    @Override
    @Transactional
    public SemestreDTO activerSemestre(Long id) {
        Semestre semestre = semestreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Semestre non trouvé avec l'id: " + id));

        // Désactiver tous les semestres
        semestreRepository.deactivateAll();

        // Activer celui demandé
        semestre.setStatut(true);
        Semestre saved = semestreRepository.save(semestre);
        log.info("Semestre activé avec l'id {}", saved.getId());
        return mapper.toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SemestreDTO> getSemestresByAnnee(Long anneeId) {
        return semestreRepository.findByAnneeId(anneeId).stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }
}
