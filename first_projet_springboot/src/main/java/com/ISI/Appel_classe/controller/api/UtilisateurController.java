package com.ISI.Appel_classe.controller.api;

import com.ISI.Appel_classe.dto.request.UtilisateurCreateDTO;
import com.ISI.Appel_classe.dto.request.UtilisateurUpdateDTO;
import com.ISI.Appel_classe.dto.response.UtilisateurDTO;
import com.ISI.Appel_classe.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    /**
     * Récupérer la liste de tous les utilisateurs
     */
    @GetMapping
    public ResponseEntity<List<UtilisateurDTO>> getAllUtilisateurs() {
        log.info("Récupération de tous les utilisateurs");
        return ResponseEntity.ok(utilisateurService.getAllUtilisateurs());
    }

    /**
     * Récupérer un utilisateur par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<UtilisateurDTO> getUtilisateurById(@PathVariable Long id) {
        log.info("Récupération de l'utilisateur avec id={}", id);
        return ResponseEntity.ok(utilisateurService.getUtilisateurById(id));
    }

    /**
     * Récupérer un utilisateur par son email
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<UtilisateurDTO> getUtilisateurByEmail(@PathVariable String email) {
        log.info("Récupération de l'utilisateur avec email={}", email);
        return ResponseEntity.ok(utilisateurService.getUtilisateurByEmail(email));
    }

    /**
     * Créer un nouvel utilisateur
     */
    @PostMapping
    public ResponseEntity<UtilisateurDTO> createUtilisateur(@RequestBody UtilisateurCreateDTO dto) {
        log.info("Création d'un nouvel utilisateur: {}", dto.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(utilisateurService.createUtilisateur(dto));
    }

    /**
     * Mettre à jour un utilisateur existant
     */
    @PutMapping("/{id}")
    public ResponseEntity<UtilisateurDTO> updateUtilisateur(@PathVariable Long id,
            @RequestBody UtilisateurUpdateDTO dto) {
        log.info("Mise à jour de l'utilisateur id={}", id);
        return ResponseEntity.ok(utilisateurService.updateUtilisateur(id, dto));
    }

    /**
     * Supprimer un utilisateur
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUtilisateur(@PathVariable Long id) {
        log.info("Suppression de l'utilisateur id={}", id);
        utilisateurService.deleteUtilisateur(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Activer/Désactiver un utilisateur
     */
    @PatchMapping("/statut/{email}")
    public ResponseEntity<UtilisateurDTO> toggleStatut(@PathVariable String email) {
        log.info("Changement de statut pour l'utilisateur email={}", email);
        return ResponseEntity.ok(utilisateurService.toggleStatut(email));
    }
}
