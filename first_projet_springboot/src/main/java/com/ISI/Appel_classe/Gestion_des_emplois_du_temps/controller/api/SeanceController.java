package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.controller.api;

import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.request.SeanceCreateDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.request.SeanceUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.response.SeanceDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.service.serviceInterface.SeanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/seances")
@RequiredArgsConstructor
public class SeanceController {

    private final SeanceService seanceService;

    @GetMapping
    @PreAuthorize("hasAuthority('SEANCE_READ')")
    public ResponseEntity<List<SeanceDTO>> getAllSeances() {
        List<SeanceDTO> seances = seanceService.getAllSeances();
        return ResponseEntity.ok(seances);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SEANCE_READ')")
    public ResponseEntity<SeanceDTO> getSeanceById(@PathVariable Long id) {
        SeanceDTO dto = seanceService.getSeanceById(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/code/{idSeance}")
    @PreAuthorize("hasAuthority('SEANCE_READ')")
    public ResponseEntity<SeanceDTO> getSeanceByCode(@PathVariable String idSeance) {
        SeanceDTO dto = seanceService.getSeanceByCode(idSeance);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SEANCE_CREATE')")
    public ResponseEntity<SeanceDTO> createSeance(@RequestBody SeanceCreateDTO dto) {
        SeanceDTO created = seanceService.createSeance(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SEANCE_UPDATE')")
    public ResponseEntity<SeanceDTO> updateSeance(
            @PathVariable Long id,
            @RequestBody SeanceUpdateDTO dto) {
        SeanceDTO updated = seanceService.updateSeance(id, dto);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/emploi-du-temps/{edtId}")
    @PreAuthorize("hasAuthority('SEANCE_READ')")
    public ResponseEntity<List<SeanceDTO>> getSeancesByEmploiDuTemps(@PathVariable Long edtId) {
        List<SeanceDTO> seances = seanceService.getSeancesByEmploiDuTempsId(edtId);
        return ResponseEntity.ok(seances);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SEANCE_DELETE')")
    public ResponseEntity<Void> deleteSeance(@PathVariable Long id) {
        seanceService.deleteSeance(id);
        return ResponseEntity.noContent().build();
    }
}
