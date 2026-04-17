package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.controller.api;

import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.response.SeanceEnseignantDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.service.serviceInterface.SeanceEnseignantService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/seances")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SeanceEnseignantController {
    
    private final SeanceEnseignantService seanceEnseignantService;
    
    /**
     * Récupère les séances d'un enseignant pour aujourd'hui
     * GET /api/seances/enseignant/{enseignantId}
     */
    @GetMapping("/enseignant/{enseignantId}")
    @PreAuthorize("hasAnyRole('ENSEIGNANT', 'ASSISTANT_PEDAGOGIQUE', 'ADMIN')")
    public ResponseEntity<List<SeanceEnseignantDTO>> getSeancesEnseignantAujourdhui(
            @PathVariable Long enseignantId) {
        List<SeanceEnseignantDTO> seances = seanceEnseignantService
            .getSeancesEnseignantParDate(enseignantId, LocalDate.now());
        return ResponseEntity.ok(seances);
    }
    
    /**
     * Récupère les séances d'un enseignant pour une date spécifique
     * GET /api/seances/enseignant/{enseignantId}/date/{date}
     */
    @GetMapping("/enseignant/{enseignantId}/date/{date}")
    @PreAuthorize("hasAnyRole('ENSEIGNANT', 'ASSISTANT_PEDAGOGIQUE', 'ADMIN')")
    public ResponseEntity<List<SeanceEnseignantDTO>> getSeancesEnseignantParDate(
            @PathVariable Long enseignantId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<SeanceEnseignantDTO> seances = seanceEnseignantService
            .getSeancesEnseignantParDate(enseignantId, date);
        return ResponseEntity.ok(seances);
    }
    
    /**
     * Récupère les séances d'un enseignant pour la semaine en cours
     * GET /api/seances/enseignant/{enseignantId}/semaine
     */
    @GetMapping("/enseignant/{enseignantId}/semaine")
    @PreAuthorize("hasAnyRole('ENSEIGNANT', 'ASSISTANT_PEDAGOGIQUE', 'ADMIN')")
    public ResponseEntity<List<SeanceEnseignantDTO>> getSeancesEnseignantSemaine(
            @PathVariable Long enseignantId) {
        List<SeanceEnseignantDTO> seances = seanceEnseignantService
            .getSeancesEnseignantSemaine(enseignantId);
        return ResponseEntity.ok(seances);
    }
    
    /**
     * Récupère les séances d'un enseignant entre deux dates
     * GET /api/seances/enseignant/{enseignantId}/periode
     */
    @GetMapping("/enseignant/{enseignantId}/periode")
    @PreAuthorize("hasAnyRole('ENSEIGNANT', 'ASSISTANT_PEDAGOGIQUE', 'ADMIN')")
    public ResponseEntity<List<SeanceEnseignantDTO>> getSeancesEnseignantPeriode(
            @PathVariable Long enseignantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        List<SeanceEnseignantDTO> seances = seanceEnseignantService
            .getSeancesEnseignantPeriode(enseignantId, dateDebut, dateFin);
        return ResponseEntity.ok(seances);
    }
}
