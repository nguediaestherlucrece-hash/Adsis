package com.ISI.Appel_classe.Gestion_des_utilisateurs.controller.api;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.AssistantPedagogiqueCreateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.AssistantPedagogiqueUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response.AssistantPedagogiqueDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.service.serviceInterface.AssistantPedagogiqueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/assistants-pedagogiques")
@RequiredArgsConstructor
public class AssistantPedagogiqueController {

    private final AssistantPedagogiqueService assistantService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AssistantPedagogiqueDTO>> getAll() {
        return ResponseEntity.ok(assistantService.getAllAssistants());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AssistantPedagogiqueDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(assistantService.getAssistantById(id));
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AssistantPedagogiqueDTO> getByEmail(@PathVariable String email) {
        return ResponseEntity.ok(assistantService.getAssistantByEmail(email));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AssistantPedagogiqueDTO> create(@RequestBody AssistantPedagogiqueCreateDTO dto) {
        log.info("Création assistant pédagogique: {}", dto.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(assistantService.createAssistant(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ASSISTANT_PEDAGOGIQUE')")
    public ResponseEntity<AssistantPedagogiqueDTO> update(
            @PathVariable Long id,
            @RequestBody AssistantPedagogiqueUpdateDTO dto) {
        return ResponseEntity.ok(assistantService.updateAssistant(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        assistantService.deleteAssistant(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/filiere/{filiereId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AssistantPedagogiqueDTO>> getAssistantPedagogiqueByFiliere(@PathVariable Long filiereId) {
        return ResponseEntity.ok(assistantService.getAssistantsByFiliere(filiereId));
    }

    @PostMapping("/{id}/filiere/{filiereId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AssistantPedagogiqueDTO> assignFiliere(@PathVariable Long id, @PathVariable Long filiereId) {
        return ResponseEntity.ok(assistantService.assignAssistantToFiliere(id, filiereId));
    }
}
