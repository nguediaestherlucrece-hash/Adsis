package com.ISI.Appel_classe.Gestion_des_utilisateurs.controller.api;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.SurveillantCreateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.SurveillantUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response.SurveillantDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.service.serviceInterface.SurveillantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/surveillants")
@RequiredArgsConstructor
public class SurveillantController {

    private final SurveillantService surveillantService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SurveillantDTO>> getAll() {
        return ResponseEntity.ok(surveillantService.getAllSurveillants());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SurveillantDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(surveillantService.getSurveillantById(id));
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SurveillantDTO> getByEmail(@PathVariable String email) {
        return ResponseEntity.ok(surveillantService.getSurveillantByEmail(email));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SurveillantDTO> create(@RequestBody SurveillantCreateDTO dto) {
        log.info("Création surveillant: {}", dto.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(surveillantService.createSurveillant(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')  or hasRole('SURVEILLANT')")
    public ResponseEntity<SurveillantDTO> update(
            @PathVariable Long id,
            @RequestBody SurveillantUpdateDTO dto) {
        return ResponseEntity.ok(surveillantService.updateSurveillant(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        surveillantService.deleteSurveillant(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/filiere/{filiereId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SurveillantDTO>> getSurveillantByFiliere(@PathVariable Long filiereId) {
        return ResponseEntity.ok(surveillantService.getSurveillantsByFiliere(filiereId));
    }

    @PostMapping("/{id}/filiere/{filiereId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SurveillantDTO> assignByFiliere(@PathVariable Long id, @PathVariable Long filiereId) {
        return ResponseEntity.ok(surveillantService.assignSurveillantToFiliere(id, filiereId));
    }

}
