package com.ISI.Appel_classe.Gestion_des_utilisateurs.service.impl;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.SpecialiteCreateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.SpecialiteUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response.SpecialiteDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.exception.ResourceNotFoundException;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.mappers.SpecialiteMapper;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Filiere;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Niveau;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Specialite;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.FiliereRepository;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.NiveauRepository;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.SpecialiteRepository;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.service.serviceInterface.SpecialiteService;
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
public class SpecialiteServiceImpl implements SpecialiteService {

    private final SpecialiteRepository specialiteRepository;
    private final SpecialiteMapper mapper;
    private final NiveauRepository niveauRepository;

    @Override
    public List<SpecialiteDTO> getAllSpecialites() {
        return specialiteRepository.findAll().stream().map(mapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public SpecialiteDTO getSpecialiteById(Long id) {
        Specialite s = specialiteRepository.findById(Objects.requireNonNull(id, "ID cannot be null"))
                .orElseThrow(() -> new ResourceNotFoundException("Spécialité non trouvée: " + id));
        return mapper.toDTO(s);
    }

    @Override
    @Transactional
    public SpecialiteDTO createSpecialite(SpecialiteCreateDTO dto) {
        Specialite s = mapper.fromCreateDTO(dto);
        if (dto.getNiveauId() != null) {
            Niveau niveau = niveauRepository.findById(dto.getNiveauId())
                    .orElseThrow(() -> new ResourceNotFoundException("Filière non trouvée: " + dto.getNiveauId()));
            s.setNiveau(niveau);
        }
        Specialite saved = specialiteRepository.save(Objects.requireNonNull(s, "Specialite cannot be null"));
        return mapper.toDTO(saved);
    }

    @Override
    @Transactional
    public SpecialiteDTO updateSpecialite(Long id, SpecialiteUpdateDTO dto) {
        Specialite s = specialiteRepository.findById(Objects.requireNonNull(id, "ID cannot be null"))
                .orElseThrow(() -> new ResourceNotFoundException("Spécialité non trouvée: " + id));

        if (dto.getIdSpecialite() != null) {
            s.setIdSpecialite(dto.getIdSpecialite());
        }
        if (dto.getNom() != null) {
            s.setNom(dto.getNom());
        }
        if (dto.getNiveauId() != null) {
            Niveau niveau = niveauRepository.findById(dto.getNiveauId())
                    .orElseThrow(() -> new ResourceNotFoundException("Filière non trouvée: " + dto.getNiveauId()));
            s.setNiveau(niveau);
        }

        Specialite updated = specialiteRepository.save(Objects.requireNonNull(s, "Specialite cannot be null"));
        return mapper.toDTO(updated);
    }

    @Override
    @Transactional
    public void deleteSpecialite(Long id) {
        if (!specialiteRepository.existsById(Objects.requireNonNull(id, "ID cannot be null"))) {
            throw new ResourceNotFoundException("Spécialité non trouvée: " + id);
        }
        specialiteRepository.deleteById(Objects.requireNonNull(id, "ID cannot be null"));
    }
}