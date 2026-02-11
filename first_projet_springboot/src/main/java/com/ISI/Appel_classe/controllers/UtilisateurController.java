package com.ISI.Appel_classe.controllers;

import com.ISI.Appel_classe.models.Utilisateur;
import com.ISI.Appel_classe.models.Statut;
import com.ISI.Appel_classe.repositories.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
public class UtilisateurController {

    private final UtilisateurRepository utilisateurRepository;

    /**
     * Récupérer la liste de tous les utilisateurs
     */
    @GetMapping
    public ResponseEntity<List<Utilisateur>> getAllUtilisateurs() {
        log.info("Récupération de tous les utilisateurs");
        List<Utilisateur> utilisateurs = utilisateurRepository.findAll();
        return ResponseEntity.ok(utilisateurs);
    }

    /**
     * Récupérer un utilisateur par son id(au cas ou on ne saitjamais)
     */
    @GetMapping("/{id}")
    public ResponseEntity<Utilisateur> getUtilisateurById(@PathVariable Long id) {
        log.info("Récupération de l'utilisateur avec id={}", id);
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findById(id);
        return utilisateurOpt
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Récupérer un utilisateur par son email
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<Utilisateur> getUtilisateurByEmail(@PathVariable String email) {
        log.info("Récupération de l'utilisateur avec email={}", email);
        Utilisateur u = utilisateurRepository.findByEmail(email);
        Optional<Utilisateur> utilisateurOpt = Optional.ofNullable(u);
        return utilisateurOpt
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Créer un nouvel utilisateur
     */
    @PostMapping
    public ResponseEntity<Utilisateur> createUtilisateur(@RequestBody Utilisateur utilisateur) {
        if (utilisateurRepository.findByEmail(utilisateur.getEmail()) == null) {
            log.info("Création d'un nouvel utilisateur: {}", utilisateur.getEmail());
            Utilisateur saved = utilisateurRepository.save(utilisateur);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } else  {
            return ResponseEntity.status(400).build();
        }
    }

    /**
     * Mettre à jour un utilisateur existant
     */
    @PutMapping("/{id}")
    public ResponseEntity<Utilisateur> updateUtilisateur(@PathVariable Long id,
                                                         @RequestBody Utilisateur utilisateurDetails) {
        log.info("Mise à jour de l'utilisateur id={}", id);

        return utilisateurRepository.findById(id)
                .map(existing -> {
                    existing.setNom(utilisateurDetails.getNom());
                    existing.setPrenom(utilisateurDetails.getPrenom());
                    existing.setEmail(utilisateurDetails.getEmail());
                    existing.setPassword(utilisateurDetails.getPassword());
                    existing.setRole(utilisateurDetails.getRole());
                    existing.setStatut(utilisateurDetails.getStatut());

                    Utilisateur updated = utilisateurRepository.save(existing);
                    return ResponseEntity.ok(updated);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Supprimer un utilisateur
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUtilisateur(@PathVariable Long id) {
        log.info("Suppression de l'utilisateur id={}", id);

        if (!utilisateurRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        utilisateurRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Changer le statut utilisateur existant
     */
    @PutMapping("/statut/{email}")
    public ResponseEntity<Utilisateur> BloquerUtilisateur(@PathVariable String email) {
        log.info("Mise à jour de l'utilisateur email={}", email);

        Utilisateur user = utilisateurRepository.findByEmail(email);
        if (user.getStatut() == Statut.INACTIF) {
            user.setStatut(Statut.ACTIF);
        } else {
            user.setStatut(Statut.INACTIF);
        }

        utilisateurRepository.save(user);

        return Optional.of(user)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

