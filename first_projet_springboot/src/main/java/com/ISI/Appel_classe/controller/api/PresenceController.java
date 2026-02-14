package com.ISI.Appel_classe.controller.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/presences")
public class PresenceController {

    /**
     * Endpoint de test pour vérifier que le contrôleur fonctionne
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> testEndpoint() {
        log.info(" Endpoint /api/presences/test appelé à {}", LocalDateTime.now());
        log.debug("Ceci est un message de debug");

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Le contrôleur Presence fonctionne correctement !");
        response.put("timestamp", LocalDateTime.now().toString());

        log.info("Réponse envoyée avec succès");
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint pour enregistrer une présence
     */
    @PostMapping("/enregistrer")
    public ResponseEntity<Map<String, String>> enregistrerPresence(
            @RequestParam Long etudiantId,
            @RequestParam Long seanceId,
            @RequestParam String statut) {

        log.info("Tentative d'enregistrement de présence - Etudiant: {}, Séance: {}, Statut: {}",
                etudiantId, seanceId, statut);

        try {
           

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Présence enregistrée avec succès");

            log.info("Présence enregistrée pour l'étudiant {}", etudiantId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error(" Erreur lors de l'enregistrement de la présence: {}", e.getMessage(), e);

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Erreur lors de l'enregistrement");

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Endpoint pour consulter l'historique des présences d'un étudiant
     */
    @GetMapping("/etudiant/{etudiantId}")
    public ResponseEntity<Map<String, Object>> consulterHistorique(@PathVariable Long etudiantId) {
        log.info("Consultation de l'historique de présences pour l'étudiant {}", etudiantId);
 
        Map<String, Object> response = new HashMap<>();
        response.put("etudiantId", etudiantId);
        response.put("presences", new java.util.ArrayList<>());

        log.debug("Historique récupéré pour l'étudiant {}", etudiantId);
        return ResponseEntity.ok(response);
    }
}
