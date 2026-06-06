package com.ISI.Appel_classe.Gestion_des_utilisateurs.service.impl;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.CycleCreateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.CycleUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response.CycleDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.exception.ResourceNotFoundException;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.mappers.CycleMapper;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Cycle;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.CycleRepository;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.service.serviceInterface.CycleService;
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
public class CycleServiceImpl implements CycleService {

    private final CycleRepository cycleRepository;
    private final CycleMapper mapper;

    @Override
    public List<CycleDTO> getAllCycles() {
        return cycleRepository.findAll().stream().map(mapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public CycleDTO getCycleById(Long id) {
        Cycle c = cycleRepository.findById(Objects.requireNonNull(id, "ID cannot be null"))
                .orElseThrow(() -> new ResourceNotFoundException("Cycle non trouvé: " + id));
        return mapper.toDTO(c);
    }

    @Override
    @Transactional
    public CycleDTO createCycle(CycleCreateDTO dto) {
        Cycle c = mapper.fromCreateDTO(dto);
        Cycle saved = cycleRepository.save(Objects.requireNonNull(c, "Cycle cannot be null"));
        return mapper.toDTO(saved);
    }

    @Override
    @Transactional
    public CycleDTO updateCycle(Long id, CycleUpdateDTO dto) {
        Cycle c = cycleRepository.findById(Objects.requireNonNull(id, "ID cannot be null"))
                .orElseThrow(() -> new ResourceNotFoundException("Cycle non trouvé: " + id));
        mapper.updateFromDTO(c, dto);
        Cycle updated = cycleRepository.save(Objects.requireNonNull(c, "Cycle cannot be null"));
        return mapper.toDTO(updated);
    }

    @Override
    @Transactional
    public void deleteCycle(Long id) {
        if (!cycleRepository.existsById(Objects.requireNonNull(id, "ID cannot be null"))) {
            throw new ResourceNotFoundException("Cycle non trouvé: " + id);
        }
        cycleRepository.deleteById(Objects.requireNonNull(id, "ID cannot be null"));
    }
}

