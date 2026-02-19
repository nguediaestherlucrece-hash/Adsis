package com.Gestion_des_utilisateurs.service.impl;

import com.Gestion_des_utilisateurs.dto.request.EtudiantCreateDTO;
import com.Gestion_des_utilisateurs.dto.request.EtudiantUpdateDTO;
import com.Gestion_des_utilisateurs.dto.response.ClasseDTO;
import com.Gestion_des_utilisateurs.dto.response.EtudiantDTO;
import com.Gestion_des_utilisateurs.entity.Classe;
import com.Gestion_des_utilisateurs.entity.Etudiant;
import com.Gestion_des_utilisateurs.entity.Role;
import com.Gestion_des_utilisateurs.entity.Statut;
import com.Gestion_des_utilisateurs.exception.BadRequestException;
import com.Gestion_des_utilisateurs.exception.ResourceNotFoundException;
import com.Gestion_des_utilisateurs.repository.ClasseRepository;
import com.Gestion_des_utilisateurs.repository.EtudiantRepository;
import com.Gestion_des_utilisateurs.repository.RoleRepository;
import com.Gestion_des_utilisateurs.repository.UtilisateurRepository;
import com.Gestion_des_utilisateurs.service.EtudiantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implémentation du service Etudiant
 * Gère la création, modification et suppression des étudiants
 * avec TOUS leurs attributs (hérités de Utilisateur + spécifiques)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EtudiantServiceImpl implements EtudiantService {

    private final EtudiantRepository etudiantRepository;
    private final ClasseRepository classeRepository;
    private final RoleRepository roleRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Override
    public List<EtudiantDTO> getAllEtudiants() {
        log.info("Récupération de tous les étudiants");
        return etudiantRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public EtudiantDTO getEtudiantById(Long id) {
        log.info("Récupération de l'étudiant avec id={}", id);
        Etudiant etudiant = etudiantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant non trouvé avec l'ID: " + id));
        return convertToDTO(etudiant);
    }

    @Override
    public EtudiantDTO getEtudiantByMatricule(String matricule) {
        log.info("Récupération de l'étudiant avec matricule={}", matricule);
        Etudiant etudiant = etudiantRepository.findByMatricule(matricule)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Étudiant non trouvé avec le matricule: " + matricule));
        return convertToDTO(etudiant);
    }

    @Override
    public EtudiantDTO getEtudiantByEmail(String email) {
        log.info("Récupération de l'étudiant avec email={}", email);
        Etudiant etudiant = etudiantRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant non trouvé avec l'email: " + email));
        return convertToDTO(etudiant);
    }

    @Override
    public List<EtudiantDTO> getEtudiantsByClasse(Long classeId) {
        log.info("Récupération des étudiants de la classe id={}", classeId);
        return etudiantRepository.findByClasseId(classeId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EtudiantDTO createEtudiant(EtudiantCreateDTO dto) {
        log.info("Création d'un nouvel étudiant: {}", dto.getEmail());

        // ========================================
        // VALIDATIONS
        // ========================================

        // Vérifier l'unicité de l'email au niveau de TOUS les utilisateurs
        // ⚠️ IMPORTANT: Utiliser utilisateurRepository, pas etudiantRepository
        if (utilisateurRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Un utilisateur avec cet email existe déjà: " + dto.getEmail());
        }

        // Vérifier l'unicité du matricule
        if (dto.getMatricule() != null && etudiantRepository.existsByMatricule(dto.getMatricule())) {
            throw new BadRequestException("Un étudiant avec ce matricule existe déjà: " + dto.getMatricule());
        }

        // ========================================
        // CRÉATION DE L'ENTITÉ ETUDIANT
        // ========================================

        // ⚠️ IMPORTANT: Créer un ETUDIANT, pas un Utilisateur !
        Etudiant etudiant = new Etudiant();

        // ========================================
        // MAPPER LES CHAMPS HÉRITÉS DE UTILISATEUR
        // ========================================

        etudiant.setNom(dto.getNom());
        etudiant.setPrenom(dto.getPrenom());
        etudiant.setEmail(dto.getEmail());

        // TODO: Hasher le mot de passe avec BCryptPasswordEncoder
        etudiant.setPassword(dto.getPassword());

        // Statut par défaut ACTIF si non fourni
        etudiant.setStatut(dto.getStatut() != null ? dto.getStatut() : Statut.ACTIF);

        // Champs par défaut de Utilisateur
        etudiant.setEnabled(true);
        etudiant.setAccountNonLocked(true);
        etudiant.setPremiereConnexion(false);
        etudiant.setCreatedAt(LocalDateTime.now());

        // ========================================
        // MAPPER LES CHAMPS SPÉCIFIQUES À ETUDIANT
        // ========================================

        etudiant.setMatricule(dto.getMatricule());
        etudiant.setDateNaissance(dto.getDateNaissance());
        etudiant.setTelephone(dto.getTelephone());

        // ========================================
        // ASSIGNER LES RÔLES
        // ========================================

        Set<Role> roles = new HashSet<>();

        if (dto.getRoleNames() == null || dto.getRoleNames().isEmpty()) {
            // Par défaut, assigner le rôle ETUDIANT
            log.info("Aucun rôle fourni, assignation du rôle ETUDIANT par défaut");
            Role roleEtudiant = roleRepository.findByName("ETUDIANT")
                    .orElseThrow(() -> new RuntimeException("Rôle ETUDIANT non trouvé dans la base de données"));
            roles.add(roleEtudiant);
        } else {
            // Charger et assigner les rôles fournis
            for (String roleName : dto.getRoleNames()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new BadRequestException("Rôle non trouvé: " + roleName));
                roles.add(role);
            }
        }

        etudiant.setRoles(roles);

        // ========================================
        // ASSIGNER LA CLASSE SI FOURNIE
        // ========================================

        if (dto.getClasseId() != null) {
            Classe classe = classeRepository.findById(dto.getClasseId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Classe non trouvée avec l'ID: " + dto.getClasseId()));
            etudiant.setClasse(classe);
            log.info("Étudiant assigné à la classe: {}", classe.getNomClasse());
        }

        // ========================================
        // SAUVEGARDER L'ETUDIANT
        // ========================================

        Etudiant saved = etudiantRepository.save(etudiant);
        log.info("Étudiant créé avec succès avec l'ID: {}", saved.getId());

        return convertToDTO(saved);
    }

    @Override
    @Transactional
    public EtudiantDTO updateEtudiant(Long id, EtudiantUpdateDTO dto) {
        log.info("Mise à jour de l'étudiant id={}", id);

        // Récupérer l'étudiant existant
        Etudiant etudiant = etudiantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant non trouvé avec l'ID: " + id));

        // ========================================
        // METTRE À JOUR LES CHAMPS HÉRITÉS DE UTILISATEUR
        // ========================================

        if (dto.getNom() != null) {
            etudiant.setNom(dto.getNom());
        }

        if (dto.getPrenom() != null) {
            etudiant.setPrenom(dto.getPrenom());
        }

        if (dto.getEmail() != null) {
            // Vérifier que le nouvel email n'est pas déjà utilisé par un autre utilisateur
            // ⚠️ IMPORTANT: Utiliser utilisateurRepository, pas etudiantRepository
            if (!dto.getEmail().equals(etudiant.getEmail()) &&
                    utilisateurRepository.existsByEmail(dto.getEmail())) {
                throw new BadRequestException("L'email est déjà utilisé par un autre utilisateur: " + dto.getEmail());
            }
            etudiant.setEmail(dto.getEmail());
        }

        if (dto.getStatut() != null) {
            etudiant.setStatut(dto.getStatut());
        }

        // ========================================
        // METTRE À JOUR LES RÔLES SI FOURNIS
        // ========================================

        if (dto.getRoleNames() != null) {
            Set<Role> roles = new HashSet<>();
            for (String roleName : dto.getRoleNames()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new BadRequestException("Rôle non trouvé: " + roleName));
                roles.add(role);
            }
            etudiant.getRoles().clear();
            etudiant.setRoles(roles);
            log.info("Rôles mis à jour pour l'étudiant id={}", id);
        }

        // ========================================
        // METTRE À JOUR LES CHAMPS SPÉCIFIQUES À ETUDIANT
        // ========================================

        if (dto.getMatricule() != null) {
            // Vérifier que le nouveau matricule n'est pas déjà utilisé
            if (!dto.getMatricule().equals(etudiant.getMatricule()) &&
                    etudiantRepository.existsByMatricule(dto.getMatricule())) {
                throw new BadRequestException(
                        "Le matricule est déjà utilisé par un autre étudiant: " + dto.getMatricule());
            }
            etudiant.setMatricule(dto.getMatricule());
        }

        if (dto.getDateNaissance() != null) {
            etudiant.setDateNaissance(dto.getDateNaissance());
        }

        if (dto.getTelephone() != null) {
            etudiant.setTelephone(dto.getTelephone());
        }

        // ========================================
        // METTRE À JOUR LA CLASSE
        // ========================================

        if (dto.getClasseId() != null) {
            Classe classe = classeRepository.findById(dto.getClasseId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Classe non trouvée avec l'ID: " + dto.getClasseId()));
            etudiant.setClasse(classe);
            log.info("Étudiant réassigné à la classe: {}", classe.getNomClasse());
        }

        // ========================================
        // SAUVEGARDER LES MODIFICATIONS
        // ========================================

        Etudiant updated = etudiantRepository.save(etudiant);
        log.info("Étudiant mis à jour avec succès");

        return convertToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteEtudiant(Long id) {
        log.info("Suppression de l'étudiant id={}", id);

        if (!etudiantRepository.existsById(id)) {
            throw new ResourceNotFoundException("Étudiant non trouvé avec l'ID: " + id);
        }

        etudiantRepository.deleteById(id);
        log.info("Étudiant supprimé avec succès");
    }

    @Override
    @Transactional
    public EtudiantDTO assignerClasse(Long etudiantId, Long classeId) {
        log.info("Assignation de la classe id={} à l'étudiant id={}", classeId, etudiantId);

        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant non trouvé avec l'ID: " + etudiantId));

        Classe classe = classeRepository.findById(classeId)
                .orElseThrow(() -> new ResourceNotFoundException("Classe non trouvée avec l'ID: " + classeId));

        etudiant.setClasse(classe);
        Etudiant updated = etudiantRepository.save(etudiant);

        log.info("Classe assignée avec succès");
        return convertToDTO(updated);
    }

    // ========================================
    // MÉTHODE DE CONVERSION ENTITY → DTO
    // ========================================

    /**
     * Convertit une entité Etudiant en EtudiantDTO
     * Inclut TOUS les champs (hérités + spécifiques)
     */
    private EtudiantDTO convertToDTO(Etudiant etudiant) {
        EtudiantDTO dto = new EtudiantDTO();

        // ========================================
        // MAPPER LES CHAMPS HÉRITÉS DE UTILISATEUR
        // ========================================

        dto.setId(etudiant.getId());
        dto.setNom(etudiant.getNom());
        dto.setPrenom(etudiant.getPrenom());
        dto.setEmail(etudiant.getEmail());
        // Le password n'est JAMAIS retourné pour des raisons de sécurité
        dto.setStatut(etudiant.getStatut());
        dto.setEnabled(etudiant.getEnabled());
        dto.setAccountNonLocked(etudiant.getAccountNonLocked());
        dto.setPremiereConnexion(etudiant.getPremiereConnexion());
        dto.setCreatedAt(etudiant.getCreatedAt());

        // Mapper les rôles en noms de rôles
        Set<String> roleNames = etudiant.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        dto.setRoleNames(roleNames);

        // ========================================
        // MAPPER LES CHAMPS SPÉCIFIQUES À ETUDIANT
        // ========================================

        dto.setMatricule(etudiant.getMatricule());
        dto.setDateNaissance(etudiant.getDateNaissance());
        dto.setTelephone(etudiant.getTelephone());

        // ========================================
        // MAPPER LES RELATIONS
        // ========================================

        if (etudiant.getClasse() != null) {
            ClasseDTO classeDTO = new ClasseDTO();
            classeDTO.setId(etudiant.getClasse().getId());
            classeDTO.setNomClasse(etudiant.getClasse().getNomClasse());
            classeDTO.setNiveau(etudiant.getClasse().getNiveau());
            dto.setClasse(classeDTO);
        }

        // Compter les présences si la liste est chargée
        if (etudiant.getPresences() != null) {
            dto.setNombrePresences(etudiant.getPresences().size());
        }

        return dto;
    }
}
