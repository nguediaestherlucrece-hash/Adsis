package com.ISI.Appel_classe.Gestion_des_utilisateurs.controller.api;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.NiveauCreateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.NiveauUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response.NiveauDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.service.serviceInterface.NiveauService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/niveaux")
@RequiredArgsConstructor
public class NiveauController {

    private final NiveauService niveauService;

    // Lecture : accessible à tous les rôles staff (filtres cascadants)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('ENSEIGNANT') or hasRole('ASSISTANT_PEDAGOGIQUE') or hasRole('SURVEILLANT')")
    public ResponseEntity<List<NiveauDTO>> getAllNiveaux() {
        return ResponseEntity.ok(niveauService.getAllNiveaux());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ENSEIGNANT') or hasRole('ASSISTANT_PEDAGOGIQUE') or hasRole('SURVEILLANT')")
    public ResponseEntity<NiveauDTO> getNiveauById(@PathVariable Long id) {
        return ResponseEntity.ok(niveauService.getNiveauById(id));
    }

    // Écriture : ADMIN uniquement
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NiveauDTO> createNiveau(@RequestBody NiveauCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(niveauService.createNiveau(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NiveauDTO> updateNiveau(@PathVariable Long id, @RequestBody NiveauUpdateDTO dto) {
        return ResponseEntity.ok(niveauService.updateNiveau(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteNiveau(@PathVariable Long id) {
        niveauService.deleteNiveau(id);
        return ResponseEntity.noContent().build();
    }
}