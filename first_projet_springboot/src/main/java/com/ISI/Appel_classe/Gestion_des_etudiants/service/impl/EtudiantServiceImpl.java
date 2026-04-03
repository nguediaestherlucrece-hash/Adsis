package com.ISI.Appel_classe.Gestion_des_etudiants.service.impl;

import com.ISI.Appel_classe.Gestion_des_etudiants.dto.request.EtudiantCreateDTO;
import com.ISI.Appel_classe.Gestion_des_etudiants.dto.request.EtudiantUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_etudiants.dto.response.EtudiantDTO;
import com.ISI.Appel_classe.Gestion_des_etudiants.dto.response.EtudiantImportResponseDTO;
import com.ISI.Appel_classe.Gestion_des_etudiants.model.Classe;
import com.ISI.Appel_classe.Gestion_des_etudiants.model.Etudiant;
import com.ISI.Appel_classe.Gestion_des_etudiants.util.ExcelEtudiantReader;
import com.ISI.Appel_classe.Gestion_des_etudiants.util.ExcelEtudiantTemplateGenerator;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Role;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.enums.Statut;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.exception.BadRequestException;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.exception.ResourceNotFoundException;
import com.ISI.Appel_classe.Gestion_des_etudiants.repository.ClasseRepository;
import com.ISI.Appel_classe.Gestion_des_etudiants.repository.EtudiantRepository;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.RoleRepository;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.UtilisateurRepository;
import com.ISI.Appel_classe.Gestion_des_etudiants.service.serviceInterface.EtudiantService;
import com.ISI.Appel_classe.Gestion_des_etudiants.mappers.EtudiantMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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
    private final EtudiantMapper mapper;
    private final ExcelEtudiantReader excelEtudiantReader;
    private final ExcelEtudiantTemplateGenerator excelTemplateGenerator;


    @Override
    public List<EtudiantDTO> getAllEtudiants() {
        log.info("Récupération de tous les étudiants");
        return etudiantRepository.findAll().stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public EtudiantDTO getEtudiantById(Long id) {
        log.info("Récupération de l'étudiant avec id={}", id);
        Etudiant etudiant = etudiantRepository.findById(Objects.requireNonNull(id, "ID cannot be null"))
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant non trouvé avec l'ID: " + id));
        return mapper.toDTO(etudiant);
    }

    @Override
    public EtudiantDTO getEtudiantByMatricule(String matricule) {
        log.info("Récupération de l'étudiant avec matricule={}", matricule);
        Etudiant etudiant = etudiantRepository.findByMatricule(matricule)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Étudiant non trouvé avec le matricule: " + matricule));
        return mapper.toDTO(etudiant);
    }

    @Override
    public EtudiantDTO getEtudiantByEmail(String email) {
        log.info("Récupération de l'étudiant avec email={}", email);
        Etudiant etudiant = etudiantRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant non trouvé avec l'email: " + email));
        return mapper.toDTO(etudiant);
    }

    @Override
    public List<EtudiantDTO> getEtudiantsByClasse(Long classeId) {
        log.info("Récupération des étudiants de la classe id={}", classeId);
        return etudiantRepository.findByClasseId(classeId).stream()
                .map(mapper::toDTO)
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
            Classe classe = classeRepository.findById(Objects.requireNonNull(dto.getClasseId(), "ClasseId cannot be null"))
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

        return mapper.toDTO(saved);
    }

    @Override
    @Transactional
    public EtudiantDTO updateEtudiant(Long id, EtudiantUpdateDTO dto) {
        log.info("Mise à jour de l'étudiant id={}", id);

        // Récupérer l'étudiant existant
        Etudiant etudiant = etudiantRepository.findById(Objects.requireNonNull(id, "ID cannot be null"))
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
            Classe classe = classeRepository.findById(Objects.requireNonNull(dto.getClasseId(), "ClasseId cannot be null"))
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Classe non trouvée avec l'ID: " + dto.getClasseId()));
            etudiant.setClasse(classe);
            log.info("Étudiant réassigné à la classe: {}", classe.getNomClasse());
        }

        // ========================================
        // SAUVEGARDER LES MODIFICATIONS
        // ========================================

        Etudiant updated = etudiantRepository.save(Objects.requireNonNull(etudiant, "Etudiant cannot be null"));
        log.info("Étudiant mis à jour avec succès");

        return mapper.toDTO(updated);
    }

    @Override
    @Transactional
    public void deleteEtudiant(Long id) {
        log.info("Suppression de l'étudiant id={}", id);

        if (!etudiantRepository.existsById(Objects.requireNonNull(id, "ID cannot be null"))) {
            throw new ResourceNotFoundException("Étudiant non trouvé avec l'ID: " + id);
        }

        etudiantRepository.deleteById(Objects.requireNonNull(id, "ID cannot be null"));
        log.info("Étudiant supprimé avec succès");
    }

    @Override
    @Transactional
    public EtudiantDTO assignerClasse(Long etudiantId, Long classeId) {
        log.info("Assignation de la classe id={} à l'étudiant id={}", classeId, etudiantId);

        Etudiant etudiant = etudiantRepository.findById(Objects.requireNonNull(etudiantId, "EtudiantId cannot be null"))
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant non trouvé avec l'ID: " + etudiantId));

        Classe classe = classeRepository.findById(Objects.requireNonNull(classeId, "ClasseId cannot be null"))
                .orElseThrow(() -> new ResourceNotFoundException("Classe non trouvée avec l'ID: " + classeId));

        etudiant.setClasse(classe);
        Etudiant updated = etudiantRepository.save(etudiant);

        log.info("Classe assignée avec succès");
        return mapper.toDTO(updated);
    }

    @Override
    @Transactional
    public EtudiantImportResponseDTO importEtudiantsFromExcel(MultipartFile file, Long classeId) {
        log.info("🔄 Début de l'import d'étudiants depuis le fichier Excel: {} vers la classe {}", file.getOriginalFilename(), classeId);

        EtudiantImportResponseDTO response = EtudiantImportResponseDTO.builder()
                .createdEtudiants(new java.util.ArrayList<>())
                .errors(new java.util.ArrayList<>())
                .build();

        if (classeId == null) {
            throw new BadRequestException("Le paramètre classeId est requis pour l'import des étudiants");
        }

        Classe classe = classeRepository.findById(classeId)
                .orElseThrow(() -> new ResourceNotFoundException("Classe non trouvée avec l'ID: " + classeId));

        try {
            // Lire le fichier Excel
            List<EtudiantCreateDTO> etudiants = excelEtudiantReader.readEtudiants(file);
            response.setTotalImported(etudiants.size());

            int successCount = 0;
            int failureCount = 0;
            int lineNumber = 2; // Commence à 2 car la ligne 1 est l'en-tête

            // Créer chaque étudiant
            for (EtudiantCreateDTO dto : etudiants) {
                try {
                    // Vérifier l'unicité de l'email
                    if (utilisateurRepository.existsByEmail(dto.getEmail())) {
                        failureCount++;
                        response.getErrors().add(
                                EtudiantImportResponseDTO.ImportError.builder()
                                        .lineNumber(lineNumber)
                                        .email(dto.getEmail())
                                        .errorMessage("Un utilisateur avec cet email existe déjà: " + dto.getEmail())
                                        .build()
                        );
                        log.warn("❌ Ligne {}: Email déjà existant: {}", lineNumber, dto.getEmail());
                        lineNumber++;
                        continue;
                    }

                    // Vérifier l'unicité du matricule
                    if (dto.getMatricule() != null && etudiantRepository.existsByMatricule(dto.getMatricule())) {
                        failureCount++;
                        response.getErrors().add(
                                EtudiantImportResponseDTO.ImportError.builder()
                                        .lineNumber(lineNumber)
                                        .email(dto.getEmail())
                                        .errorMessage("Un étudiant avec ce matricule existe déjà: " + dto.getMatricule())
                                        .build()
                        );
                        log.warn("❌ Ligne {}: Matricule déjà existant: {}", lineNumber, dto.getMatricule());
                        lineNumber++;
                        continue;
                    }


                    // Assigner la classe fournie à l'ensemble du lot
                    dto.setClasseId(classeId);

                    // Créer l'étudiant
                    EtudiantDTO created = createEtudiant(dto);
                    response.getCreatedEtudiants().add(created);
                    successCount++;
                    log.info("✅ Ligne {}: Étudiant créé avec succès - {} {} (ID: {})",
                            lineNumber, created.getNom(), created.getPrenom(), created.getId());

                } catch (Exception e) {
                    failureCount++;
                    response.getErrors().add(
                            EtudiantImportResponseDTO.ImportError.builder()
                                    .lineNumber(lineNumber)
                                    .email(dto.getEmail())
                                    .errorMessage("Erreur lors de la création de l'étudiant: " + e.getMessage())
                                    .build()
                    );
                    log.error("❌ Ligne {}: Erreur lors de la création de l'étudiant {}: {}",
                            lineNumber, dto.getEmail(), e.getMessage());
                } finally {
                    lineNumber++;
                }
            }

            // Construire le message de réponse
            response.setSuccessCount(successCount);
            response.setFailureCount(failureCount);

            String message = String.format(
                    "Import terminé: %d étudiant(s) créé(s) avec succès, %d erreur(s)",
                    successCount, failureCount
            );
            response.setMessage(message);

            log.info("📊 {}", message);
            return response;

        } catch (Exception e) {
            log.error("❌ Erreur critique lors de l'import: {}", e.getMessage());
            response.setMessage("Erreur lors de l'import du fichier Excel: " + e.getMessage());
            response.setFailureCount(response.getTotalImported());
            response.getErrors().add(
                    EtudiantImportResponseDTO.ImportError.builder()
                            .lineNumber(0)
                            .email("")
                            .errorMessage("Erreur critique: " + e.getMessage())
                            .build()
            );
            return response;
        }
    }

    @Override
    public byte[] generateEtudiantTemplate() throws IOException {
        log.info("Génération du fichier Excel modèle pour l'import d'étudiants");
        return excelTemplateGenerator.generateTemplate();
    }

}
