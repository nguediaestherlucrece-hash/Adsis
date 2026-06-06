package com.ISI.Appel_classe.Gestion_des_etudiants.controller.api;

import com.ISI.Appel_classe.Gestion_des_etudiants.dto.response.ClasseDTO;
import com.ISI.Appel_classe.Gestion_des_etudiants.dto.request.ClasseCreateDTO;
import com.ISI.Appel_classe.Gestion_des_etudiants.dto.request.ClasseUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_etudiants.service.serviceInterface.ClasseService;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Utilisateur;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.AssistantPedagogique;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class ClasseController {

    private final ClasseService classeService;
    private final UtilisateurRepository utilisateurRepository;

    // Lecture : accessible à tous les rôles staff
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('ENSEIGNANT') or hasRole('ASSISTANT_PEDAGOGIQUE') or hasRole('SURVEILLANT')")
    public ResponseEntity<List<ClasseDTO>> getAllClasses(Authentication auth) {
        if (auth != null) {
            Utilisateur user = utilisateurRepository.findByEmail(auth.getName());
            if (user instanceof AssistantPedagogique ap && ap.getFiliere() != null) {
                return ResponseEntity.ok(classeService.getClassesByFiliere(ap.getFiliere().getId()));
            }
        }
        return ResponseEntity.ok(classeService.getAllClasses());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ENSEIGNANT') or hasRole('ASSISTANT_PEDAGOGIQUE') or hasRole('SURVEILLANT')")
    public ResponseEntity<ClasseDTO> getClasseById(@PathVariable Long id) {
        return ResponseEntity.ok(classeService.getClasseById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClasseDTO> createClasse(@RequestBody ClasseCreateDTO dto) {
        ClasseDTO created = classeService.createClasse(dto);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClasseDTO> updateClasse(@PathVariable Long id, @RequestBody ClasseUpdateDTO dto) {
        ClasseDTO updated = classeService.updateClasse(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteClasse(@PathVariable Long id) {
        classeService.deleteClasse(id);
        return ResponseEntity.noContent().build();
    }
}
