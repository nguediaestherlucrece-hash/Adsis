package com.ISI.Appel_classe.Gestion_des_utilisateurs.service.impl;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.AdministrateurCreateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.AdministrateurUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response.AdministrateurDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Administrateur;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Role;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.enums.Statut;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.exception.BadRequestException;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.exception.ResourceNotFoundException;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.AdministrateurRepository;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.RoleRepository;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.UtilisateurRepository;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.service.serviceInterface.AdministrateurService;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.mappers.AdministrateurMapper;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.util.UserPasswordGenerator;
import com.ISI.Appel_classe.mail.EmailSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implémentation du service Administrateur
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminstrateurServiceImpl implements AdministrateurService {

    private final AdministrateurRepository administrateurRepository;
    private final RoleRepository roleRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final AdministrateurMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final PermissionOverrideHelper permissionOverrideHelper;
    private final EmailSenderService emailSenderService;

    @Override
    public List<AdministrateurDTO> getAllAdministrateurs() {
        log.info("Récupération de tous les administrateurs");
        return administrateurRepository.findAll().stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AdministrateurDTO getAdministrateurById(Long id) {
        log.info("Récupération de l'administrateur id={}", id);
        Administrateur administrateur = administrateurRepository.findById(Objects.requireNonNull(id, "ID cannot be null"))
                .orElseThrow(() -> new ResourceNotFoundException("Administrateur non trouvé avec l'ID: " + id));
        return mapper.toDTO(administrateur);
    }

    @Override
    public AdministrateurDTO getAdministrateurByEmail(String email) {
        log.info("Récupération de l'administrateur email={}", email);
        Administrateur administrateur = administrateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Administrateur non trouvé avec l'email: " + email));
        return mapper.toDTO(administrateur);
    }

    @Override
    @Transactional
    public AdministrateurDTO createAdministrateur(AdministrateurCreateDTO dto) {
        log.info("Création d'un nouvel administrateur: {}", dto.getEmail());

        // ⚠️ IMPORTANT: Vérifier l'unicité de l'email au niveau de TOUS les
        // utilisateurs
        if (utilisateurRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Un utilisateur avec cet email existe déjà: " + dto.getEmail());
        }

        // Créer un ADMINISTRATEUR
        Administrateur administrateur = new Administrateur();

        // Attributs hérités de Utilisateur
        administrateur.setNom(dto.getNom());
        administrateur.setPrenom(dto.getPrenom());
        administrateur.setEmail(dto.getEmail());
        administrateur.setStatut(dto.getStatut() != null ? dto.getStatut() : Statut.ACTIF);

        String generatedPassword = UserPasswordGenerator.generateRandomPassword();
        administrateur.setPassword(passwordEncoder.encode(generatedPassword));

        administrateur.setEnabled(true);
        administrateur.setAccountNonLocked(true);
        administrateur.setPremiereConnexion(false);
        administrateur.setCreatedAt(LocalDateTime.now());

        // Assigner les rôles
        Set<Role> roles = new HashSet<>();
        if (dto.getRoleNames() == null || dto.getRoleNames().isEmpty()) {
            Role roleAdministrateur = roleRepository.findByName("ADMINISTRATEUR")
                    .orElseThrow(() -> new RuntimeException("Rôle ADMINISTRATEUR non trouvé"));
            roles.add(roleAdministrateur);
        } else {
            for (String roleName : dto.getRoleNames()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new BadRequestException("Rôle non trouvé: " + roleName));
                roles.add(role);
            }
        }
        administrateur.setRoles(roles);

        // Appliquer les permissions retirées
        permissionOverrideHelper.applyRemovedPermissions(administrateur, dto.getRemovedPermissions());

        Administrateur saved = administrateurRepository.save(administrateur);
        log.info("Administrateur créé avec succès - ID: {}", saved.getId());

        // Envoyer le mot de passe par email
        emailSenderService.sendPasswordEmail(saved.getEmail(), saved.getNom(), saved.getPrenom(), generatedPassword);

        return mapper.toDTO(saved);
    }

    @Override
    @Transactional
    public AdministrateurDTO updateAdministrateur(Long id, AdministrateurUpdateDTO dto) {
        log.info("Mise à jour de l'administrateur id={}", id);

        Administrateur administrateur = administrateurRepository.findById(Objects.requireNonNull(id, "ID cannot be null"))
                .orElseThrow(() -> new ResourceNotFoundException("Administrateur non trouvé: " + id));

        // Mettre à jour les attributs hérités
        if (dto.getNom() != null)
            administrateur.setNom(dto.getNom());
        if (dto.getPrenom() != null)
            administrateur.setPrenom(dto.getPrenom());

        if (dto.getEmail() != null) {
            // ⚠️ IMPORTANT: Vérifier l'unicité de l'email au niveau de TOUS les
            // utilisateurs
            if (!dto.getEmail().equals(administrateur.getEmail()) &&
                    utilisateurRepository.existsByEmail(dto.getEmail())) {
                throw new BadRequestException("Email déjà utilisé: " + dto.getEmail());
            }
            administrateur.setEmail(dto.getEmail());
        }

        if (dto.getStatut() != null) {
            administrateur.setStatut(dto.getStatut());
        }

        // Mettre à jour les rôles
        if (dto.getRoleNames() != null) {
            Set<Role> roles = new HashSet<>();
            for (String roleName : dto.getRoleNames()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new BadRequestException("Rôle non trouvé: " + roleName));
                roles.add(role);
            }
            administrateur.getRoles().clear();
            administrateur.setRoles(roles);
        }

        // Mettre à jour les permissions retirées
        permissionOverrideHelper.updateRemovedPermissions(administrateur, dto.getRemovedPermissions());

        Administrateur updated = administrateurRepository.save(Objects.requireNonNull(administrateur, "Administrateur cannot be null"));
        log.info("Administrateur mis à jour avec succès");

        return mapper.toDTO(updated);
    }

    @Override
    @Transactional
    public void deleteAdministrateur(Long id) {
        log.info("Suppression de l'administrateur id={}", id);
        Administrateur administrateur = administrateurRepository.findById(Objects.requireNonNull(id, "ID cannot be null"))
                .orElseThrow(() -> new ResourceNotFoundException("Administrateur non trouvé: " + id));

        administrateurRepository.delete(administrateur);
        log.info("Administrateur supprimé avec succès");
    }
}
