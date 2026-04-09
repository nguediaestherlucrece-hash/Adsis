package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.controller.api;

import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.request.AnneeAcademiqueCreateDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.request.AnneeAcademiqueUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.response.AnneeAcademiqueDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.service.serviceInterface.AnneeAcademiqueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/annees-academiques")
@RequiredArgsConstructor
public class AnneeAcademiqueController {

    private final AnneeAcademiqueService anneeAcademiqueService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AnneeAcademiqueDTO>> getAllAnnees() {
        List<AnneeAcademiqueDTO> annees = anneeAcademiqueService.getAllAnnees();
        return ResponseEntity.ok(annees);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnneeAcademiqueDTO> getAnneeById(@PathVariable Long id) {
        AnneeAcademiqueDTO dto = anneeAcademiqueService.getAnneeById(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/code/{idAnnee}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnneeAcademiqueDTO> getAnneeByCode(@PathVariable String idAnnee) {
        AnneeAcademiqueDTO dto = anneeAcademiqueService.getAnneeByCode(idAnnee);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnneeAcademiqueDTO> createAnnee(@RequestBody AnneeAcademiqueCreateDTO dto) {
        AnneeAcademiqueDTO created = anneeAcademiqueService.createAnnee(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnneeAcademiqueDTO> updateAnnee(
            @PathVariable Long id,
            @RequestBody AnneeAcademiqueUpdateDTO dto) {
        AnneeAcademiqueDTO updated = anneeAcademiqueService.updateAnnee(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAnnee(@PathVariable Long id) {
        anneeAcademiqueService.deleteAnnee(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnneeAcademiqueDTO> activerAnnee(@PathVariable Long id) {
        AnneeAcademiqueDTO activated = anneeAcademiqueService.activerAnnee(id);
        return ResponseEntity.ok(activated);
    }
}
