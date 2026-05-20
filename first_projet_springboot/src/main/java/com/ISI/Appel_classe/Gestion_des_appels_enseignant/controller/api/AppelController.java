package com.ISI.Appel_classe.Gestion_des_appels_enseignant.controller.api;

import com.ISI.Appel_classe.Gestion_des_appels_enseignant.dto.request.DemarrerAppelRequest;
import com.ISI.Appel_classe.Gestion_des_appels_enseignant.dto.request.EnregistrerPresenceRequest;
import com.ISI.Appel_classe.Gestion_des_appels_enseignant.dto.request.EnregistrerPresencesBatchRequest;
import com.ISI.Appel_classe.Gestion_des_appels_enseignant.dto.response.AppelDTO;
import com.ISI.Appel_classe.Gestion_des_appels_enseignant.dto.response.EtudiantSeanceDTO;
import com.ISI.Appel_classe.Gestion_des_appels_enseignant.dto.response.PresenceDTO;
import com.ISI.Appel_classe.Gestion_des_appels_enseignant.service.serviceInterface.AppelService;
import com.ISI.Appel_classe.Gestion_des_etudiants.repository.EtudiantRepository;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appels")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AppelController {
    
    private final AppelService appelService;
    private final EtudiantRepository etudiantRepository;
    
    /**
     * Démarre un nouvel appel pour une séance
     * POST /api/appels
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ENSEIGNANT', 'SURVEILLANT', 'ASSISTANT_PEDAGOGIQUE')")
    public ResponseEntity<AppelDTO> demarrerAppel(@Valid @RequestBody DemarrerAppelRequest request) {
        AppelDTO appel = appelService.demarrerAppel(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(appel);
    }
    
    /**
     * Clôture un appel existant
     * PUT /api/appels/{id}/cloturer
     */
    @PutMapping("/{id}/cloturer")
    @PreAuthorize("hasAnyRole('ENSEIGNANT', 'SURVEILLANT', 'ASSISTANT_PEDAGOGIQUE')")
    public ResponseEntity<AppelDTO> cloturerAppel(@PathVariable Long id) {
        AppelDTO appel = appelService.cloturerAppel(id);
        return ResponseEntity.ok(appel);
    }
    
    /**
     * Enregistre une présence unique
     * POST /api/appels/{id}/presences
     */
    @PostMapping("/{id}/presences")
    @PreAuthorize("hasAnyRole('ENSEIGNANT', 'SURVEILLANT', 'ASSISTANT_PEDAGOGIQUE')")
    public ResponseEntity<PresenceDTO> enregistrerPresence(
            @PathVariable Long id,
            @Valid @RequestBody EnregistrerPresenceRequest request) {
        PresenceDTO presence = appelService.enregistrerPresence(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(presence);
    }
    
    /**
     * Enregistre plusieurs présences en batch (pour synchronisation PWA)
     * POST /api/appels/presences/batch
     */
    @PostMapping("/presences/batch")
    @PreAuthorize("hasAnyRole('ENSEIGNANT', 'SURVEILLANT', 'ASSISTANT_PEDAGOGIQUE')")
    public ResponseEntity<List<PresenceDTO>> enregistrerPresencesBatch(
            @Valid @RequestBody EnregistrerPresencesBatchRequest request) {
        List<PresenceDTO> presences = appelService.enregistrerPresencesBatch(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(presences);
    }
    
    /**
     * Récupère un appel par son ID
     * GET /api/appels/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ENSEIGNANT', 'SURVEILLANT', 'ASSISTANT_PEDAGOGIQUE', 'ADMIN')")
    public ResponseEntity<AppelDTO> getAppelById(@PathVariable Long id) {
        AppelDTO appel = appelService.getAppelById(id);
        return ResponseEntity.ok(appel);
    }
    
    /**
     * Récupère l'appel ouvert pour une séance
     * GET /api/appels/seance/{seanceId}/ouvert
     */
    @GetMapping("/seance/{seanceId}/ouvert")
    @PreAuthorize("hasAnyRole('ENSEIGNANT', 'SURVEILLANT', 'ASSISTANT_PEDAGOGIQUE', 'ETUDIANT')")
    public ResponseEntity<AppelDTO> getAppelOuvertBySeance(@PathVariable Long seanceId) {
        AppelDTO appel = appelService.getAppelOuvertBySeance(seanceId);
        if (appel == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(appel);
    }
    
    /**
     * Récupère la liste des étudiants d'une séance avec leur statut
     * GET /api/appels/seance/{seanceId}/etudiants
     */
    @GetMapping("/seance/{seanceId}/etudiants")
    @PreAuthorize("hasAnyRole('ENSEIGNANT', 'SURVEILLANT', 'ASSISTANT_PEDAGOGIQUE')")
    public ResponseEntity<List<EtudiantSeanceDTO>> getEtudiantsSeance(
            @PathVariable Long seanceId,
            @RequestParam(required = false) Long appelId) {
        List<EtudiantSeanceDTO> etudiants = appelService.getEtudiantsSeanceAvecStatut(seanceId, appelId);
        return ResponseEntity.ok(etudiants);
    }
    
    /**
     * Valide un code PIN pour un appel
     * POST /api/appels/{id}/valider-pin
     */
    @PostMapping("/{id}/valider-pin")
    @PreAuthorize("hasAnyRole('ENSEIGNANT', 'SURVEILLANT', 'ETUDIANT')")
    public ResponseEntity<Map<String, Boolean>> validerCodePIN(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String codePIN = request.get("codePIN");
        boolean valide = appelService.validerCodePIN(id, codePIN);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("valide", valide);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Régénère un QR code pour un appel
     * POST /api/appels/{id}/regenerer-qr
     */
    @PostMapping("/{id}/regenerer-qr")
    @PreAuthorize("hasAnyRole('ENSEIGNANT', 'SURVEILLANT', 'ASSISTANT_PEDAGOGIQUE')")
    public ResponseEntity<AppelDTO> regenererQRCode(@PathVariable Long id) {
        AppelDTO appel = appelService.regenererQRCode(id);
        return ResponseEntity.ok(appel);
    }

    /**
     * Génère un code PIN pour un appel existant
     * POST /api/appels/{id}/generer-pin
     */
    @PostMapping("/{id}/generer-pin")
    @PreAuthorize("hasAnyRole('ENSEIGNANT', 'SURVEILLANT', 'ASSISTANT_PEDAGOGIQUE')")
    public ResponseEntity<AppelDTO> genererPIN(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Integer> request) {
        Integer duree = (request != null) ? request.get("dureeMinutes") : null;
        AppelDTO appel = appelService.genererPINPourAppel(id, duree);
        return ResponseEntity.ok(appel);
    }

    /**
     * Récupère les présences d'un appel
     * GET /api/appels/{id}/presences
     */
    @GetMapping("/{id}/presences")
    @PreAuthorize("hasAnyRole('ENSEIGNANT', 'SURVEILLANT', 'ASSISTANT_PEDAGOGIQUE', 'ADMIN')")
    public ResponseEntity<List<PresenceDTO>> getPresences(@PathVariable Long id) {
        List<PresenceDTO> presences = appelService.getPresencesByAppelId(id);
        return ResponseEntity.ok(presences);
    }

    /**
     * Valide un token QR code pour un appel
     * POST /api/appels/{id}/valider-qr
     */
    @PostMapping("/{id}/valider-qr")
    @PreAuthorize("hasAnyRole('ENSEIGNANT', 'SURVEILLANT', 'ASSISTANT_PEDAGOGIQUE')")
    public ResponseEntity<AppelDTO> validerQR(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String qrToken = request.get("qrToken");
        AppelDTO appel = appelService.validerQRCode(id, qrToken);
        return ResponseEntity.ok(appel);
    }

    /**
     * Valide un code PIN généré par un étudiant et marque l'étudiant présent
     * POST /api/appels/{id}/valider-pin-etudiant
     */
    @PostMapping("/{id}/valider-pin-etudiant")
    @PreAuthorize("hasAnyRole('ENSEIGNANT', 'SURVEILLANT', 'ASSISTANT_PEDAGOGIQUE')")
    public ResponseEntity<PresenceDTO> validerPINEtudiant(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String codePIN = request.get("codePIN");
        PresenceDTO presence = appelService.validerCodePINEtudiant(id, codePIN);
        return ResponseEntity.status(HttpStatus.CREATED).body(presence);
    }

    /**
     * Valide un QR code généré par un étudiant et marque l'étudiant présent
     * POST /api/appels/{id}/valider-qr-etudiant
     */
    @PostMapping("/{id}/valider-qr-etudiant")
    @PreAuthorize("hasAnyRole('ENSEIGNANT', 'SURVEILLANT', 'ASSISTANT_PEDAGOGIQUE')")
    public ResponseEntity<PresenceDTO> validerQRCodeEtudiant(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String qrToken = request.get("qrToken");
        PresenceDTO presence = appelService.validerQRCodeEtudiant(id, qrToken);
        return ResponseEntity.status(HttpStatus.CREATED).body(presence);
    }

    // =====================================================================
    // ENDPOINTS DÉDIÉS AUX ÉTUDIANTS : validation PIN/QR de l'enseignant
    // =====================================================================

    /**
     * L'étudiant saisit le PIN généré par l'enseignant pour se marquer présent.
     * POST /api/appels/seance/{seanceId}/etudiant/valider-pin
     */
    @PostMapping("/seance/{seanceId}/etudiant/valider-pin")
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<?> marquerPresentViaPin(
            @PathVariable Long seanceId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            // 1. Récupérer l'appel ouvert pour cette séance
            AppelDTO appel = appelService.getAppelOuvertBySeance(seanceId);
            if (appel == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Aucun appel ouvert pour cette séance"));
            }

            // 2. Valider le PIN de l'enseignant (stocké dans pinCache)
            String codePIN = request.get("codePIN");
            if (codePIN == null || codePIN.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Code PIN manquant"));
            }
            boolean pinValide = appelService.validerCodePIN(appel.getId(), codePIN);
            if (!pinValide) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Code PIN invalide ou expiré"));
            }

            // 3. Identifier l'étudiant authentifié
            String email = authentication.getName();
            Long etudiantId = etudiantRepository.findByEmail(email)
                    .map(e -> e.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Étudiant non trouvé pour l'email: " + email));

            // 4. Enregistrer la présence
            EnregistrerPresenceRequest presenceReq = new EnregistrerPresenceRequest();
            presenceReq.setEtudiantId(etudiantId);
            presenceReq.setStatut("présent");
            presenceReq.setModeEnregistrement("PIN");
            presenceReq.setRetard(false);

            PresenceDTO presence = appelService.enregistrerPresence(appel.getId(), presenceReq);
            return ResponseEntity.status(HttpStatus.CREATED).body(presence);

        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * L'étudiant scanne le QR code de l'enseignant pour se marquer présent.
     * POST /api/appels/seance/{seanceId}/etudiant/valider-qr
     */
    @PostMapping("/seance/{seanceId}/etudiant/valider-qr")
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<?> marquerPresentViaQr(
            @PathVariable Long seanceId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            // 1. Récupérer l'appel ouvert pour cette séance
            AppelDTO appel = appelService.getAppelOuvertBySeance(seanceId);
            if (appel == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Aucun appel ouvert pour cette séance"));
            }

            // 2. Valider le QR token de l'enseignant
            String qrToken = request.get("qrToken");
            if (qrToken == null || qrToken.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "QR token manquant"));
            }
            // validerQRCode lève une exception si le token est invalide
            appelService.validerQRCode(appel.getId(), qrToken);

            // 3. Identifier l'étudiant authentifié
            String email = authentication.getName();
            Long etudiantId = etudiantRepository.findByEmail(email)
                    .map(e -> e.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Étudiant non trouvé pour l'email: " + email));

            // 4. Enregistrer la présence
            EnregistrerPresenceRequest presenceReq = new EnregistrerPresenceRequest();
            presenceReq.setEtudiantId(etudiantId);
            presenceReq.setStatut("présent");
            presenceReq.setModeEnregistrement("QR");
            presenceReq.setRetard(false);

            PresenceDTO presence = appelService.enregistrerPresence(appel.getId(), presenceReq);
            return ResponseEntity.status(HttpStatus.CREATED).body(presence);

        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }
}
