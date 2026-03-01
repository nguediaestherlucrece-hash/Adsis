package com.ISI.Appel_classe.Gestion_des_utilisateurs.controller.api;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.EtudiantCreateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.EtudiantUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response.EtudiantDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.service.serviceInterface.EtudiantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST pour la gestion des étudiants
 * Expose des endpoints pour CRUD complet avec tous les attributs (Utilisateur +
 * Etudiant)
 */
@Slf4j
@RestController
@RequestMapping("/api/etudiants")
@RequiredArgsConstructor
public class EtudiantController {

    private final EtudiantService etudiantService;

    /**
     * GET /api/etudiants
     * Récupère la liste de tous les étudiants
     * Retourne TOUS les attributs (hérités + spécifiques)
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ETUDIANT_READ')")
    public ResponseEntity<List<EtudiantDTO>> getAllEtudiants() {
        log.info("📖 [GET /api/etudiants] Récupération de tous les étudiants");
        List<EtudiantDTO> etudiants = etudiantService.getAllEtudiants();
        log.info("✅ {} étudiant(s) récupéré(s)", etudiants.size());
        return ResponseEntity.ok(etudiants);
    }

    /**
     * GET /api/etudiants/{id}
     * Récupère un étudiant par son ID
     * Retourne TOUS les attributs incluant les relations
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ETUDIANT_READ')")
    public ResponseEntity<EtudiantDTO> getEtudiantById(@PathVariable Long id) {
        log.info("📖 [GET /api/etudiants/{}] Récupération de l'étudiant", id);
        EtudiantDTO etudiant = etudiantService.getEtudiantById(id);
        log.info("✅ Étudiant récupéré: {} {}", etudiant.getNom(), etudiant.getPrenom());
        return ResponseEntity.ok(etudiant);
    }

    /**
     * GET /api/etudiants/matricule/{matricule}
     * Récupère un étudiant par son matricule
     */
    @GetMapping("/matricule/{matricule}")
    @PreAuthorize("hasAuthority('ETUDIANT_READ')")
    public ResponseEntity<EtudiantDTO> getEtudiantByMatricule(@PathVariable String matricule) {
        log.info("📖 [GET /api/etudiants/matricule/{}] Récupération par matricule", matricule);
        EtudiantDTO etudiant = etudiantService.getEtudiantByMatricule(matricule);
        log.info("✅ Étudiant trouvé: {} {}", etudiant.getNom(), etudiant.getPrenom());
        return ResponseEntity.ok(etudiant);
    }

    /**
     * GET /api/etudiants/email/{email}
     * Récupère un étudiant par son email
     */
    @GetMapping("/email/{email}")
    @PreAuthorize("hasAuthority('ETUDIANT_READ')")
    public ResponseEntity<EtudiantDTO> getEtudiantByEmail(@PathVariable String email) {
        log.info("📖 [GET /api/etudiants/email/{}] Récupération par email", email);
        EtudiantDTO etudiant = etudiantService.getEtudiantByEmail(email);
        log.info("✅ Étudiant trouvé: {} {}", etudiant.getNom(), etudiant.getPrenom());
        return ResponseEntity.ok(etudiant);
    }

    /**
     * GET /api/etudiants/classe/{classeId}
     * Récupère tous les étudiants d'une classe
     */
    @GetMapping("/classe/{classeId}")
    @PreAuthorize("hasAuthority('ETUDIANT_READ')")
    public ResponseEntity<List<EtudiantDTO>> getEtudiantsByClasse(@PathVariable Long classeId) {
        log.info("📖 [GET /api/etudiants/classe/{}] Récupération des étudiants de la classe", classeId);
        List<EtudiantDTO> etudiants = etudiantService.getEtudiantsByClasse(classeId);
        log.info("✅ {} étudiant(s) trouvé(s) dans la classe", etudiants.size());
        return ResponseEntity.ok(etudiants);
    }

    /**
     * POST /api/etudiants
     * Crée un nouvel étudiant avec TOUS ses attributs
     * Body doit contenir:
     * - Attributs de Utilisateur: nom, prenom, email, password, statut, roleNames
     * - Attributs spécifiques: matricule, dateNaissance, telephone
     * - Relations: classeId
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ETUDIANT_CREATE')")
    public ResponseEntity<EtudiantDTO> createEtudiant(@RequestBody EtudiantCreateDTO dto) {
        log.info("➕ [POST /api/etudiants] Création d'un nouvel étudiant: {}", dto.getEmail());
        log.debug("Données reçues: nom={}, prenom={}, matricule={}, classeId={}",
                dto.getNom(), dto.getPrenom(), dto.getMatricule(), dto.getClasseId());

        EtudiantDTO created = etudiantService.createEtudiant(dto);

        log.info("✅ Étudiant créé avec succès - ID: {}, Matricule: {}",
                created.getId(), created.getMatricule());

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/etudiants/{id}
     * Met à jour un étudiant existant
     * Permet de modifier TOUS les attributs modifiables (hérités + spécifiques)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ETUDIANT_UPDATE')")
    public ResponseEntity<EtudiantDTO> updateEtudiant(
            @PathVariable Long id,
            @RequestBody EtudiantUpdateDTO dto) {
        log.info("✏️ [PUT /api/etudiants/{}] Mise à jour de l'étudiant", id);
        log.debug("Nouvelles données: nom={}, prenom={}, email={}, matricule={}, classeId={}",
                dto.getNom(), dto.getPrenom(), dto.getEmail(), dto.getMatricule(), dto.getClasseId());

        EtudiantDTO updated = etudiantService.updateEtudiant(id, dto);

        log.info("✅ Étudiant mis à jour avec succès");
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /api/etudiants/{id}
     * Supprime un étudiant
     * Supprime l'enregistrement dans les tables etudiant ET utilisateur (cascade)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ETUDIANT_DELETE')")
    public ResponseEntity<Void> deleteEtudiant(@PathVariable Long id) {
        log.info("🗑️ [DELETE /api/etudiants/{}] Suppression de l'étudiant", id);

        etudiantService.deleteEtudiant(id);

        log.info("✅ Étudiant supprimé avec succès");
        return ResponseEntity.noContent().build();
    }

    /**
     * PATCH /api/etudiants/{etudiantId}/classe/{classeId}
     * Assigne ou change la classe d'un étudiant
     */
    @PatchMapping("/{etudiantId}/classe/{classeId}")
    @PreAuthorize("hasAuthority('ETUDIANT_UPDATE')")
    public ResponseEntity<EtudiantDTO> assignerClasse(
            @PathVariable Long etudiantId,
            @PathVariable Long classeId) {
        log.info("🔄 [PATCH /api/etudiants/{}/classe/{}] Assignation de classe", etudiantId, classeId);

        EtudiantDTO updated = etudiantService.assignerClasse(etudiantId, classeId);

        log.info("✅ Étudiant assigné à la classe avec succès");
        return ResponseEntity.ok(updated);
    }
}
