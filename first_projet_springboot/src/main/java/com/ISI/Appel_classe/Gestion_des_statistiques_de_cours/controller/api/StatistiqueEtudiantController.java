package com.ISI.Appel_classe.Gestion_des_statistiques_de_cours.controller.api;

import com.ISI.Appel_classe.Gestion_des_etudiants.model.Etudiant;
import com.ISI.Appel_classe.Gestion_des_etudiants.repository.EtudiantRepository;
import com.ISI.Appel_classe.Gestion_des_statistiques_de_cours.dto.CourseUEMinusStatsDTO;
import com.ISI.Appel_classe.Gestion_des_statistiques_de_cours.dto.PresenceHistoryDTO;
import com.ISI.Appel_classe.Gestion_des_statistiques_de_cours.dto.StudentPresenceStatsDTO;
import com.ISI.Appel_classe.Gestion_des_statistiques_de_cours.service.serviceInterface.StatistiquesService;
import com.ISI.Appel_classe.Gestion_des_statistiques_de_cours.util.ExcelExportUtils;
import com.ISI.Appel_classe.Gestion_des_statistiques_de_cours.util.PDFExportUtils;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/statistiques/etudiant")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ETUDIANT')")
public class StatistiqueEtudiantController {

    private final StatistiquesService statistiquesService;
    private final EtudiantRepository etudiantRepository;

    private Etudiant getEtudiantConnecte(Authentication auth) {
        String email = auth.getName();
        return etudiantRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Étudiant non trouvé pour l'email : " + email));
    }

    @GetMapping("/historique")
    public ResponseEntity<Page<PresenceHistoryDTO>> getHistorique(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestParam(required = false) Long semestreId,
            @RequestParam(required = false) Long coursId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth
    ) {
        Etudiant etudiant = getEtudiantConnecte(auth);
        // Utilise le tri par date de séance décroissante
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "seance.date"));
        Page<PresenceHistoryDTO> history = statistiquesService.getStudentPresenceHistory(
                etudiant.getId(), date, dateDebut, dateFin, semestreId, coursId, pageable
        );
        return ResponseEntity.ok(history);
    }

    @GetMapping("/personnelles")
    public ResponseEntity<StudentPresenceStatsDTO> getStatsPersonnelles(Authentication auth) {
        Etudiant etudiant = getEtudiantConnecte(auth);
        StudentPresenceStatsDTO stats = statistiquesService.getStudentPresenceStats(etudiant.getId());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/penalites")
    public ResponseEntity<List<CourseUEMinusStatsDTO>> getPenalites(Authentication auth) {
        Etudiant etudiant = getEtudiantConnecte(auth);
        List<CourseUEMinusStatsDTO> penalites = statistiquesService.getStudentPenalites(etudiant.getId());
        return ResponseEntity.ok(penalites);
    }

    @GetMapping("/export/excel")
    public ResponseEntity<Resource> exportExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestParam(required = false) Long semestreId,
            @RequestParam(required = false) Long coursId,
            Authentication auth
    ) {
        Etudiant etudiant = getEtudiantConnecte(auth);
        Page<PresenceHistoryDTO> history = statistiquesService.getStudentPresenceHistory(
                etudiant.getId(), null, dateDebut, dateFin, semestreId, coursId, Pageable.unpaged()
        );
        StudentPresenceStatsDTO stats = statistiquesService.getStudentPresenceStats(etudiant.getId());
        ByteArrayInputStream in = ExcelExportUtils.exportStudentHistory(history.getContent(), stats);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=historique_presence_" + etudiant.getMatricule() + ".xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<Resource> exportPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestParam(required = false) Long semestreId,
            @RequestParam(required = false) Long coursId,
            Authentication auth
    ) {
        Etudiant etudiant = getEtudiantConnecte(auth);
        Page<PresenceHistoryDTO> history = statistiquesService.getStudentPresenceHistory(
                etudiant.getId(), null, dateDebut, dateFin, semestreId, coursId, Pageable.unpaged()
        );
        StudentPresenceStatsDTO stats = statistiquesService.getStudentPresenceStats(etudiant.getId());
        String studentName = etudiant.getPrenom() + " " + etudiant.getNom();
        String className = etudiant.getClasse() != null ? etudiant.getClasse().getNomClasse() : "Aucune";

        ByteArrayInputStream in = PDFExportUtils.exportStudentHistoryPdf(
                history.getContent(), stats, studentName, etudiant.getMatricule(), className
        );

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=historique_presence_" + etudiant.getMatricule() + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(in));
    }
}
