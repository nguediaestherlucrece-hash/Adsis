package com.ISI.Appel_classe.Gestion_des_appels_etudiant.controller.api;

import com.ISI.Appel_classe.Gestion_des_appels_etudiant.dto.response.*;
import com.ISI.Appel_classe.Gestion_des_etudiants.model.Etudiant;
import com.ISI.Appel_classe.Gestion_des_etudiants.repository.EtudiantRepository;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.Seance;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.repository.SeanceRepository;
import com.ISI.Appel_classe.Gestion_des_justificatifs.enums.StatutValidation;
import com.ISI.Appel_classe.Gestion_des_justificatifs.model.Justificatif;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.exception.ResourceNotFoundException;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Presence;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.PresenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Contrôleur REST dédié à la vue personnelle de l'étudiant connecté.
 *
 * Modèle réel :
 *   - CoursUE : champs intitule, codeUE (pas nom/code/salle)
 *   - Seance  : pas de champ salle propre
 *   - Presence.statut : "présent", "absent", "retard", "présent_partiel"
 *   - StatutValidation : EN_ATTENTE, ACCEPTE, REJETE  (pas REFUSE)
 *   - Presence.getJustificatif() disponible directement (OneToOne)
 */
@Slf4j
@RestController
@RequestMapping("/api/etudiant")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ETUDIANT')")
public class EtudiantDashboardController {

    private final EtudiantRepository etudiantRepository;
    private final PresenceRepository presenceRepository;
    private final SeanceRepository seanceRepository;

    // ═══ HELPERS ══════════════════════════════════════════════════════════════

    /** Résout l'étudiant connecté à partir de l'email JWT. */
    private Etudiant getEtudiantConnecte(Authentication auth) {
        String email = auth.getName();
        return etudiantRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Étudiant non trouvé pour l'email : " + email));
    }

    /**
     * Libellé lisible d'un CoursUE.
     * CoursUE possède : intitule, codeUE  (pas getNom/getCode/getSalle)
     */
    private String getLibelleCours(Seance s) {
        if (s == null || s.getCours() == null) return "—";
        String intitule = s.getCours().getIntitule();
        String code     = s.getCours().getCodeUE();
        return (intitule != null && !intitule.isBlank()) ? intitule
             : (code     != null && !code.isBlank())     ? code
             : "—";
    }

    /** Vérifie si la présence est une absence (statut "absent" ou "retard"). */
    private boolean estAbsent(Presence p) {
        String s = p.getStatut();
        return "absent".equalsIgnoreCase(s) || "retard".equalsIgnoreCase(s);
    }

    /** Vérifie si le justificatif associé à une présence est ACCEPTE. */
    private boolean estJustifiee(Presence p) {
        Justificatif j = p.getJustificatif();   // OneToOne direct, pas de repository call
        return j != null && StatutValidation.ACCEPTE.equals(j.getStatutValidation());
    }

    /** Justificatif REJETE ou absent → l'absence peut être re-justifiée. */
    private boolean peutEtreJustifiee(Presence p) {
        Justificatif j = p.getJustificatif();
        return j == null || StatutValidation.REJETE.equals(j.getStatutValidation());
    }

    private Long justificatifId(Presence p) {
        return p.getJustificatif() != null ? p.getJustificatif().getId() : null;
    }

    private Comparator<Presence> parDateDesc() {
        return Comparator.comparing(
                (Presence p) -> p.getSeance() != null && p.getSeance().getDate() != null
                        ? p.getSeance().getDate() : LocalDate.MIN,
                Comparator.reverseOrder());
    }

    /**
     * Retourne UNE seule présence par séance pour l'étudiant :
     * celle dont l'ID est le plus élevé (= le dernier appel enregistré).
     * Cela garantit que si l'enseignant relance l'appel et marque l'étudiant
     * présent, la présence de l'appel précédent (absent) ne remonte plus.
     */
    private List<Presence> getLatestPresencesParSeance(Long etudiantId) {
        return new ArrayList<>(
                presenceRepository.findByEtudiantId(etudiantId).stream()
                        .filter(p -> p.getSeance() != null)
                        .collect(Collectors.toMap(
                                p -> p.getSeance().getId(),
                                p -> p,
                                (p1, p2) -> p1.getId() > p2.getId() ? p1 : p2
                        ))
                        .values()
        );
    }

