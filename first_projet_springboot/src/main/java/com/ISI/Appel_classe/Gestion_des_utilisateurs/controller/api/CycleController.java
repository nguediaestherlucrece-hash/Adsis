package com.ISI.Appel_classe.Gestion_des_utilisateurs.controller.api;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.CycleCreateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.CycleUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response.CycleDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.service.serviceInterface.CycleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/cycles")
@RequiredArgsConstructor
public class CycleController {

    private final CycleService cycleService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CycleDTO>> getAllCycle() {
        return ResponseEntity.ok(cycleService.getAllCycles());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CycleDTO> getCycleById(@PathVariable Long id) {
        return ResponseEntity.ok(cycleService.getCycleById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CycleDTO> createCycle(@RequestBody CycleCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cycleService.createCycle(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CycleDTO> updateCycle(@PathVariable Long id, @RequestBody CycleUpdateDTO dto) {
        return ResponseEntity.ok(cycleService.updateCycle(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCycle(@PathVariable Long id) {
        cycleService.deleteCycle(id);
        return ResponseEntity.noContent().build();
    }
}