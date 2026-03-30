package com.ISI.Appel_classe.Gestion_des_etudiants.controller.api;

import com.ISI.Appel_classe.Gestion_des_etudiants.dto.response.ClasseDTO;
import com.ISI.Appel_classe.Gestion_des_etudiants.dto.request.ClasseCreateDTO;
import com.ISI.Appel_classe.Gestion_des_etudiants.dto.request.ClasseUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_etudiants.service.serviceInterface.ClasseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class ClasseController {

    private final ClasseService classeService;

    @GetMapping
    @PreAuthorize("hasAuthority('CLASSE_READ')")
    public ResponseEntity<List<ClasseDTO>> getAllClasses() {
        return ResponseEntity.ok(classeService.getAllClasses());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CLASSE_READ')")
    public ResponseEntity<ClasseDTO> getClasseById(@PathVariable Long id) {
        return ResponseEntity.ok(classeService.getClasseById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CLASSE_CREATE')")
    public ResponseEntity<ClasseDTO> createClasse(@RequestBody ClasseCreateDTO dto) {
        ClasseDTO created = classeService.createClasse(dto);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CLASSE_UPDATE')")
    public ResponseEntity<ClasseDTO> updateClasse(@PathVariable Long id, @RequestBody ClasseUpdateDTO dto) {
        ClasseDTO updated = classeService.updateClasse(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CLASSE_DELETE')")
    public ResponseEntity<Void> deleteClasse(@PathVariable Long id) {
        classeService.deleteClasse(id);
        return ResponseEntity.noContent().build();
    }
}
