package com.ISI.Appel_classe.Gestion_des_utilisateurs.service.impl;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.AssistantPedagogiqueCreateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.AssistantPedagogiqueUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response.AssistantPedagogiqueDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.AssistantPedagogique;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Role;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.enums.Statut;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.exception.BadRequestException;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.exception.ResourceNotFoundException;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.AssistantPedagogiqueRepository;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.RoleRepository;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.UtilisateurRepository;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.service.serviceInterface.AssistantPedagogiqueService;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.mappers.AssistantPedagogiqueMapper;
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
public class AssistantPedagogiqueServiceImpl implements AssistantPedagogiqueService {

    private final AssistantPedagogiqueRepository assistantRepository;
    private final RoleRepository roleRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final AssistantPedagogiqueMapper mapper;
    private final com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.FiliereRepository filiereRepository;
    private final PermissionOverrideHelper permissionOverrideHelper;

    @Override
    public List<AssistantPedagogiqueDTO> getAllAssistants() {
        return assistantRepository.findAll().stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AssistantPedagogiqueDTO getAssistantById(Long id) {
        AssistantPedagogique assistant = assistantRepository.findById(Objects.requireNonNull(id, "ID cannot be null"))
                .orElseThrow(() -> new ResourceNotFoundException("Assistant pédagogique non trouvé: " + id));
        return mapper.toDTO(assistant);
    }

    @Override
    public AssistantPedagogiqueDTO getAssistantByEmail(String email) {
        AssistantPedagogique assistant = assistantRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Assistant pédagogique non trouvé: " + email));
        return mapper.toDTO(assistant);
    }

    @Override
    public List<AssistantPedagogiqueDTO> getAssistantsByDepartement(String departement) {
        return assistantRepository.findByDepartement(departement).stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AssistantPedagogiqueDTO createAssistant(AssistantPedagogiqueCreateDTO dto) {
        // ⚠️ IMPORTANT: Vérifier l'unicité de l'email au niveau de TOUS les
        // utilisateurs, pas seulement les assistants
        if (utilisateurRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Un utilisateur avec cet email existe déjà: " + dto.getEmail());
        }

        // Créer un ASSISTANT PEDAGOGIQUE
        AssistantPedagogique assistant = new AssistantPedagogique();

        // Attributs hérités
        assistant.setNom(dto.getNom());
        assistant.setPrenom(dto.getPrenom());
        assistant.setEmail(dto.getEmail());
        assistant.setPassword(dto.getPassword()); // TODO: Hasher
        assistant.setStatut(dto.getStatut() != null ? dto.getStatut() : Statut.ACTIF);
        assistant.setEnabled(true);
        assistant.setAccountNonLocked(true);
        assistant.setPremiereConnexion(false);
        assistant.setCreatedAt(LocalDateTime.now());

        // Attributs spécifiques
        assistant.setDepartement(dto.getDepartement());

        // Rôles
        Set<Role> roles = new HashSet<>();
        if (dto.getRoleNames() == null || dto.getRoleNames().isEmpty()) {
            Role roleAssistant = roleRepository.findByName("ASSISTANT_PEDAGOGIQUE")
                    .orElseThrow(() -> new RuntimeException("Rôle ASSISTANT_PEDAGOGIQUE non trouvé"));
            roles.add(roleAssistant);
        } else {
            for (String roleName : dto.getRoleNames()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new BadRequestException("Rôle non trouvé: " + roleName));
                roles.add(role);
            }
        }
        assistant.setRoles(roles);

        // Appliquer les permissions retirées
        permissionOverrideHelper.applyRemovedPermissions(assistant, dto.getRemovedPermissions());

        AssistantPedagogique saved = assistantRepository.save(assistant);
        log.info("Assistant pédagogique créé - ID: {}", saved.getId());

        return mapper.toDTO(saved);
    }

    @Override
    @Transactional
    public AssistantPedagogiqueDTO updateAssistant(Long id, AssistantPedagogiqueUpdateDTO dto) {
        AssistantPedagogique assistant = assistantRepository.findById(Objects.requireNonNull(id, "ID cannot be null"))
                .orElseThrow(() -> new ResourceNotFoundException("Assistant pédagogique non trouvé: " + id));

        if (dto.getNom() != null)
            assistant.setNom(dto.getNom());
        if (dto.getPrenom() != null)
            assistant.setPrenom(dto.getPrenom());

        if (dto.getEmail() != null) {
            // ⚠️ IMPORTANT: Vérifier l'unicité de l'email au niveau de TOUS les
            // utilisateurs
            if (!dto.getEmail().equals(assistant.getEmail()) &&
                    utilisateurRepository.existsByEmail(dto.getEmail())) {
                throw new BadRequestException("Email déjà utilisé: " + dto.getEmail());
            }
            assistant.setEmail(dto.getEmail());
        }

        if (dto.getStatut() != null)
            assistant.setStatut(dto.getStatut());
        if (dto.getDepartement() != null)
            assistant.setDepartement(dto.getDepartement());

        if (dto.getRoleNames() != null) {
            Set<Role> roles = new HashSet<>();
            for (String roleName : dto.getRoleNames()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new BadRequestException("Rôle non trouvé: " + roleName));
                roles.add(role);
            }
            assistant.getRoles().clear();
            assistant.setRoles(roles);
        }

        // Mettre à jour les permissions retirées
        permissionOverrideHelper.updateRemovedPermissions(assistant, dto.getRemovedPermissions());

        AssistantPedagogique updated = assistantRepository.save(Objects.requireNonNull(assistant, "AssistantPedagogique cannot be null"));
        return mapper.toDTO(updated);
    }

    @Override
    @Transactional
    public void deleteAssistant(Long id) {
        if (!assistantRepository.existsById(Objects.requireNonNull(id, "ID cannot be null"))) {
            throw new ResourceNotFoundException("Assistant pédagogique non trouvé: " + id);
        }
        assistantRepository.deleteById(Objects.requireNonNull(id, "ID cannot be null"));
    }

    @Override
    @Transactional
    public AssistantPedagogiqueDTO assignAssistantToFiliere(Long assistantId, Long filiereId) {
        AssistantPedagogique assistant = assistantRepository.findById(Objects.requireNonNull(assistantId, "AssistantId cannot be null"))
                .orElseThrow(() -> new ResourceNotFoundException("Assistant pédagogique non trouvé: " + assistantId));
        com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Filiere filiere = filiereRepository.findById(Objects.requireNonNull(filiereId, "FiliereId cannot be null"))
                .orElseThrow(() -> new ResourceNotFoundException("Filière non trouvée: " + filiereId));
        assistant.setFiliere(filiere);
        AssistantPedagogique saved = assistantRepository.save(assistant);
        return mapper.toDTO(saved);
    }

    @Override
    public List<AssistantPedagogiqueDTO> getAssistantsByFiliere(Long filiereId) {
        return assistantRepository.findByFiliere_Id(filiereId).stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }
}
