package com.ISI.Appel_classe.Gestion_des_utilisateurs.service.impl;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.SurveillantCreateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.SurveillantUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response.SurveillantDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Role;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.enums.Statut;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Surveillant;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.exception.BadRequestException;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.exception.ResourceNotFoundException;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.RoleRepository;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.SurveillantRepository;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.UtilisateurRepository;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.service.serviceInterface.SurveillantService;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.mappers.SurveillantMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SurveillantServiceImpl implements SurveillantService {

    private final SurveillantRepository surveillantRepository;
    private final RoleRepository roleRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final SurveillantMapper mapper;
    private final com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.FiliereRepository filiereRepository;

    @Override
    public List<SurveillantDTO> getAllSurveillants() {
        return surveillantRepository.findAll().stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SurveillantDTO getSurveillantById(Long id) {
        Surveillant surveillant = surveillantRepository.findById(Objects.requireNonNull(id, "ID cannot be null"))
                .orElseThrow(() -> new ResourceNotFoundException("Surveillant non trouvé: " + id));
        return mapper.toDTO(surveillant);
    }

    @Override
    public SurveillantDTO getSurveillantByEmail(String email) {
        Surveillant surveillant = surveillantRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Surveillant non trouvé: " + email));
        return mapper.toDTO(surveillant);
    }

    @Override
    public List<SurveillantDTO> getSurveillantsByAffectation(String affectation) {
        return surveillantRepository.findByAffectation(affectation).stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SurveillantDTO createSurveillant(SurveillantCreateDTO dto) {
        // ⚠️ IMPORTANT: Vérifier l'unicité de l'email au niveau de TOUS les
        // utilisateurs
        if (utilisateurRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Un utilisateur avec cet email existe déjà: " + dto.getEmail());
        }

        // Créer un SURVEILLANT
        Surveillant surveillant = new Surveillant();

        // Attributs hérités
        surveillant.setNom(dto.getNom());
        surveillant.setPrenom(dto.getPrenom());
        surveillant.setEmail(dto.getEmail());
        surveillant.setPassword(dto.getPassword()); // TODO: Hasher
        surveillant.setStatut(dto.getStatut() != null ? dto.getStatut() : Statut.ACTIF);
        surveillant.setEnabled(true);
        surveillant.setAccountNonLocked(true);
        surveillant.setPremiereConnexion(false);
        surveillant.setCreatedAt(LocalDateTime.now());

        // Attributs spécifiques
        surveillant.setAffectation(dto.getAffectation());

        // Rôles
        Set<Role> roles = new HashSet<>();
        if (dto.getRoleNames() == null || dto.getRoleNames().isEmpty()) {
            Role roleSurveillant = roleRepository.findByName("SURVEILLANT")
                    .orElseThrow(() -> new RuntimeException("Rôle SURVEILLANT non trouvé"));
            roles.add(roleSurveillant);
        } else {
            for (String roleName : dto.getRoleNames()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new BadRequestException("Rôle non trouvé: " + roleName));
                roles.add(role);
            }
        }
        surveillant.setRoles(roles);

        Surveillant saved = surveillantRepository.save(surveillant);
        log.info("Surveillant créé - ID: {}", saved.getId());

        return mapper.toDTO(saved);
    }

    @Override
    @Transactional
    public SurveillantDTO updateSurveillant(Long id, SurveillantUpdateDTO dto) {
        Surveillant surveillant = surveillantRepository.findById(Objects.requireNonNull(id, "ID cannot be null"))
                .orElseThrow(() -> new ResourceNotFoundException("Surveillant non trouvé: " + id));

        if (dto.getNom() != null)
            surveillant.setNom(dto.getNom());
        if (dto.getPrenom() != null)
            surveillant.setPrenom(dto.getPrenom());

        if (dto.getEmail() != null) {
            // ⚠️ IMPORTANT: Vérifier l'unicité de l'email au niveau de TOUS les
            // utilisateurs
            if (!dto.getEmail().equals(surveillant.getEmail()) &&
                    utilisateurRepository.existsByEmail(dto.getEmail())) {
                throw new BadRequestException("Email déjà utilisé: " + dto.getEmail());
            }
            surveillant.setEmail(dto.getEmail());
        }

        if (dto.getStatut() != null)
            surveillant.setStatut(dto.getStatut());
        if (dto.getAffectation() != null)
            surveillant.setAffectation(dto.getAffectation());

        if (dto.getRoleNames() != null) {
            Set<Role> roles = new HashSet<>();
            for (String roleName : dto.getRoleNames()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new BadRequestException("Rôle non trouvé: " + roleName));
                roles.add(role);
            }
            surveillant.getRoles().clear();
            surveillant.setRoles(roles);
        }

        Surveillant updated = surveillantRepository.save(Objects.requireNonNull(surveillant, "Surveillant cannot be null"));
        return mapper.toDTO(updated);
    }

    @Override
    @Transactional
    public void deleteSurveillant(Long id) {
        if (!surveillantRepository.existsById(Objects.requireNonNull(id, "ID cannot be null"))) {
            throw new ResourceNotFoundException("Surveillant non trouvé: " + id);
        }
        surveillantRepository.deleteById(Objects.requireNonNull(id, "ID cannot be null"));
    }

    @Override
    @Transactional
    public SurveillantDTO assignSurveillantToFiliere(Long surveillantId, Long filiereId) {
        Surveillant s = surveillantRepository.findById(Objects.requireNonNull(surveillantId, "SurveillantId cannot be null"))
                .orElseThrow(() -> new ResourceNotFoundException("Surveillant non trouvé: " + surveillantId));
        com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Filiere filiere = filiereRepository.findById(Objects.requireNonNull(filiereId, "FiliereId cannot be null"))
                .orElseThrow(() -> new ResourceNotFoundException("Filière non trouvée: " + filiereId));
        s.setFiliere(filiere);
        Surveillant saved = surveillantRepository.save(s);
        return mapper.toDTO(saved);
    }

    @Override
    public List<SurveillantDTO> getSurveillantsByFiliere(Long filiereId) {
        return surveillantRepository.findByFiliere_Id(filiereId).stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }



}
