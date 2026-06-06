package com.ISI.Appel_classe.Gestion_des_appels_etudiant.controller.api;

import com.ISI.Appel_classe.Gestion_des_appels_etudiant.dto.request.GenererCodesPresenceRequest;
import com.ISI.Appel_classe.Gestion_des_appels_etudiant.dto.request.MarquerPresentParCodePINRequest;
import com.ISI.Appel_classe.Gestion_des_appels_etudiant.dto.request.MarquerPresentParQRCodeRequest;
import com.ISI.Appel_classe.Gestion_des_appels_etudiant.dto.response.CodePresenceDTO;
import com.ISI.Appel_classe.Gestion_des_appels_etudiant.dto.response.PresenceEtudiantDTO;
import com.ISI.Appel_classe.Gestion_des_appels_etudiant.dto.response.StatutSeanceEtudiantDTO;
import com.ISI.Appel_classe.Gestion_des_appels_etudiant.service.serviceInterface.AppelEtudiantService;
import com.ISI.Appel_classe.Gestion_des_etudiants.repository.EtudiantRepository;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/appels-etudiant")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AppelEtudiantController {

    private final AppelEtudiantService appelEtudiantService;
    // ✅ Injecté pour corriger le bug extractEtudiantIdFromAuthentication
    private final EtudiantRepository etudiantRepository;

    /**
     * Génère un code PIN et/ou QR code pour permettre à l'enseignant de marquer l'étudiant présent
     * POST /api/appels-etudiant/generer-codes
     */
    @PostMapping("/generer-codes")
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<CodePresenceDTO> genererCodesPresence(
            @Valid @RequestBody GenererCodesPresenceRequest request,
            Authentication authentication) {
        Long etudiantId = extractEtudiantIdFromAuthentication(authentication);
        CodePresenceDTO codes = appelEtudiantService.genererCodesPresence(etudiantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(codes);
    }

    /**
     * Marque l'étudiant présent en utilisant un code PIN généré par l'enseignant
     * POST /api/appels-etudiant/marquer-present-pin
     */
    @PostMapping("/marquer-present-pin")
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<PresenceEtudiantDTO> marquerPresentParCodePIN(
            @Valid @RequestBody MarquerPresentParCodePINRequest request,
            Authentication authentication) {
        Long etudiantId = extractEtudiantIdFromAuthentication(authentication);
        PresenceEtudiantDTO presence = appelEtudiantService.marquerPresentParCodePIN(etudiantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(presence);
    }

    /**
     * Marque l'étudiant présent en utilisant un QR code généré par l'enseignant
     * POST /api/appels-etudiant/marquer-present-qr
     */
    @PostMapping("/marquer-present-qr")
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<PresenceEtudiantDTO> marquerPresentParQRCode(
            @Valid @RequestBody MarquerPresentParQRCodeRequest request,
            Authentication authentication) {
        Long etudiantId = extractEtudiantIdFromAuthentication(authentication);
        PresenceEtudiantDTO presence = appelEtudiantService.marquerPresentParQRCode(etudiantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(presence);
    }

    /**
     * Récupère les codes présence actifs de l'étudiant pour une séance
     * GET /api/appels-etudiant/codes-actifs/{seanceId}
     */
    @GetMapping("/codes-actifs/{seanceId}")
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<CodePresenceDTO> getCodesPresenceActifs(
            @PathVariable Long seanceId,
            Authentication authentication) {
        Long etudiantId = extractEtudiantIdFromAuthentication(authentication);
        CodePresenceDTO codes = appelEtudiantService.getCodesPresenceActifs(etudiantId, seanceId);
        return ResponseEntity.ok(codes);
    }

    /**
     * Valide un code PIN généré par l'étudiant (côté enseignant)
     * POST /api/appels-etudiant/appel/{appelId}/etudiant/{etudiantId}/valider-pin
     */
    @PostMapping("/appel/{appelId}/etudiant/{etudiantId}/valider-pin")
    @PreAuthorize("hasAnyRole('ENSEIGNANT', 'SURVEILLANT', 'ASSISTANT_PEDAGOGIQUE')")
    public ResponseEntity<PresenceEtudiantDTO> validerCodePINEtudiant(
            @PathVariable Long appelId,
            @PathVariable Long etudiantId,
            @RequestBody java.util.Map<String, String> request) {
        String codePIN = request.get("codePIN");
        PresenceEtudiantDTO presence = appelEtudiantService.validerCodePINEtudiant(appelId, etudiantId, codePIN);
        return ResponseEntity.status(HttpStatus.CREATED).body(presence);
    }

    /**
     * Valide un QR code généré par l'étudiant (côté enseignant)
     * POST /api/appels-etudiant/appel/{appelId}/etudiant/{etudiantId}/valider-qr
     */
    @PostMapping("/appel/{appelId}/etudiant/{etudiantId}/valider-qr")
    @PreAuthorize("hasAnyRole('ENSEIGNANT', 'SURVEILLANT', 'ASSISTANT_PEDAGOGIQUE')")
    public ResponseEntity<PresenceEtudiantDTO> validerQRCodeEtudiant(
            @PathVariable Long appelId,
            @PathVariable Long etudiantId,
            @RequestBody java.util.Map<String, String> request) {
        String qrToken = request.get("qrToken");
        PresenceEtudiantDTO presence = appelEtudiantService.validerQRCodeEtudiant(appelId, etudiantId, qrToken);
        return ResponseEntity.status(HttpStatus.CREATED).body(presence);
    }

    /**
     * Retourne le statut de l’appel pour l’étudiant authentifié sur une séance :
     * - appelOuvert : y a-t-il un appel en cours ?
     * - dejaPresent : a-t-il répondu à CET appel (pas les précédents) ?
     * - messageStatut: message lisible pour l’affichage ("✅ Présent · 08h25", "⚠️ En retard…")
     * GET /api/appels-etudiant/statut-seance/{seanceId}
     */
    @GetMapping("/statut-seance/{seanceId}")
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<StatutSeanceEtudiantDTO> getStatutSeancePourEtudiant(
            @PathVariable Long seanceId,
            Authentication authentication) {
        Long etudiantId = extractEtudiantIdFromAuthentication(authentication);
        StatutSeanceEtudiantDTO statut = appelEtudiantService
                .getStatutSeancePourEtudiant(seanceId, etudiantId);
        return ResponseEntity.ok(statut);
    }

    /**
     * ✅ CORRECTION BUG CRITIQUE :
     * Avant : Long.parseLong(authentication.getName()) → NumberFormatException car getName() = email
     * Après : on recherche l’Etudiant en base par email pour obtenir son ID Long
     */
    private Long extractEtudiantIdFromAuthentication(Authentication authentication) {
        String email = authentication.getName(); // getName() retourne l’email (username de UserDetails)
        return etudiantRepository.findByEmail(email)
                .map(etudiant -> etudiant.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Étudiant introuvable pour l’email : " + email));
    }
}
