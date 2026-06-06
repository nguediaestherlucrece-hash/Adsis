package com.ISI.Appel_classe.Gestion_des_utilisateurs.service.impl;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.NiveauCreateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.NiveauUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response.NiveauDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.exception.ResourceNotFoundException;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.mappers.NiveauMapper;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Filiere;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Niveau;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.FiliereRepository;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.NiveauRepository;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.service.serviceInterface.NiveauService;
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
public class NiveauServiceImpl implements NiveauService {

    private final NiveauRepository niveauRepository;
    private final NiveauMapper mapper;
    private final FiliereRepository filiereRepository;

    @Override
    public List<NiveauDTO> getAllNiveaux() {
        return niveauRepository.findAllByOrderByOrdreAsc().stream().map(mapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public NiveauDTO getNiveauById(Long id) {
        Niveau n = niveauRepository.findById(Objects.requireNonNull(id, "ID cannot be null"))
                .orElseThrow(() -> new ResourceNotFoundException("Niveau non trouvé: " + id));
        return mapper.toDTO(n);
    }

    @Override
    @Transactional
    public NiveauDTO createNiveau(NiveauCreateDTO dto) {
        Niveau n = mapper.fromCreateDTO(dto);

        if (dto.getFiliereId() != null) {
            Filiere filiere = filiereRepository.findById(dto.getFiliereId())
                    .orElseThrow(() -> new ResourceNotFoundException("Filière non trouvée: " + dto.getFiliereId()));
            n.setFiliere(filiere);
        }

        Niveau saved = niveauRepository.save(Objects.requireNonNull(n, "Niveau cannot be null"));
        return mapper.toDTO(saved);
    }

    @Override
    @Transactional
    public NiveauDTO updateNiveau(Long id, NiveauUpdateDTO dto) {
        Niveau n = niveauRepository.findById(Objects.requireNonNull(id, "ID cannot be null"))
                .orElseThrow(() -> new ResourceNotFoundException("Niveau non trouvé: " + id));

        if (dto.getIdNiveau() != null) {
            n.setIdNiveau(dto.getIdNiveau());
        }
        if (dto.getNom() != null) {
            n.setNom(dto.getNom());
        }
        if (dto.getOrdre() != null) {
            n.setOrdre(dto.getOrdre());
        }
        if (dto.getFiliereId() != null) {
            Filiere filiere = filiereRepository.findById(dto.getFiliereId())
                    .orElseThrow(() -> new ResourceNotFoundException("Filière non trouvée: " + dto.getFiliereId()));
            n.setFiliere(filiere);
        }

        Niveau updated = niveauRepository.save(Objects.requireNonNull(n, "Niveau cannot be null"));
        return mapper.toDTO(updated);
    }

    @Override
    @Transactional
    public void deleteNiveau(Long id) {
        if (!niveauRepository.existsById(Objects.requireNonNull(id, "ID cannot be null"))) {
            throw new ResourceNotFoundException("Niveau non trouvé: " + id);
        }
        niveauRepository.deleteById(Objects.requireNonNull(id, "ID cannot be null"));
    }
}