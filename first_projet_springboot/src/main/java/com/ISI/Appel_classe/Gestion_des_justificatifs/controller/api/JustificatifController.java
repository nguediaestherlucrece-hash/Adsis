package com.ISI.Appel_classe.Gestion_des_justificatifs.controller.api;

import com.ISI.Appel_classe.Gestion_des_justificatifs.dto.request.AddComplementRequest;
import com.ISI.Appel_classe.Gestion_des_justificatifs.dto.request.CreateJustificatifRequest;
import com.ISI.Appel_classe.Gestion_des_justificatifs.dto.request.FilterJustificatifRequest;
import com.ISI.Appel_classe.Gestion_des_justificatifs.dto.request.UpdateJustificatifRequest;
import com.ISI.Appel_classe.Gestion_des_justificatifs.dto.response.JustificatifListResponse;
import com.ISI.Appel_classe.Gestion_des_justificatifs.dto.response.JustificatifResponse;
import com.ISI.Appel_classe.Gestion_des_justificatifs.service.serviceInterface.JustificatifService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/justificatifs")
@CrossOrigin(origins = "*")
public class JustificatifController {

    @Autowired
    private JustificatifService justificatifService;

    // ============ ENDPOINTS POUR LES ÉTUDIANTS ============

    /**
     * Soumettre un justificatif pour une absence/retard
     * POST /api/justificatifs/submit
     */
    @PostMapping("/submit")
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<?> submitJustificatif(
            @ModelAttribute CreateJustificatifRequest request,
            @RequestParam Long etudiantId) {
        try {
            JustificatifResponse response = justificatifService.createJustificatif(request, etudiantId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erreur: " + e.getMessage());
        }
    }

    /**
     * Obtenir l'historique de mes justificatifs
     * GET /api/justificatifs/my-justificatifs
     */
    @GetMapping("/my-justificatifs")
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<JustificatifListResponse> getMyJustificatifs(
            @RequestParam Long etudiantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {

        FilterJustificatifRequest filter = new FilterJustificatifRequest();
        filter.setPage(page);
        filter.setSize(size);
        filter.setDateDebutSoumission(dateDebut);
        filter.setDateFinSoumission(dateFin);

        JustificatifListResponse response = justificatifService.getMyJustificatifs(etudiantId, filter);
        return ResponseEntity.ok(response);
    }

    /**
     * Ajouter un complément à un justificatif rejeté
     * POST /api/justificatifs/{id}/complement
     */
    @PostMapping("/{id}/complement")
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<?> addComplement(
            @PathVariable Long id,
            @ModelAttribute AddComplementRequest request,
            @RequestParam Long etudiantId) {
        try {
            JustificatifResponse response = justificatifService.addComplement(id, request, etudiantId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erreur: " + e.getMessage());
        }
    }

    /**
     * Supprimer un justificatif en attente
     * DELETE /api/justificatifs/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<?> deleteJustificatif(
            @PathVariable Long id,
            @RequestParam Long etudiantId) {
        try {
            justificatifService.deleteJustificatif(id, etudiantId);
            return ResponseEntity.ok("Justificatif supprimé avec succès");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erreur: " + e.getMessage());
        }
    }

    /**
     * Obtenir les détails d'un justificatif
     * GET /api/justificatifs/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ETUDIANT') or hasRole('ASSISTANT_PEDAGOGIQUE')")
    public ResponseEntity<JustificatifResponse> getJustificatifById(@PathVariable Long id) {
        try {
            JustificatifResponse response = justificatifService.getJustificatifById(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    // ============ ENDPOINTS POUR LES ASSISTANTS PÉDAGOGIQUES ============

    /**
     * Obtenir tous les justificatifs avec filtres (pour l'assistant)
     * GET /api/justificatifs/assistant/all
     */
    @GetMapping("/assistant/all")
    @PreAuthorize("hasRole('ASSISTANT_PEDAGOGIQUE')")
    public ResponseEntity<JustificatifListResponse> getAllJustificatifs(
            @RequestParam Long assistantId,
            @RequestParam(required = false) Long etudiantId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateSoumissionDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateSoumissionFin,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateValidationDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateValidationFin,
            @RequestParam(required = false) String statut,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        FilterJustificatifRequest filter = new FilterJustificatifRequest();
        filter.setEtudiantId(etudiantId);
        filter.setDateDebutSoumission(dateSoumissionDebut);
        filter.setDateFinSoumission(dateSoumissionFin);
        filter.setDateDebutValidation(dateValidationDebut);
        filter.setDateFinValidation(dateValidationFin);
        if (statut != null && !statut.isEmpty()) {
            filter.setStatutValidation(com.ISI.Appel_classe.Gestion_des_justificatifs.enums.StatutValidation.valueOf(statut));
        }
        filter.setPage(page);
        filter.setSize(size);

        JustificatifListResponse response = justificatifService.getAllJustificatifs(assistantId, filter);
        return ResponseEntity.ok(response);
    }

    /**
     * Valider ou rejeter un justificatif
     * PUT /api/justificatifs/{id}/validate
     */
    @PutMapping("/{id}/validate")
    @PreAuthorize("hasRole('ASSISTANT_PEDAGOGIQUE')")
    public ResponseEntity<?> validateJustificatif(
            @PathVariable Long id,
            @RequestBody UpdateJustificatifRequest request,
            @RequestParam Long assistantId) {
        try {
            JustificatifResponse response = justificatifService.validateJustificatif(id, request, assistantId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erreur: " + e.getMessage());
        }
    }
}
