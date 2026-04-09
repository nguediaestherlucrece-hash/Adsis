package com.ISI.Appel_classe.Gestion_des_utilisateurs.controller.api;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.SpecialiteCreateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.SpecialiteUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response.SpecialiteDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.service.serviceInterface.SpecialiteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/specialites")
@RequiredArgsConstructor
public class SpecialiteController {

    private final SpecialiteService specialiteService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SpecialiteDTO>> getAllSpecialites() {
        return ResponseEntity.ok(specialiteService.getAllSpecialites());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SpecialiteDTO> getSpecialiteById(@PathVariable Long id) {
        return ResponseEntity.ok(specialiteService.getSpecialiteById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SpecialiteDTO> createSpecialite(@RequestBody SpecialiteCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(specialiteService.createSpecialite(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SpecialiteDTO> updateSpecialite(@PathVariable Long id, @RequestBody SpecialiteUpdateDTO dto) {
        return ResponseEntity.ok(specialiteService.updateSpecialite(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSpecialite(@PathVariable Long id) {
        specialiteService.deleteSpecialite(id);
        return ResponseEntity.noContent().build();
    }
}