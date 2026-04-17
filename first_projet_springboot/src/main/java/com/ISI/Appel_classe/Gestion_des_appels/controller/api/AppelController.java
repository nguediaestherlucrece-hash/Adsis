package com.ISI.Appel_classe.Gestion_des_appels.controller.api;

import com.ISI.Appel_classe.Gestion_des_appels.dto.request.DemarrerAppelRequest;
import com.ISI.Appel_classe.Gestion_des_appels.dto.request.EnregistrerPresenceRequest;
import com.ISI.Appel_classe.Gestion_des_appels.dto.request.EnregistrerPresencesBatchRequest;
import com.ISI.Appel_classe.Gestion_des_appels.dto.response.AppelDTO;
import com.ISI.Appel_classe.Gestion_des_appels.dto.response.EtudiantSeanceDTO;
import com.ISI.Appel_classe.Gestion_des_appels.dto.response.PresenceDTO;
import com.ISI.Appel_classe.Gestion_des_appels.service.serviceInterface.AppelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAnyRole('ENSEIGNANT', 'SURVEILLANT', 'ASSISTANT_PEDAGOGIQUE')")
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
}
