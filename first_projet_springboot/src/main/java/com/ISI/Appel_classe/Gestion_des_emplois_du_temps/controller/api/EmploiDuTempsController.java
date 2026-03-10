package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.controller.api;

import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.request.EmploiDuTempsCreateDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.request.EmploiDuTempsUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.response.EmploiDuTempsDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.service.serviceInterface.EmploiDuTempsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/emplois-du-temps")
@RequiredArgsConstructor
public class EmploiDuTempsController {

    private final EmploiDuTempsService emploiDuTempsService;

    @GetMapping
    @PreAuthorize("hasAuthority('EMPLOI_READ')")
    public ResponseEntity<List<EmploiDuTempsDTO>> getAllEmplois() {
        List<EmploiDuTempsDTO> emplois = emploiDuTempsService.getAllEmplois();
        return ResponseEntity.ok(emplois);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOI_READ')")
    public ResponseEntity<EmploiDuTempsDTO> getEmploiById(@PathVariable Long id) {
        EmploiDuTempsDTO dto = emploiDuTempsService.getEmploiById(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/code/{idEmploi}")
    @PreAuthorize("hasAuthority('EMPLOI_READ')")
    public ResponseEntity<EmploiDuTempsDTO> getEmploiByCode(@PathVariable String idEmploi) {
        EmploiDuTempsDTO dto = emploiDuTempsService.getEmploiByCode(idEmploi);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('EMPLOI_CREATE')")
    public ResponseEntity<EmploiDuTempsDTO> createEmploi(@RequestBody EmploiDuTempsCreateDTO dto) {
        EmploiDuTempsDTO created = emploiDuTempsService.createEmploi(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOI_UPDATE')")
    public ResponseEntity<EmploiDuTempsDTO> updateEmploi(
            @PathVariable Long id,
            @RequestBody EmploiDuTempsUpdateDTO dto) {
        EmploiDuTempsDTO updated = emploiDuTempsService.updateEmploi(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOI_DELETE')")
    public ResponseEntity<Void> deleteEmploi(@PathVariable Long id) {
        emploiDuTempsService.deleteEmploi(id);
        return ResponseEntity.noContent().build();
    }
}
