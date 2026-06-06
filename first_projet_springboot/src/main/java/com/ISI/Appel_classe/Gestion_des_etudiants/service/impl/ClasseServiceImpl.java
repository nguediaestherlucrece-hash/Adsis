package com.ISI.Appel_classe.Gestion_des_etudiants.service.impl;

import com.ISI.Appel_classe.Gestion_des_etudiants.dto.request.ClasseCreateDTO;
import com.ISI.Appel_classe.Gestion_des_etudiants.dto.request.ClasseUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_etudiants.dto.response.ClasseDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.exception.ResourceNotFoundException;
import com.ISI.Appel_classe.Gestion_des_etudiants.mappers.ClasseMapper;
import com.ISI.Appel_classe.Gestion_des_etudiants.model.Classe;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Specialite;
import com.ISI.Appel_classe.Gestion_des_etudiants.repository.ClasseRepository;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.SpecialiteRepository;
import com.ISI.Appel_classe.Gestion_des_etudiants.service.serviceInterface.ClasseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClasseServiceImpl implements ClasseService {

    private final ClasseRepository repository;
    private final ClasseMapper mapper;
    private final SpecialiteRepository specialiteRepository;

    @Override
    public List<ClasseDTO> getAllClasses() {
        return repository.findAll().stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClasseDTO> getClassesByFiliere(Long filiereId) {
        return repository.findBySpecialiteNiveauFiliereId(filiereId).stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ClasseDTO getClasseById(Long id) {
        Classe c = repository.findById(Objects.requireNonNull(id, "ID cannot be null"))
                .orElseThrow(() -> new ResourceNotFoundException("Classe non trouvée: " + id));
        return mapper.toDTO(c);
    }

    @Override
    @Transactional
    public ClasseDTO createClasse(ClasseCreateDTO dto) {
        Classe c = mapper.fromCreateDTO(dto);
        if (dto.getSpecialiteId() != null) {
            Specialite specialite = specialiteRepository.findById(dto.getSpecialiteId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Spécialité non trouvée: " + dto.getSpecialiteId()));
            c.setSpecialite(specialite);
        }
        Classe saved = repository.save(Objects.requireNonNull(c, "Classe cannot be null"));
        return mapper.toDTO(saved);
    }

    @Override
    @Transactional
    public ClasseDTO updateClasse(Long id, ClasseUpdateDTO dto) {
        Classe c = repository.findById(Objects.requireNonNull(id, "ID cannot be null"))
                .orElseThrow(() -> new ResourceNotFoundException("Classe non trouvée: " + id));

        if (dto.getIdClasse() != null)
            c.setIdClasse(dto.getIdClasse());
        if (dto.getNomClasse() != null)
            c.setNomClasse(dto.getNomClasse());
        if (dto.getAnneeAcademique() != null)
            c.setAnneeAcademique(dto.getAnneeAcademique());

        if (dto.getSpecialiteId() != null) {
            Specialite specialite = specialiteRepository.findById(dto.getSpecialiteId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Spécialité non trouvée: " + dto.getSpecialiteId()));
            c.setSpecialite(specialite);
        } else {
            c.setSpecialite(null);
        }

        Classe updated = repository.save(Objects.requireNonNull(c, "Classe cannot be null"));
        return mapper.toDTO(updated);
    }

    @Override
    @Transactional
    public void deleteClasse(Long id) {
        if (!repository.existsById(Objects.requireNonNull(id, "ID cannot be null"))) {
            throw new ResourceNotFoundException("Classe non trouvée: " + id);
        }
        repository.deleteById(Objects.requireNonNull(id, "ID cannot be null"));
    }
}
