package com.ISI.Appel_classe.controller.api;

import com.ISI.Appel_classe.dto.request.SurveillantCreateDTO;
import com.ISI.Appel_classe.dto.request.SurveillantUpdateDTO;
import com.ISI.Appel_classe.dto.response.SurveillantDTO;
import com.ISI.Appel_classe.service.SurveillantService;
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
    @PreAuthorize("hasAuthority('SURVEILLANT_READ')")
    public ResponseEntity<List<SurveillantDTO>> getAll() {
        return ResponseEntity.ok(surveillantService.getAllSurveillants());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SURVEILLANT_READ')")
    public ResponseEntity<SurveillantDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(surveillantService.getSurveillantById(id));
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasAuthority('SURVEILLANT_READ')")
    public ResponseEntity<SurveillantDTO> getByEmail(@PathVariable String email) {
        return ResponseEntity.ok(surveillantService.getSurveillantByEmail(email));
    }

    @GetMapping("/affectation/{affectation}")
    @PreAuthorize("hasAuthority('SURVEILLANT_READ')")
    public ResponseEntity<List<SurveillantDTO>> getByAffectation(@PathVariable String affectation) {
        return ResponseEntity.ok(surveillantService.getSurveillantsByAffectation(affectation));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SURVEILLANT_CREATE')")
    public ResponseEntity<SurveillantDTO> create(@RequestBody SurveillantCreateDTO dto) {
        log.info("Création surveillant: {}", dto.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(surveillantService.createSurveillant(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SURVEILLANT_UPDATE')")
    public ResponseEntity<SurveillantDTO> update(
            @PathVariable Long id,
            @RequestBody SurveillantUpdateDTO dto) {
        return ResponseEntity.ok(surveillantService.updateSurveillant(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SURVEILLANT_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        surveillantService.deleteSurveillant(id);
        return ResponseEntity.noContent().build();
    }
}
