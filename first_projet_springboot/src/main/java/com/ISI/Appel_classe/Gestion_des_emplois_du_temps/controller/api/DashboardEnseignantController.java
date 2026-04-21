package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.controller.api;

import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.response.DashboardEnseignantDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.service.serviceInterface.SeanceEnseignantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard/enseignant")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DashboardEnseignantController {

    private final SeanceEnseignantService seanceEnseignantService;

    /**
     * Récupère toutes les données du tableau de bord enseignant
     * GET /api/dashboard/enseignant/{enseignantId}
     */
    @GetMapping("/{enseignantId}")
    @PreAuthorize("hasRole('ENSEIGNANT')")
    public ResponseEntity<DashboardEnseignantDTO> getDashboard(@PathVariable Long enseignantId) {
        DashboardEnseignantDTO dashboard = seanceEnseignantService.getDashboardData(enseignantId);
        return ResponseEntity.ok(dashboard);
    }
}
