package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.controller.api;

import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.request.SemestreCreateDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.request.SemestreUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.response.SemestreDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.service.serviceInterface.SemestreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/semestres")
@RequiredArgsConstructor
public class SemestreController {

    private final SemestreService semestreService;

    @GetMapping
    @PreAuthorize("hasAuthority('SEMESTRE_READ')")
    public ResponseEntity<List<SemestreDTO>> getAllSemestres() {
        List<SemestreDTO> semestres = semestreService.getAllSemestres();
        return ResponseEntity.ok(semestres);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SEMESTRE_READ')")
    public ResponseEntity<SemestreDTO> getSemestreById(@PathVariable Long id) {
        SemestreDTO dto = semestreService.getSemestreById(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/code/{idSemestre}")
    @PreAuthorize("hasAuthority('SEMESTRE_READ')")
    public ResponseEntity<SemestreDTO> getSemestreByCode(@PathVariable String idSemestre) {
        SemestreDTO dto = semestreService.getSemestreByCode(idSemestre);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SEMESTRE_CREATE')")
    public ResponseEntity<SemestreDTO> createSemestre(@RequestBody SemestreCreateDTO dto) {
        SemestreDTO created = semestreService.createSemestre(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SEMESTRE_UPDATE')")
    public ResponseEntity<SemestreDTO> updateSemestre(
            @PathVariable Long id,
            @RequestBody SemestreUpdateDTO dto) {
        SemestreDTO updated = semestreService.updateSemestre(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SEMESTRE_DELETE')")
    public ResponseEntity<Void> deleteSemestre(@PathVariable Long id) {
        semestreService.deleteSemestre(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activer")
    @PreAuthorize("hasAuthority('SEMESTRE_UPDATE')")
    public ResponseEntity<SemestreDTO> activerSemestre(@PathVariable Long id) {
        SemestreDTO activated = semestreService.activerSemestre(id);
        return ResponseEntity.ok(activated);
    }

    @GetMapping("/annee/{anneeId}")
    @PreAuthorize("hasAuthority('SEMESTRE_READ')")
    public ResponseEntity<List<SemestreDTO>> getSemestresByAnnee(@PathVariable Long anneeId) {
        List<SemestreDTO> semestres = semestreService.getSemestresByAnnee(anneeId);
        return ResponseEntity.ok(semestres);
    }
}
