package com.ISI.Appel_classe.Gestion_des_etudiants.controller.api;

import com.ISI.Appel_classe.Gestion_des_etudiants.dto.request.EtudiantCreateDTO;
import com.ISI.Appel_classe.Gestion_des_etudiants.dto.request.EtudiantUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_etudiants.dto.request.MigrationRequestDTO;
import com.ISI.Appel_classe.Gestion_des_etudiants.dto.response.EtudiantDTO;
import com.ISI.Appel_classe.Gestion_des_etudiants.dto.response.EtudiantImportResponseDTO;
import com.ISI.Appel_classe.Gestion_des_etudiants.dto.response.HistoriqueClasseDTO;
import com.ISI.Appel_classe.Gestion_des_etudiants.service.serviceInterface.EtudiantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    @PreAuthorize("hasRole('ENSEIGNANT') or hasRole('ASSISTANT_PEDAGOGIQUE') or hasRole('SURVEILLANT')")
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
    @PreAuthorize("hasRole('ENSEIGNANT') or hasRole('ASSISTANT_PEDAGOGIQUE') or hasRole('SURVEILLANT')")
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
    @PreAuthorize("hasRole('ENSEIGNANT') or hasRole('ASSISTANT_PEDAGOGIQUE') or hasRole('SURVEILLANT')")
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
    @PreAuthorize("hasRole('ENSEIGNANT') or hasRole('ASSISTANT_PEDAGOGIQUE') or hasRole('SURVEILLANT')")
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
    @PreAuthorize("hasRole('ENSEIGNANT') or hasRole('ASSISTANT_PEDAGOGIQUE') or hasRole('SURVEILLANT')")
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
    @PreAuthorize("hasRole('ASSISTANT_PEDAGOGIQUE')")
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
    @PreAuthorize("hasRole('ASSISTANT_PEDAGOGIQUE') or hasRole('ETUDIANT')")
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
    @PreAuthorize("hasRole('ASSISTANT_PEDAGOGIQUE')")
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
    @PreAuthorize("hasRole('ASSISTANT_PEDAGOGIQUE')")
    public ResponseEntity<EtudiantDTO> assignerClasse(
            @PathVariable Long etudiantId,
            @PathVariable Long classeId) {
        log.info("🔄 [PATCH /api/etudiants/{}/classe/{}] Assignation de classe", etudiantId, classeId);

        EtudiantDTO updated = etudiantService.assignerClasse(etudiantId, classeId);

        log.info("✅ Étudiant assigné à la classe avec succès");
        return ResponseEntity.ok(updated);
    }

    /**
     * PATCH /api/etudiants/{id}/toggle-statut
     * Bascule le statut d'un étudiant entre ACTIF et INACTIF
     */
    @PatchMapping("/{id}/toggle-statut")
    @PreAuthorize("hasRole('ASSISTANT_PEDAGOGIQUE')")
    public ResponseEntity<EtudiantDTO> toggleStatut(@PathVariable Long id) {
        log.info("🔄 [PATCH /api/etudiants/{}/toggle-statut] Basculement du statut", id);

        EtudiantDTO updated = etudiantService.toggleStatut(id);

        log.info("✅ Statut de l'étudiant basculé: {}", updated.getStatut());
        return ResponseEntity.ok(updated);
    }
    /**
     * POST /api/etudiants/import/excel
     * Importe une liste d'étudiants depuis un fichier Excel
     * Accepte les fichiers .xlsx et .xls
     *
     * Format du fichier Excel attendu (en-têtes dans la première ligne):
     * - Colonne A: Nom (obligatoire)
     * - Colonne B: Prénom (obligatoire)
     * - Colonne C: Email (obligatoire)
     * - Colonne D: Matricule (obligatoire)
     * - Colonne E: Date Naissance (optionnel) - Format: YYYY-MM-DD
     * - Colonne F: Téléphone (optionnel)
     *
     * @param file Le fichier Excel à importer
     * @param classeId L'ID de la classe à associer à tous les étudiants importés
     * @return Un DTO contenant les résultats de l'import (succès, échecs, etc.)
     */


    @PostMapping("/import/excel")
    @PreAuthorize("hasRole('ASSISTANT_PEDAGOGIQUE')")
    public ResponseEntity<EtudiantImportResponseDTO> importEtudiantsFromExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam("classeId") Long classeId) {
        log.info("📥 [POST /api/etudiants/import/excel] Import d'étudiants depuis Excel: {} vers classe {}",
                file.getOriginalFilename(), classeId);

        EtudiantImportResponseDTO response = etudiantService.importEtudiantsFromExcel(file, classeId);

        log.info("📊 Import terminé: {} succès, {} erreurs",
                response.getSuccessCount(), response.getFailureCount());

        return ResponseEntity.ok(response);
    }


    /**
     * GET /api/etudiants/template/excel
     * Télécharge un fichier Excel modèle pour l'import d'étudiants
     * Le fichier contient les en-têtes et des lignes d'exemple
     *
     * Format du fichier Excel (en-têtes dans la première ligne):
     * - Colonne A: Nom (obligatoire)
     * - Colonne B: Prénom (obligatoire)
     * - Colonne C: Email (obligatoire)
     * - Colonne E: Matricule (obligatoire)
     * - Colonne F: Date Naissance (optionnel) - Format: YYYY-MM-DD
     * - Colonne G: Téléphone (optionnel)
     *
     * @return Un fichier Excel modèle prêt à être rempli
     */
    @GetMapping("/template/excel")
    @PreAuthorize("hasRole('ASSISTANT_PEDAGOGIQUE')")
    public ResponseEntity<byte[]> downloadExcelTemplate() {
        log.info("📋 [GET /api/etudiants/template/excel] Téléchargement du fichier Excel modèle");

        try {
            byte[] excelFile = etudiantService.generateEtudiantTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "modele_etudiants.xls");
            headers.setContentLength(excelFile.length);

            log.info("✅ Fichier Excel modèle généré avec succès (taille: {} bytes)", excelFile.length);
            return new ResponseEntity<>(excelFile, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("❌ Erreur lors de la génération du fichier Excel modèle: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========================================
    // MIGRATION & HISTORIQUE
    // ========================================

    @PostMapping("/migrer")
    @PreAuthorize("hasRole('ASSISTANT_PEDAGOGIQUE')")
    public ResponseEntity<List<EtudiantDTO>> migrerEtudiants(@RequestBody MigrationRequestDTO dto) {
        log.info("POST /api/etudiants/migrer - Migration de {} étudiant(s) vers classe {}",
                dto.getEtudiantIds().size(), dto.getClasseDestinationId());
        List<EtudiantDTO> result = etudiantService.migrerEtudiants(dto);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/historique")
    @PreAuthorize("hasRole('ASSISTANT_PEDAGOGIQUE')")
    public ResponseEntity<List<HistoriqueClasseDTO>> getHistorique(@PathVariable Long id) {
        log.info("GET /api/etudiants/{}/historique", id);
        List<HistoriqueClasseDTO> historique = etudiantService.getHistorique(id);
        return ResponseEntity.ok(historique);
    }
}