    /** Chaîne du statut du justificatif ou null. */
    private String statJustificatif(Presence p) {
        Justificatif j = p.getJustificatif();
        return j != null ? j.getStatutValidation().name() : null;
    }

    // ═══ GET /api/etudiant/cours/a-venir ══════════════════════════════════════

    @GetMapping("/cours/a-venir")
    public ResponseEntity<List<ProchainCoursDTO>> getProchainsCours(Authentication auth) {
        Etudiant etudiant = getEtudiantConnecte(auth);
        log.info("[ETU] GET /cours/a-venir — {}", etudiant.getEmail());

        if (etudiant.getClasse() == null) return ResponseEntity.ok(Collections.emptyList());

        LocalDate today = LocalDate.now();
        List<Seance> seances = seanceRepository.findByClasseIdAndDateBetween(
                etudiant.getClasse().getId(), today, today.plusDays(7));

        List<ProchainCoursDTO> result = seances.stream()
                .filter(s -> s.getDate() != null && !s.getDate().isBefore(today))
                .sorted(Comparator.comparing(Seance::getDate)
                        .thenComparing(s -> s.getHeureDebut() != null
                                ? s.getHeureDebut() : java.time.LocalTime.MIDNIGHT))
                .limit(5)
                .map(s -> {
                    ProchainCoursDTO dto = new ProchainCoursDTO();
                    dto.setSeanceId(s.getId());
                    dto.setDate(s.getDate());
                    dto.setJour(s.getDate().getDayOfWeek()
                            .getDisplayName(TextStyle.FULL, Locale.FRENCH));
                    dto.setHeureDebut(s.getHeureDebut());
                    dto.setHeureFin(s.getHeureFin());
                    dto.setMatiere(getLibelleCours(s));
                    if (s.getEnseignants() != null && !s.getEnseignants().isEmpty()) {
                        dto.setEnseignant(s.getEnseignants().get(0).getPrenom()
                                + " " + s.getEnseignants().get(0).getNom());
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // ═══ GET /api/etudiant/absences ═══════════════════════════════════════════

    @GetMapping("/absences")
    public ResponseEntity<List<AbsenceResumeeDTO>> getAbsencesRecentes(
            @RequestParam(defaultValue = "5") int limite, Authentication auth) {
        Etudiant etudiant = getEtudiantConnecte(auth);
        log.info("[ETU] GET /absences?limite={} — {}", limite, etudiant.getEmail());

        List<AbsenceResumeeDTO> result = getLatestPresencesParSeance(etudiant.getId())
                .stream()
                .filter(this::estAbsent)
                .sorted(parDateDesc())
                .limit(limite)
                .map(p -> {
                    AbsenceResumeeDTO dto = new AbsenceResumeeDTO();
                    dto.setId(p.getId());
                    if (p.getSeance() != null) {
                        dto.setDate(p.getSeance().getDate());
                        dto.setMatiere(getLibelleCours(p.getSeance()));
                    }
                    dto.setJustifiee(estJustifiee(p));
                    dto.setJustificatifStatut(statJustificatif(p));
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // ═══ GET /api/etudiant/absences/toutes ════════════════════════════════════

    @GetMapping("/absences/toutes")
    public ResponseEntity<List<AbsenceDetailDTO>> getToutesAbsences(Authentication auth) {
        Etudiant etudiant = getEtudiantConnecte(auth);
        log.info("[ETU] GET /absences/toutes — {}", etudiant.getEmail());

        List<AbsenceDetailDTO> result = getLatestPresencesParSeance(etudiant.getId())
                .stream()
                .filter(this::estAbsent)
                .sorted(parDateDesc())
                .map(p -> {
                    AbsenceDetailDTO dto = new AbsenceDetailDTO();
                    dto.setId(p.getId());
                    dto.setStatut(p.getStatut());
                    if (p.getSeance() != null) {
                        dto.setDate(p.getSeance().getDate());
                        if (p.getSeance().getHeureDebut() != null)
                            dto.setHeureDebut(p.getSeance().getHeureDebut().toString());
                        if (p.getSeance().getHeureFin() != null)
                            dto.setHeureFin(p.getSeance().getHeureFin().toString());
                        dto.setMatiere(getLibelleCours(p.getSeance()));
                    }
                    dto.setJustifiee(estJustifiee(p));
                    dto.setJustificatifId(justificatifId(p));
                    dto.setJustificatifStatut(statJustificatif(p));
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // ═══ GET /api/etudiant/absences/non-justifiees ════════════════════════════

    @GetMapping("/absences/non-justifiees")
    public ResponseEntity<List<AbsencePourJustificationDTO>> getAbsencesNonJustifiees(
            Authentication auth) {
        Etudiant etudiant = getEtudiantConnecte(auth);
        log.info("[ETU] GET /absences/non-justifiees — {}", etudiant.getEmail());

        List<AbsencePourJustificationDTO> result = getLatestPresencesParSeance(etudiant.getId())
                .stream()
                .filter(this::estAbsent)
                // Inclure toutes les absences qui n'ont pas encore été ACCEPTEES
                // (ni soumises, EN_ATTENTE, REJETE → tout sauf ACCEPTE)
                .filter(p -> !estJustifiee(p))
                .sorted(parDateDesc())
                .map(p -> {
                    AbsencePourJustificationDTO dto = new AbsencePourJustificationDTO();
                    dto.setId(p.getId());
                    if (p.getSeance() != null) {
                        dto.setDate(p.getSeance().getDate());
                        dto.setMatiere(getLibelleCours(p.getSeance()));
                    }
                    // Renseigner le statut du justificatif pour que le frontend
                    // puisse griser l'option si un justificatif est EN_ATTENTE
                    dto.setJustificatifStatut(statJustificatif(p));
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // ═══ GET /api/etudiant/emploi-du-temps/semaine ════════════════════════════

    @GetMapping("/emploi-du-temps/semaine")
    public ResponseEntity<List<SeanceSemaineDTO>> getEmploiDuTempsSemaine(
            @RequestParam(required = false) String date, Authentication auth) {
        Etudiant etudiant = getEtudiantConnecte(auth);
        log.info("[ETU] GET /emploi-du-temps/semaine — {}", etudiant.getEmail());

        if (etudiant.getClasse() == null) return ResponseEntity.ok(Collections.emptyList());

        LocalDate refDate = (date != null && !date.isBlank())
                ? LocalDate.parse(date) : LocalDate.now();
        LocalDate lundi    = refDate.with(DayOfWeek.MONDAY);
        LocalDate vendredi = lundi.plusDays(4);

        List<SeanceSemaineDTO> result = seanceRepository
                .findByClasseIdAndDateBetween(etudiant.getClasse().getId(), lundi, vendredi)
                .stream()
                .filter(s -> s.getDate() != null)
                .sorted(Comparator.comparing(Seance::getDate)
                        .thenComparing(s -> s.getHeureDebut() != null
                                ? s.getHeureDebut() : java.time.LocalTime.MIDNIGHT))
                .map(s -> {
                    SeanceSemaineDTO dto = new SeanceSemaineDTO();
                    dto.setSeanceId(s.getId());
                    dto.setDate(s.getDate());
                    dto.setJour(s.getDate().getDayOfWeek()
                            .getDisplayName(TextStyle.FULL, Locale.FRENCH));
                    dto.setHeureDebut(s.getHeureDebut());
                    dto.setHeureFin(s.getHeureFin());
                    dto.setMatiere(getLibelleCours(s));
                    if (s.getEnseignants() != null && !s.getEnseignants().isEmpty()) {
                        dto.setEnseignant(s.getEnseignants().get(0).getPrenom()
                                + " " + s.getEnseignants().get(0).getNom());
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}
