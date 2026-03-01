package com.ISI.Appel_classe.Gestion_des_utilisateurs.service.impl;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.FiliereCreateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.FiliereUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response.FiliereDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.exception.ResourceNotFoundException;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.mappers.FiliereMapper;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Filiere;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.CycleRepository;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.FiliereRepository;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.service.serviceInterface.FiliereService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FiliereServiceImpl implements FiliereService {

    private final FiliereRepository filiereRepository;
    private final CycleRepository cycleRepository;
    private final FiliereMapper mapper;

    @Override
    public List<FiliereDTO> getAllFilieres() {
        return filiereRepository.findAll().stream().map(mapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public FiliereDTO getFiliereById(Long id) {
        Filiere f = filiereRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Filière non trouvée: " + id));
        return mapper.toDTO(f);
    }

    @Override
    @Transactional
    public FiliereDTO createFiliere(FiliereCreateDTO dto) {
        Filiere f = mapper.fromCreateDTO(dto);
        if (dto.getCycleId() != null) {
            f.setCycle(cycleRepository.findById(dto.getCycleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cycle non trouvé: " + dto.getCycleId())));
        }
        Filiere saved = filiereRepository.save(f);
        return mapper.toDTO(saved);
    }

    @Override
    @Transactional
    public FiliereDTO updateFiliere(Long id, FiliereUpdateDTO dto) {
        Filiere f = filiereRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Filière non trouvée: " + id));
        mapper.updateFromDTO(f, dto);
        if (dto.getCycleId() != null) {
            f.setCycle(cycleRepository.findById(dto.getCycleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cycle non trouvé: " + dto.getCycleId())));
        } else {
            f.setCycle(null); // Retrait explicite du cycle
        }
        Filiere updated = filiereRepository.save(f);
        return mapper.toDTO(updated);
    }

    @Override
    @Transactional
    public void deleteFiliere(Long id) {
        if (!filiereRepository.existsById(id)) {
            throw new ResourceNotFoundException("Filière non trouvée: " + id);
        }
        filiereRepository.deleteById(id);
    }
}
