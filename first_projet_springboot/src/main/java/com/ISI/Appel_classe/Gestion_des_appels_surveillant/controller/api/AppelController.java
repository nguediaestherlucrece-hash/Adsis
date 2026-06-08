package com.ISI.Appel_classe.Gestion_des_appels_surveillant.controller.api;

import com.ISI.Appel_classe.Gestion_des_appels_enseignant.dto.request.DemarrerAppelRequest;
import com.ISI.Appel_classe.Gestion_des_appels_enseignant.dto.request.EnregistrerPresenceRequest;
import com.ISI.Appel_classe.Gestion_des_appels_enseignant.dto.request.EnregistrerPresencesBatchRequest;
import com.ISI.Appel_classe.Gestion_des_appels_enseignant.dto.response.AppelDTO;
import com.ISI.Appel_classe.Gestion_des_appels_enseignant.dto.response.EtudiantSeanceDTO;
import com.ISI.Appel_classe.Gestion_des_appels_enseignant.dto.response.PresenceDTO;
import com.ISI.Appel_classe.Gestion_des_appels_enseignant.service.serviceInterface.AppelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/surveillant/appels")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AppelController {

    private final AppelService appelService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SURVEILLANT', 'ASSISTANT_PEDAGOGIQUE')")
    public ResponseEntity<AppelDTO> demarrerAppel(@Valid @RequestBody DemarrerAppelRequest request) {
        AppelDTO appel = appelService.demarrerAppel(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(appel);
    }

    @PutMapping("/{id}/cloturer")
    @PreAuthorize("hasAnyRole('SURVEILLANT', 'ASSISTANT_PEDAGOGIQUE')")
    public ResponseEntity<AppelDTO> cloturerAppel(@PathVariable Long id) {
        AppelDTO appel = appelService.cloturerAppel(id);
        return ResponseEntity.ok(appel);
    }

    @PostMapping("/{id}/presences")
    @PreAuthorize("hasAnyRole('SURVEILLANT', 'ASSISTANT_PEDAGOGIQUE')")
    public ResponseEntity<PresenceDTO> enregistrerPresence(
            @PathVariable Long id,
            @Valid @RequestBody EnregistrerPresenceRequest request) {
        PresenceDTO presence = appelService.enregistrerPresence(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(presence);
    }

    @PostMapping("/presences/batch")
    @PreAuthorize("hasAnyRole('SURVEILLANT', 'ASSISTANT_PEDAGOGIQUE')")
    public ResponseEntity<List<PresenceDTO>> enregistrerPresencesBatch(
            @Valid @RequestBody EnregistrerPresencesBatchRequest request) {
        List<PresenceDTO> presences = appelService.enregistrerPresencesBatch(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(presences);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SURVEILLANT', 'ASSISTANT_PEDAGOGIQUE')")
    public ResponseEntity<AppelDTO> getAppelById(@PathVariable Long id) {
        AppelDTO appel = appelService.getAppelById(id);
        return ResponseEntity.ok(appel);
    }

    @GetMapping("/seance/{seanceId}/historique")
    @PreAuthorize("hasAnyRole('SURVEILLANT', 'ASSISTANT_PEDAGOGIQUE')")
    public ResponseEntity<List<AppelDTO>> getHistoriqueAppels(@PathVariable Long seanceId) {
        List<AppelDTO> historique = appelService.getHistoriqueAppels(seanceId);
        return ResponseEntity.ok(historique);
    }

    @GetMapping("/seance/{seanceId}/ouvert")
    @PreAuthorize("hasAnyRole('SURVEILLANT', 'ASSISTANT_PEDAGOGIQUE')")
    public ResponseEntity<AppelDTO> getAppelOuvertBySeance(@PathVariable Long seanceId) {
        AppelDTO appel = appelService.getAppelOuvertBySeance(seanceId);
        if (appel == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(appel);
    }

    @GetMapping("/seance/{seanceId}/etudiants")
    @PreAuthorize("hasAnyRole('SURVEILLANT', 'ASSISTANT_PEDAGOGIQUE')")
    public ResponseEntity<List<EtudiantSeanceDTO>> getEtudiantsSeance(
            @PathVariable Long seanceId,
            @RequestParam(required = false) Long appelId) {
        List<EtudiantSeanceDTO> etudiants = appelService.getEtudiantsSeanceAvecStatut(seanceId, appelId);
        return ResponseEntity.ok(etudiants);
    }

    @PostMapping("/{id}/valider-pin")
    @PreAuthorize("hasAnyRole('SURVEILLANT', 'ASSISTANT_PEDAGOGIQUE')")
    public ResponseEntity<Map<String, Boolean>> validerCodePIN(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String codePIN = request.get("codePIN");
        boolean valide = appelService.validerCodePIN(id, codePIN);

        return ResponseEntity.ok(Map.of("valide", valide));
    }

    @PostMapping("/{id}/regenerer-qr")
    @PreAuthorize("hasAnyRole('SURVEILLANT', 'ASSISTANT_PEDAGOGIQUE')")
    public ResponseEntity<AppelDTO> regenererQRCode(@PathVariable Long id) {
        AppelDTO appel = appelService.regenererQRCode(id);
        return ResponseEntity.ok(appel);
    }

    @PostMapping("/{id}/generer-pin")
    @PreAuthorize("hasAnyRole('SURVEILLANT', 'ASSISTANT_PEDAGOGIQUE')")
    public ResponseEntity<AppelDTO> genererPIN(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Integer> request) {
        Integer duree = (request != null) ? request.get("dureeMinutes") : null;
        AppelDTO appel = appelService.genererPINPourAppel(id, duree);
        return ResponseEntity.ok(appel);
    }

    @GetMapping("/{id}/presences")
    @PreAuthorize("hasAnyRole('SURVEILLANT', 'ASSISTANT_PEDAGOGIQUE')")
    public ResponseEntity<List<PresenceDTO>> getPresences(@PathVariable Long id) {
        List<PresenceDTO> presences = appelService.getPresencesByAppelId(id);
        return ResponseEntity.ok(presences);
    }

    @PostMapping("/{id}/valider-qr")
    @PreAuthorize("hasAnyRole('SURVEILLANT', 'ASSISTANT_PEDAGOGIQUE')")
    public ResponseEntity<AppelDTO> validerQR(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String qrToken = request.get("qrToken");
        AppelDTO appel = appelService.validerQRCode(id, qrToken);
        return ResponseEntity.ok(appel);
    }

    @PostMapping("/{id}/valider-pin-etudiant")
    @PreAuthorize("hasAnyRole('SURVEILLANT', 'ASSISTANT_PEDAGOGIQUE')")
    public ResponseEntity<PresenceDTO> validerPINEtudiant(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String codePIN = request.get("codePIN");
        PresenceDTO presence = appelService.validerCodePINEtudiant(id, codePIN);
        return ResponseEntity.status(HttpStatus.CREATED).body(presence);
    }

    @PostMapping("/{id}/valider-qr-etudiant")
    @PreAuthorize("hasAnyRole('SURVEILLANT', 'ASSISTANT_PEDAGOGIQUE')")
    public ResponseEntity<PresenceDTO> validerQRCodeEtudiant(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String qrToken = request.get("qrToken");
        PresenceDTO presence = appelService.validerQRCodeEtudiant(id, qrToken);
        return ResponseEntity.status(HttpStatus.CREATED).body(presence);
    }
}
