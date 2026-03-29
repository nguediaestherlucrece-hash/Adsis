package com.ISI.Appel_classe.Gestion_des_utilisateurs.service.impl;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.FiliereCreateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.FiliereUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response.FiliereDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.exception.ResourceNotFoundException;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.mappers.FiliereMapper;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Cycle;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Filiere;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Niveau;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.CycleRepository;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.FiliereRepository;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.NiveauRepository;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.service.serviceInterface.FiliereService;
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
public class FiliereServiceImpl implements FiliereService {

    private final FiliereRepository filiereRepository;
    private final FiliereMapper mapper;
    private final CycleRepository cycleRepository;

    @Override
    public List<FiliereDTO> getAllFilieres() {
        return filiereRepository.findAll().stream().map(mapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public FiliereDTO getFiliereById(Long id) {
        Filiere f = filiereRepository.findById(Objects.requireNonNull(id, "ID cannot be null"))
                .orElseThrow(() -> new ResourceNotFoundException("Filière non trouvée: " + id));
        return mapper.toDTO(f);
    }

    @Override
    @Transactional
    public FiliereDTO createFiliere(FiliereCreateDTO dto) {
        Filiere f = mapper.fromCreateDTO(dto);
        // Attacher les Niveaux si niveauIds fournis
        if (dto.getCycleId() != null) {
            Cycle cycle = cycleRepository.findById(dto.getCycleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Filière non trouvée: " + dto.getCycleId()));
            f.setCycle(cycle);
        }
        Filiere saved = filiereRepository.save(Objects.requireNonNull(f, "Filiere cannot be null"));
        return mapper.toDTO(saved);
    }

    @Override
    @Transactional
    public FiliereDTO updateFiliere(Long id, FiliereUpdateDTO dto) {
        Filiere f = filiereRepository.findById(Objects.requireNonNull(id, "ID cannot be null"))
                .orElseThrow(() -> new ResourceNotFoundException("Filière non trouvée: " + id));

        if (dto.getIdFiliere() != null)
            f.setIdFiliere(dto.getIdFiliere());
        if (dto.getNomFiliere() != null)
            f.setNomFiliere(dto.getNomFiliere());
        if (dto.getDescription() != null)
            f.setDescription(dto.getDescription());

        // Mettre à jour les niveaux parents
        if (dto.getCycleId() != null) {
            Cycle cycle = cycleRepository.findById(dto.getCycleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Filière non trouvée: " + dto.getCycleId()));
            f.setCycle(cycle);
        }

        Filiere updated = filiereRepository.save(Objects.requireNonNull(f, "Filiere cannot be null"));
        return mapper.toDTO(updated);
    }

    @Override
    @Transactional
    public void deleteFiliere(Long id) {
        if (!filiereRepository.existsById(Objects.requireNonNull(id, "ID cannot be null"))) {
            throw new ResourceNotFoundException("Filière non trouvée: " + id);
        }
        filiereRepository.deleteById(Objects.requireNonNull(id, "ID cannot be null"));
    }
}
