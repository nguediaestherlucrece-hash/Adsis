package com.ISI.Appel_classe.service.impl;

import com.ISI.Appel_classe.dto.request.AssistantPedagogiqueCreateDTO;
import com.ISI.Appel_classe.dto.request.AssistantPedagogiqueUpdateDTO;
import com.ISI.Appel_classe.dto.response.AssistantPedagogiqueDTO;
import com.ISI.Appel_classe.entity.AssistantPedagogique;
import com.ISI.Appel_classe.entity.Role;
import com.ISI.Appel_classe.entity.Statut;
import com.ISI.Appel_classe.exception.BadRequestException;
import com.ISI.Appel_classe.exception.ResourceNotFoundException;
import com.ISI.Appel_classe.repository.AssistantPedagogiqueRepository;
import com.ISI.Appel_classe.repository.RoleRepository;
import com.ISI.Appel_classe.service.AssistantPedagogiqueService;
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
public class AssistantPedagogiqueServiceImpl implements AssistantPedagogiqueService {

    private final AssistantPedagogiqueRepository assistantRepository;
    private final RoleRepository roleRepository;

    @Override
    public List<AssistantPedagogiqueDTO> getAllAssistants() {
        return assistantRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AssistantPedagogiqueDTO getAssistantById(Long id) {
        AssistantPedagogique assistant = assistantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assistant pédagogique non trouvé: " + id));
        return convertToDTO(assistant);
    }

    @Override
    public AssistantPedagogiqueDTO getAssistantByEmail(String email) {
        AssistantPedagogique assistant = assistantRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Assistant pédagogique non trouvé: " + email));
        return convertToDTO(assistant);
    }

    @Override
    public List<AssistantPedagogiqueDTO> getAssistantsByDepartement(String departement) {
        return assistantRepository.findByDepartement(departement).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AssistantPedagogiqueDTO createAssistant(AssistantPedagogiqueCreateDTO dto) {
        if (assistantRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Un assistant avec cet email existe déjà: " + dto.getEmail());
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

        AssistantPedagogique saved = assistantRepository.save(assistant);
        log.info("Assistant pédagogique créé - ID: {}", saved.getId());

        return convertToDTO(saved);
    }

    @Override
    @Transactional
    public AssistantPedagogiqueDTO updateAssistant(Long id, AssistantPedagogiqueUpdateDTO dto) {
        AssistantPedagogique assistant = assistantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assistant pédagogique non trouvé: " + id));

        if (dto.getNom() != null)
            assistant.setNom(dto.getNom());
        if (dto.getPrenom() != null)
            assistant.setPrenom(dto.getPrenom());

        if (dto.getEmail() != null) {
            if (!dto.getEmail().equals(assistant.getEmail()) &&
                    assistantRepository.existsByEmail(dto.getEmail())) {
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

        AssistantPedagogique updated = assistantRepository.save(assistant);
        return convertToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteAssistant(Long id) {
        if (!assistantRepository.existsById(id)) {
            throw new ResourceNotFoundException("Assistant pédagogique non trouvé: " + id);
        }
        assistantRepository.deleteById(id);
    }

    private AssistantPedagogiqueDTO convertToDTO(AssistantPedagogique assistant) {
        AssistantPedagogiqueDTO dto = new AssistantPedagogiqueDTO();

        dto.setId(assistant.getId());
        dto.setNom(assistant.getNom());
        dto.setPrenom(assistant.getPrenom());
        dto.setEmail(assistant.getEmail());
        dto.setStatut(assistant.getStatut());
        dto.setEnabled(assistant.getEnabled());
        dto.setAccountNonLocked(assistant.getAccountNonLocked());
        dto.setPremiereConnexion(assistant.getPremiereConnexion());
        dto.setCreatedAt(assistant.getCreatedAt());

        Set<String> roleNames = assistant.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        dto.setRoleNames(roleNames);

        dto.setDepartement(assistant.getDepartement());

        if (assistant.getJustificatifsValides() != null) {
            dto.setNombreJustificatifsValides(assistant.getJustificatifsValides().size());
        }

        if (assistant.getEmploisDuTemps() != null) {
            dto.setNombreEmploisDuTemps(assistant.getEmploisDuTemps().size());
        }

        return dto;
    }
}
