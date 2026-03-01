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
    @PreAuthorize("hasAuthority('CYCLE_READ')")
    public ResponseEntity<List<CycleDTO>> getAllCycle() {
        return ResponseEntity.ok(cycleService.getAllCycles());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CYCLE_READ')")
    public ResponseEntity<CycleDTO> getCycleById(@PathVariable Long id) {
        return ResponseEntity.ok(cycleService.getCycleById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CYCLE_CREATE')")
    public ResponseEntity<CycleDTO> createCycle(@RequestBody CycleCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cycleService.createCycle(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CYCLE_UPDATE')")
    public ResponseEntity<CycleDTO> updateCycle(@PathVariable Long id, @RequestBody CycleUpdateDTO dto) {
        return ResponseEntity.ok(cycleService.updateCycle(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CYCLE_DELETE')")
    public ResponseEntity<Void> deleteCycle(@PathVariable Long id) {
        cycleService.deleteCycle(id);
        return ResponseEntity.noContent().build();
    }
}