package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.controller.api;

import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.request.coursUECreateDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.request.coursUEUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.response.CoursUEDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.service.serviceInterface.CourUEService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/cours-ue")
@RequiredArgsConstructor
public class CoursUEController {

    private final CourUEService courUEService;

    @GetMapping
    @PreAuthorize("hasRole('ASSISTANT_PEDAGOGIQUE')")
    public ResponseEntity<List<CoursUEDTO>> getAllCours() {
        List<CoursUEDTO> cours = courUEService.getAllCours();
        return ResponseEntity.ok(cours);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ASSISTANT_PEDAGOGIQUE')")
    public ResponseEntity<CoursUEDTO> getCoursById(@PathVariable Long id) {
        CoursUEDTO dto = courUEService.getCoursById(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/code/{idCours}")
    @PreAuthorize("hasRole('ASSISTANT_PEDAGOGIQUE')")
    public ResponseEntity<CoursUEDTO> getCoursByCode(@PathVariable String idCours) {
        CoursUEDTO dto = courUEService.getCoursByCode(idCours);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    @PreAuthorize("hasRole('ASSISTANT_PEDAGOGIQUE')")
    public ResponseEntity<CoursUEDTO> createCours(@RequestBody coursUECreateDTO dto) {
        CoursUEDTO created = courUEService.createCours(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ASSISTANT_PEDAGOGIQUE')")
    public ResponseEntity<CoursUEDTO> updateCours(
            @PathVariable Long id,
            @RequestBody coursUEUpdateDTO dto) {
        CoursUEDTO updated = courUEService.updateCours(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ASSISTANT_PEDAGOGIQUE')")
    public ResponseEntity<Void> deleteCours(@PathVariable Long id) {
        courUEService.deleteCours(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/specialite/{specialiteId}")
    @PreAuthorize("hasRole('ASSISTANT_PEDAGOGIQUE')")
    public ResponseEntity<List<CoursUEDTO>> getCoursBySpecialite(@PathVariable Long specialiteId) {
        List<CoursUEDTO> cours = courUEService.getCoursBySpecialite(specialiteId);
        return ResponseEntity.ok(cours);
    }
}
