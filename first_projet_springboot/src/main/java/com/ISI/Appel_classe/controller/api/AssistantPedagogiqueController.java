package com.ISI.Appel_classe.controller.api;

import com.ISI.Appel_classe.dto.request.AssistantPedagogiqueCreateDTO;
import com.ISI.Appel_classe.dto.request.AssistantPedagogiqueUpdateDTO;
import com.ISI.Appel_classe.dto.response.AssistantPedagogiqueDTO;
import com.ISI.Appel_classe.service.AssistantPedagogiqueService;
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
    @PreAuthorize("hasAuthority('ASSISTANT_READ')")
    public ResponseEntity<List<AssistantPedagogiqueDTO>> getAll() {
        return ResponseEntity.ok(assistantService.getAllAssistants());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSISTANT_READ')")
    public ResponseEntity<AssistantPedagogiqueDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(assistantService.getAssistantById(id));
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasAuthority('ASSISTANT_READ')")
    public ResponseEntity<AssistantPedagogiqueDTO> getByEmail(@PathVariable String email) {
        return ResponseEntity.ok(assistantService.getAssistantByEmail(email));
    }

    @GetMapping("/departement/{departement}")
    @PreAuthorize("hasAuthority('ASSISTANT_READ')")
    public ResponseEntity<List<AssistantPedagogiqueDTO>> getByDepartement(@PathVariable String departement) {
        return ResponseEntity.ok(assistantService.getAssistantsByDepartement(departement));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ASSISTANT_CREATE')")
    public ResponseEntity<AssistantPedagogiqueDTO> create(@RequestBody AssistantPedagogiqueCreateDTO dto) {
        log.info("Création assistant pédagogique: {}", dto.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(assistantService.createAssistant(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSISTANT_UPDATE')")
    public ResponseEntity<AssistantPedagogiqueDTO> update(
            @PathVariable Long id,
            @RequestBody AssistantPedagogiqueUpdateDTO dto) {
        return ResponseEntity.ok(assistantService.updateAssistant(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSISTANT_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        assistantService.deleteAssistant(id);
        return ResponseEntity.noContent().build();
    }
}
