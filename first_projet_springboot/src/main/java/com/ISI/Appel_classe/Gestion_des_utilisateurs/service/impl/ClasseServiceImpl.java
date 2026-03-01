package com.ISI.Appel_classe.Gestion_des_utilisateurs.service.impl;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.ClasseCreateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.ClasseUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response.ClasseDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.exception.ResourceNotFoundException;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.mappers.ClasseMapper;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Classe;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.ClasseRepository;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.FiliereRepository;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.service.serviceInterface.ClasseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClasseServiceImpl implements ClasseService {

    private final ClasseRepository repository;
    private final FiliereRepository filiereRepository;
    private final ClasseMapper mapper;

    @Override
    public List<ClasseDTO> getAllClasses() {
        return repository.findAll().stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ClasseDTO getClasseById(Long id) {
        Classe c = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classe non trouvée: " + id));
        return mapper.toDTO(c);
    }

    @Override
    @Transactional
    public ClasseDTO createClasse(ClasseCreateDTO dto) {
        Classe c = mapper.fromCreateDTO(dto);
        if (dto.getFiliereId() != null) {
            c.setFiliere(filiereRepository.findById(dto.getFiliereId())
                    .orElseThrow(() -> new ResourceNotFoundException("Filière non trouvée: " + dto.getFiliereId())));
        }
        Classe saved = repository.save(c);
        return mapper.toDTO(saved);
    }

    @Override
    @Transactional
    public ClasseDTO updateClasse(Long id, ClasseUpdateDTO dto) {
        Classe c = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classe non trouvée: " + id));
        mapper.updateFromDTO(c, dto);
        if (dto.getFiliereId() != null) {
            c.setFiliere(filiereRepository.findById(dto.getFiliereId())
                    .orElseThrow(() -> new ResourceNotFoundException("Filière non trouvée: " + dto.getFiliereId())));
        } else {
            c.setFiliere(null); // Retrait explicite de la filière
        }
        Classe updated = repository.save(c);
        return mapper.toDTO(updated);
    }

    @Override
    @Transactional
    public void deleteClasse(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Classe non trouvée: " + id);
        }
        repository.deleteById(id);
    }
}
