package com.ISI.Appel_classe.Gestion_des_utilisateurs.service.impl;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.UtilisateurCreateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.UtilisateurUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response.UtilisateurDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Role;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.enums.Statut;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Utilisateur;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.exception.BadRequestException;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.exception.ResourceNotFoundException;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.RoleRepository;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.UtilisateurRepository;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.service.serviceInterface.UtilisateurService;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.mappers.UtilisateurMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UtilisateurServiceImpl implements UtilisateurService {

    private final UtilisateurRepository repository;
    private final RoleRepository roleRepository;
    private final UtilisateurMapper mapper;

    @Override
    public List<UtilisateurDTO> getAllUtilisateurs() {
        return repository.findAll().stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UtilisateurDTO getUtilisateurById(Long id) {
        Utilisateur utilisateur = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID: " + id));
        return mapper.toDTO(utilisateur);
    }

    @Override
    public UtilisateurDTO getUtilisateurByEmail(String email) {
        Utilisateur utilisateur = repository.findByEmail(email);
        if (utilisateur == null) {
            throw new ResourceNotFoundException("Utilisateur non trouvé avec l'email: " + email);
        }
        return mapper.toDTO(utilisateur);
    }

    @Override
    @Transactional
    public UtilisateurDTO createUtilisateur(UtilisateurCreateDTO dto) {
        if (repository.findByEmail(dto.getEmail()) != null) {
            throw new BadRequestException("Un utilisateur avec cet email existe déjà");
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(dto.getNom());
        utilisateur.setPrenom(dto.getPrenom());
        utilisateur.setEmail(dto.getEmail());
        utilisateur.setPassword(dto.getPassword()); // TODO: Hash le mot de passe avec BCrypt
        utilisateur.setStatut(dto.getStatut() != null ? dto.getStatut() : Statut.ACTIF);

        // Assigner les rôles
        if (dto.getRoleNames() != null && !dto.getRoleNames().isEmpty()) {
            Set<Role> roles = new HashSet<>();
            for (String roleName : dto.getRoleNames()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new BadRequestException("Rôle non trouvé: " + roleName));
                roles.add(role);
            }
            utilisateur.setRoles(roles);
        }

        Utilisateur saved = repository.save(utilisateur);
        return mapper.toDTO(saved);
    }

    @Override
    @Transactional
    public UtilisateurDTO updateUtilisateur(Long id, UtilisateurUpdateDTO dto) {
        Utilisateur utilisateur = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID: " + id));

        utilisateur.setNom(dto.getNom());
        utilisateur.setPrenom(dto.getPrenom());
        utilisateur.setEmail(dto.getEmail());

        // Mettre à jour les rôles
        if (dto.getRoleNames() != null) {
            Set<Role> roles = new HashSet<>();
            for (String roleName : dto.getRoleNames()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new BadRequestException("Rôle non trouvé: " + roleName));
                roles.add(role);
            }
            utilisateur.getRoles().clear();
            utilisateur.setRoles(roles);
        }

        if (dto.getStatut() != null) {
            utilisateur.setStatut(dto.getStatut());
        }

        Utilisateur updated = repository.save(utilisateur);
        return mapper.toDTO(updated);
    }

    @Override
    @Transactional
    public void deleteUtilisateur(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Utilisateur non trouvé avec l'ID: " + id);
        }
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public UtilisateurDTO toggleStatut(String email) {
        Utilisateur utilisateur = repository.findByEmail(email);
        if (utilisateur == null) {
            throw new ResourceNotFoundException("Utilisateur non trouvé avec l'email: " + email);
        }

        utilisateur.setStatut(utilisateur.getStatut() == Statut.ACTIF ? Statut.INACTIF : Statut.ACTIF);
        Utilisateur updated = repository.save(utilisateur);
        return mapper.toDTO(updated);
    }

}

