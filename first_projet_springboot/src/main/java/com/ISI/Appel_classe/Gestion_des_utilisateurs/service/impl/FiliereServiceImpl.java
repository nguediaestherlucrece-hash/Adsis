package com.ISI.Appel_classe.Gestion_des_utilisateurs.service.impl;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.FiliereCreateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.FiliereUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response.FiliereDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.exception.ResourceNotFoundException;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.mappers.FiliereMapper;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Filiere;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Niveau;
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
    private final NiveauRepository niveauRepository;

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
        Filiere saved = filiereRepository.save(Objects.requireNonNull(f, "Filiere cannot be null"));

        // Attacher les Niveaux si niveauIds fournis
        if (dto.getNiveauIds() != null && !dto.getNiveauIds().isEmpty()) {
            for (Long nid : dto.getNiveauIds()) {
                Niveau nv = niveauRepository.findById(nid).orElse(null);
                if (nv != null) {
                    nv.getFilieres().add(saved);
                    niveauRepository.save(nv);
                    saved.getNiveaux().add(nv);
                }
            }
        }

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
        List<Niveau> copyNiveaux = new java.util.ArrayList<>(f.getNiveaux());
        for (Niveau nv : copyNiveaux) {
            if (dto.getNiveauIds() == null || !dto.getNiveauIds().contains(nv.getId())) {
                nv.getFilieres().removeIf(fil -> fil.getId().equals(f.getId()));
                niveauRepository.save(nv);
                f.getNiveaux().removeIf(niv -> niv.getId().equals(nv.getId()));
            }
        }

        if (dto.getNiveauIds() != null) {
            for (Long nid : dto.getNiveauIds()) {
                boolean alreadyHas = f.getNiveaux().stream().anyMatch(n -> n.getId().equals(nid));
                if (!alreadyHas) {
                    Niveau n = niveauRepository.findById(nid)
                            .orElseThrow(() -> new ResourceNotFoundException("Niveau non trouvé: " + nid));

                    if (n.getFilieres().stream().noneMatch(fil -> fil.getId().equals(f.getId()))) {
                        n.getFilieres().add(f);
                    }
                    niveauRepository.save(n);

                    if (f.getNiveaux().stream().noneMatch(niv -> niv.getId().equals(n.getId()))) {
                        f.getNiveaux().add(n);
                    }
                }
            }
        }

        Filiere updated = filiereRepository.save(Objects.requireNonNull(f, "Filiere cannot be null"));
        return mapper.toDTO(updated);
    }

    @Override
    @Transactional
    public void deleteFiliere(Long id) {
        Filiere f = filiereRepository.findById(Objects.requireNonNull(id, "ID cannot be null"))
                .orElseThrow(() -> new ResourceNotFoundException("Filière non trouvée: " + id));

        if (f.getNiveaux() != null) {
            for (Niveau nv : f.getNiveaux()) {
                nv.getFilieres().removeIf(fil -> fil.getId().equals(f.getId()));
                niveauRepository.save(nv);
            }
        }

        filiereRepository.delete(f);
    }
}
