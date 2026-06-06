package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.controller.api;

import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.request.EmploiDuTempsCreateDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.request.EmploiDuTempsUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.response.EmploiDuTempsDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.service.serviceInterface.EmploiDuTempsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.UtilisateurRepository;
import com.ISI.Appel_classe.Gestion_des_etudiants.repository.ClasseRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/emplois-du-temps")
@RequiredArgsConstructor
public class EmploiDuTempsController {

    private final EmploiDuTempsService emploiDuTempsService;
    private final UtilisateurRepository utilisateurRepository;
    private final ClasseRepository classeRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('COURSE_VIEW')")
    public ResponseEntity<List<EmploiDuTempsDTO>> getAllEmplois(Authentication auth) {
        List<EmploiDuTempsDTO> emplois = emploiDuTempsService.getAllEmplois();
        
        com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Utilisateur user = utilisateurRepository.findByEmail(auth.getName());
        if (user instanceof com.ISI.Appel_classe.Gestion_des_utilisateurs.model.AssistantPedagogique ap && ap.getFiliere() != null) {
            Long filiereId = ap.getFiliere().getId();
            emplois = emplois.stream()
                .filter(e -> {
                    if (e.getClasseId() == null) return false;
                    return classeRepository.findById(e.getClasseId())
                        .map(c -> c.getSpecialite() != null && c.getSpecialite().getNiveau() != null 
                            && c.getSpecialite().getNiveau().getFiliere() != null 
                            && c.getSpecialite().getNiveau().getFiliere().getId().equals(filiereId))
                        .orElse(false);
                })
                .collect(Collectors.toList());
        }
        return ResponseEntity.ok(emplois);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('COURSE_VIEW')")
    public ResponseEntity<EmploiDuTempsDTO> getEmploiById(@PathVariable Long id) {
        EmploiDuTempsDTO dto = emploiDuTempsService.getEmploiById(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/code/{idEmploi}")
    @PreAuthorize("hasAuthority('COURSE_VIEW')")
    public ResponseEntity<EmploiDuTempsDTO> getEmploiByCode(@PathVariable String idEmploi) {
        EmploiDuTempsDTO dto = emploiDuTempsService.getEmploiByCode(idEmploi);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('COURSE_MANAGE')")
    public ResponseEntity<EmploiDuTempsDTO> createEmploi(@RequestBody EmploiDuTempsCreateDTO dto) {
        EmploiDuTempsDTO created = emploiDuTempsService.createEmploi(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('COURSE_MANAGE')")
    public ResponseEntity<EmploiDuTempsDTO> updateEmploi(
            @PathVariable Long id,
            @RequestBody EmploiDuTempsUpdateDTO dto) {
        EmploiDuTempsDTO updated = emploiDuTempsService.updateEmploi(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('COURSE_MANAGE')")
    public ResponseEntity<Void> deleteEmploi(@PathVariable Long id) {
        emploiDuTempsService.deleteEmploi(id);
        return ResponseEntity.noContent().build();
    }
}
