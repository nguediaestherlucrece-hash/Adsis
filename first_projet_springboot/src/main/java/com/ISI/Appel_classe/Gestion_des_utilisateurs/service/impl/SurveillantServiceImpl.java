package com.Gestion_des_utilisateurs.service.impl;

import com.Gestion_des_utilisateurs.dto.request.SurveillantCreateDTO;
import com.Gestion_des_utilisateurs.dto.request.SurveillantUpdateDTO;
import com.Gestion_des_utilisateurs.dto.response.SurveillantDTO;
import com.Gestion_des_utilisateurs.entity.Role;
import com.Gestion_des_utilisateurs.entity.Statut;
import com.Gestion_des_utilisateurs.entity.Surveillant;
import com.Gestion_des_utilisateurs.exception.BadRequestException;
import com.Gestion_des_utilisateurs.exception.ResourceNotFoundException;
import com.Gestion_des_utilisateurs.repository.RoleRepository;
import com.Gestion_des_utilisateurs.repository.SurveillantRepository;
import com.Gestion_des_utilisateurs.repository.UtilisateurRepository;
import com.Gestion_des_utilisateurs.service.SurveillantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SurveillantServiceImpl implements SurveillantService {

    private final SurveillantRepository surveillantRepository;
    private final RoleRepository roleRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Override
    public List<SurveillantDTO> getAllSurveillants() {
        return surveillantRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SurveillantDTO getSurveillantById(Long id) {
        Surveillant surveillant = surveillantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Surveillant non trouvé: " + id));
        return convertToDTO(surveillant);
    }

    @Override
    public SurveillantDTO getSurveillantByEmail(String email) {
        Surveillant surveillant = surveillantRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Surveillant non trouvé: " + email));
        return convertToDTO(surveillant);
    }

    @Override
    public List<SurveillantDTO> getSurveillantsByAffectation(String affectation) {
        return surveillantRepository.findByAffectation(affectation).stream()
                .map(this::convertToDTO)
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

        return convertToDTO(saved);
    }

    @Override
    @Transactional
    public SurveillantDTO updateSurveillant(Long id, SurveillantUpdateDTO dto) {
        Surveillant surveillant = surveillantRepository.findById(id)
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

        Surveillant updated = surveillantRepository.save(surveillant);
        return convertToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteSurveillant(Long id) {
        if (!surveillantRepository.existsById(id)) {
            throw new ResourceNotFoundException("Surveillant non trouvé: " + id);
        }
        surveillantRepository.deleteById(id);
    }

    private SurveillantDTO convertToDTO(Surveillant surveillant) {
        SurveillantDTO dto = new SurveillantDTO();

        dto.setId(surveillant.getId());
        dto.setNom(surveillant.getNom());
        dto.setPrenom(surveillant.getPrenom());
        dto.setEmail(surveillant.getEmail());
        dto.setStatut(surveillant.getStatut());
        dto.setEnabled(surveillant.getEnabled());
        dto.setAccountNonLocked(surveillant.getAccountNonLocked());
        dto.setPremiereConnexion(surveillant.getPremiereConnexion());
        dto.setCreatedAt(surveillant.getCreatedAt());

        Set<String> roleNames = surveillant.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        dto.setRoleNames(roleNames);

        dto.setAffectation(surveillant.getAffectation());

        if (surveillant.getSeances() != null) {
            dto.setNombreSeances(surveillant.getSeances().size());
        }

        return dto;
    }
}
