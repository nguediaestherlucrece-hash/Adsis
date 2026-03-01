package com.ISI.Appel_classe.Gestion_des_utilisateurs.controller.api;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.FiliereCreateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.FiliereUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response.FiliereDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.service.serviceInterface.FiliereService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/filieres")
@RequiredArgsConstructor
public class FiliereController {

    private final FiliereService filiereService;

    @GetMapping
    @PreAuthorize("hasAuthority('FILIERE_READ')")
    public ResponseEntity<List<FiliereDTO>> getAllFiliere() {
        return ResponseEntity.ok(filiereService.getAllFilieres());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('FILIERE_READ')")
    public ResponseEntity<FiliereDTO> getFiliereById(@PathVariable Long id) {
        return ResponseEntity.ok(filiereService.getFiliereById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('FILIERE_CREATE')")
    public ResponseEntity<FiliereDTO> createFiliere(@RequestBody FiliereCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(filiereService.createFiliere(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('FILIERE_UPDATE')")
    public ResponseEntity<FiliereDTO> updateFiliere(@PathVariable Long id, @RequestBody FiliereUpdateDTO dto) {
        return ResponseEntity.ok(filiereService.updateFiliere(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('FILIERE_DELETE')")
    public ResponseEntity<Void> deleteFiliere(@PathVariable Long id) {
        filiereService.deleteFiliere(id);
        return ResponseEntity.noContent().build();
    }
}
