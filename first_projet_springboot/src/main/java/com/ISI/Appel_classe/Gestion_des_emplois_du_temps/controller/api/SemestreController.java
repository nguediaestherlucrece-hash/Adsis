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

    // Lecture : accessible à tous les rôles staff
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('ENSEIGNANT') or hasRole('ASSISTANT_PEDAGOGIQUE') or hasRole('SURVEILLANT')")
    public ResponseEntity<List<SemestreDTO>> getAllSemestres() {
        List<SemestreDTO> semestres = semestreService.getAllSemestres();
        return ResponseEntity.ok(semestres);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ENSEIGNANT') or hasRole('ASSISTANT_PEDAGOGIQUE') or hasRole('SURVEILLANT')")
    public ResponseEntity<SemestreDTO> getSemestreById(@PathVariable Long id) {
        SemestreDTO dto = semestreService.getSemestreById(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/code/{idSemestre}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ENSEIGNANT') or hasRole('ASSISTANT_PEDAGOGIQUE') or hasRole('SURVEILLANT')")
    public ResponseEntity<SemestreDTO> getSemestreByCode(@PathVariable String idSemestre) {
        SemestreDTO dto = semestreService.getSemestreByCode(idSemestre);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SemestreDTO> createSemestre(@RequestBody SemestreCreateDTO dto) {
        SemestreDTO created = semestreService.createSemestre(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SemestreDTO> updateSemestre(
            @PathVariable Long id,
            @RequestBody SemestreUpdateDTO dto) {
        SemestreDTO updated = semestreService.updateSemestre(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSemestre(@PathVariable Long id) {
        semestreService.deleteSemestre(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SemestreDTO> activerSemestre(@PathVariable Long id) {
        SemestreDTO activated = semestreService.activerSemestre(id);
        return ResponseEntity.ok(activated);
    }

    @GetMapping("/annee/{anneeId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ENSEIGNANT') or hasRole('ASSISTANT_PEDAGOGIQUE') or hasRole('SURVEILLANT')")
    public ResponseEntity<List<SemestreDTO>> getSemestresByAnnee(@PathVariable Long anneeId) {
        List<SemestreDTO> semestres = semestreService.getSemestresByAnnee(anneeId);
        return ResponseEntity.ok(semestres);
    }
}
