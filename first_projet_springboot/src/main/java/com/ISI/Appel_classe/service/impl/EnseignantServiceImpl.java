package com.ISI.Appel_classe.service.impl;

import com.ISI.Appel_classe.dto.request.EnseignantCreateDTO;
import com.ISI.Appel_classe.dto.request.EnseignantUpdateDTO;
import com.ISI.Appel_classe.dto.response.EnseignantDTO;
import com.ISI.Appel_classe.entity.CoursUE;
import com.ISI.Appel_classe.entity.Enseignant;
import com.ISI.Appel_classe.entity.Role;
import com.ISI.Appel_classe.entity.Statut;
import com.ISI.Appel_classe.exception.BadRequestException;
import com.ISI.Appel_classe.exception.ResourceNotFoundException;
import com.ISI.Appel_classe.repository.CoursUERepository;
import com.ISI.Appel_classe.repository.EnseignantRepository;
import com.ISI.Appel_classe.repository.RoleRepository;
import com.ISI.Appel_classe.repository.UtilisateurRepository;
import com.ISI.Appel_classe.service.EnseignantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implémentation du service Enseignant
 * Gère TOUS les attributs (hérités + spécifiques)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnseignantServiceImpl implements EnseignantService {

    private final EnseignantRepository enseignantRepository;
    private final CoursUERepository coursUERepository;
    private final RoleRepository roleRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Override
    public List<EnseignantDTO> getAllEnseignants() {
        log.info("Récupération de tous les enseignants");
        return enseignantRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public EnseignantDTO getEnseignantById(Long id) {
        log.info("Récupération de l'enseignant id={}", id);
        Enseignant enseignant = enseignantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enseignant non trouvé avec l'ID: " + id));
        return convertToDTO(enseignant);
    }

    @Override
    public EnseignantDTO getEnseignantByEmail(String email) {
        log.info("Récupération de l'enseignant email={}", email);
        Enseignant enseignant = enseignantRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Enseignant non trouvé avec l'email: " + email));
        return convertToDTO(enseignant);
    }

    @Override
    public List<EnseignantDTO> getEnseignantsBySpecialite(String specialite) {
        log.info("Récupération des enseignants de spécialité: {}", specialite);
        return enseignantRepository.findBySpecialite(specialite).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EnseignantDTO> getEnseignantsByGrade(String grade) {
        log.info("Récupération des enseignants de grade: {}", grade);
        return enseignantRepository.findByGrade(grade).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EnseignantDTO createEnseignant(EnseignantCreateDTO dto) {
        log.info("Création d'un nouvel enseignant: {}", dto.getEmail());

        // ⚠️ IMPORTANT: Vérifier l'unicité de l'email au niveau de TOUS les
        // utilisateurs
        if (utilisateurRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Un utilisateur avec cet email existe déjà: " + dto.getEmail());
        }

        // Créer un ENSEIGNANT
        Enseignant enseignant = new Enseignant();

        // Attributs hérités de Utilisateur
        enseignant.setNom(dto.getNom());
        enseignant.setPrenom(dto.getPrenom());
        enseignant.setEmail(dto.getEmail());
        enseignant.setPassword(dto.getPassword()); // TODO: Hasher
        enseignant.setStatut(dto.getStatut() != null ? dto.getStatut() : Statut.ACTIF);
        enseignant.setEnabled(true);
        enseignant.setAccountNonLocked(true);
        enseignant.setPremiereConnexion(false);
        enseignant.setCreatedAt(LocalDateTime.now());

        // Attributs spécifiques à Enseignant
        enseignant.setSpecialite(dto.getSpecialite());
        enseignant.setGrade(dto.getGrade());

        // Assigner les rôles
        Set<Role> roles = new HashSet<>();
        if (dto.getRoleNames() == null || dto.getRoleNames().isEmpty()) {
            Role roleEnseignant = roleRepository.findByName("ENSEIGNANT")
                    .orElseThrow(() -> new RuntimeException("Rôle ENSEIGNANT non trouvé"));
            roles.add(roleEnseignant);
        } else {
            for (String roleName : dto.getRoleNames()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new BadRequestException("Rôle non trouvé: " + roleName));
                roles.add(role);
            }
        }
        enseignant.setRoles(roles);

        // Assigner les cours si fournis
        if (dto.getCoursIds() != null && !dto.getCoursIds().isEmpty()) {
            List<CoursUE> cours = new ArrayList<>();
            for (Long coursId : dto.getCoursIds()) {
                CoursUE coursUE = coursUERepository.findById(coursId)
                        .orElseThrow(() -> new ResourceNotFoundException("Cours non trouvé: " + coursId));
                cours.add(coursUE);
            }
            enseignant.setCoursEnseignes(cours);
        }

        Enseignant saved = enseignantRepository.save(enseignant);
        log.info("Enseignant créé avec succès - ID: {}", saved.getId());

        return convertToDTO(saved);
    }

    @Override
    @Transactional
    public EnseignantDTO updateEnseignant(Long id, EnseignantUpdateDTO dto) {
        log.info("Mise à jour de l'enseignant id={}", id);

        Enseignant enseignant = enseignantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enseignant non trouvé: " + id));

        // Mettre à jour les attributs hérités
        if (dto.getNom() != null)
            enseignant.setNom(dto.getNom());
        if (dto.getPrenom() != null)
            enseignant.setPrenom(dto.getPrenom());

        if (dto.getEmail() != null) {
            // ⚠️ IMPORTANT: Vérifier l'unicité de l'email au niveau de TOUS les
            // utilisateurs
            if (!dto.getEmail().equals(enseignant.getEmail()) &&
                    utilisateurRepository.existsByEmail(dto.getEmail())) {
                throw new BadRequestException("Email déjà utilisé: " + dto.getEmail());
            }
            enseignant.setEmail(dto.getEmail());
        }

        if (dto.getStatut() != null)
            enseignant.setStatut(dto.getStatut());

        // Mettre à jour les rôles
        if (dto.getRoleNames() != null) {
            Set<Role> roles = new HashSet<>();
            for (String roleName : dto.getRoleNames()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new BadRequestException("Rôle non trouvé: " + roleName));
                roles.add(role);
            }
            enseignant.getRoles().clear();
            enseignant.setRoles(roles);
        }

        // Mettre à jour les attributs spécifiques
        if (dto.getSpecialite() != null)
            enseignant.setSpecialite(dto.getSpecialite());
        if (dto.getGrade() != null)
            enseignant.setGrade(dto.getGrade());

        // Mettre à jour les cours
        if (dto.getCoursIds() != null) {
            List<CoursUE> cours = new ArrayList<>();
            for (Long coursId : dto.getCoursIds()) {
                CoursUE coursUE = coursUERepository.findById(coursId)
                        .orElseThrow(() -> new ResourceNotFoundException("Cours non trouvé: " + coursId));
                cours.add(coursUE);
            }
            enseignant.getCoursEnseignes().clear();
            enseignant.setCoursEnseignes(cours);
        }

        Enseignant updated = enseignantRepository.save(enseignant);
        log.info("Enseignant mis à jour avec succès");

        return convertToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteEnseignant(Long id) {
        log.info("Suppression de l'enseignant id={}", id);
        if (!enseignantRepository.existsById(id)) {
            throw new ResourceNotFoundException("Enseignant non trouvé: " + id);
        }
        enseignantRepository.deleteById(id);
        log.info("Enseignant supprimé avec succès");
    }

    @Override
    @Transactional
    public EnseignantDTO assignerCours(Long enseignantId, List<Long> coursIds) {
        log.info("Assignation de {} cours à l'enseignant id={}", coursIds.size(), enseignantId);

        Enseignant enseignant = enseignantRepository.findById(enseignantId)
                .orElseThrow(() -> new ResourceNotFoundException("Enseignant non trouvé: " + enseignantId));

        List<CoursUE> cours = new ArrayList<>();
        for (Long coursId : coursIds) {
            CoursUE coursUE = coursUERepository.findById(coursId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cours non trouvé: " + coursId));
            cours.add(coursUE);
        }

        enseignant.setCoursEnseignes(cours);
        Enseignant updated = enseignantRepository.save(enseignant);

        log.info("Cours assignés avec succès");
        return convertToDTO(updated);
    }

    private EnseignantDTO convertToDTO(Enseignant enseignant) {
        EnseignantDTO dto = new EnseignantDTO();

        // Attributs hérités
        dto.setId(enseignant.getId());
        dto.setNom(enseignant.getNom());
        dto.setPrenom(enseignant.getPrenom());
        dto.setEmail(enseignant.getEmail());
        dto.setStatut(enseignant.getStatut());
        dto.setEnabled(enseignant.getEnabled());
        dto.setAccountNonLocked(enseignant.getAccountNonLocked());
        dto.setPremiereConnexion(enseignant.getPremiereConnexion());
        dto.setCreatedAt(enseignant.getCreatedAt());

        Set<String> roleNames = enseignant.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        dto.setRoleNames(roleNames);

        // Attributs spécifiques
        dto.setSpecialite(enseignant.getSpecialite());
        dto.setGrade(enseignant.getGrade());

        // Relations
        if (enseignant.getCoursEnseignes() != null) {
            List<EnseignantDTO.CoursSimpleDTO> cours = enseignant.getCoursEnseignes().stream()
                    .map(c -> {
                        EnseignantDTO.CoursSimpleDTO coursDTO = new EnseignantDTO.CoursSimpleDTO();
                        coursDTO.setId(c.getId());
                        coursDTO.setNomCours(c.getIntitule());
                        coursDTO.setCode(c.getCodeUE());
                        return coursDTO;
                    })
                    .collect(Collectors.toList());
            dto.setCoursEnseignes(cours);
        }

        if (enseignant.getSeances() != null) {
            dto.setNombreSeances(enseignant.getSeances().size());
        }

        return dto;
    }
}
