package com.ISI.Appel_classe.Gestion_des_statistiques_de_cours.controller.api;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Enseignant;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.EnseignantRepository;
import com.ISI.Appel_classe.Gestion_des_statistiques_de_cours.dto.TeacherSeanceStatsDTO;
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
@RequestMapping("/api/statistiques/enseignant")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ENSEIGNANT')")
public class StatistiqueEnseignantController {

    private final StatistiquesService statistiquesService;
    private final EnseignantRepository enseignantRepository;

    private Enseignant getEnseignantConnecte(Authentication auth) {
        String email = auth.getName();
        return enseignantRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Enseignant non trouvé pour l'email : " + email));
    }

    @GetMapping("/sessions")
    public ResponseEntity<Page<TeacherSeanceStatsDTO>> getSessions(
            @RequestParam(required = false) Long coursId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth
    ) {
        Enseignant enseignant = getEnseignantConnecte(auth);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"));
        Page<TeacherSeanceStatsDTO> stats = statistiquesService.getTeacherSeanceStats(
                enseignant.getId(), coursId, dateDebut, dateFin, pageable
        );
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/export/excel")
    public ResponseEntity<Resource> exportExcel(
            @RequestParam(required = false) Long coursId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            Authentication auth
    ) {
        Enseignant enseignant = getEnseignantConnecte(auth);
        List<TeacherSeanceStatsDTO> stats = statistiquesService.getTeacherSeanceStatsList(
                enseignant.getId(), coursId, dateDebut, dateFin
        );
        ByteArrayInputStream in = ExcelExportUtils.exportTeacherHistory(stats);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=statistiques_cours_enseignant.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<Resource> exportPdf(
            @RequestParam(required = false) Long coursId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            Authentication auth
    ) {
        Enseignant enseignant = getEnseignantConnecte(auth);
        List<TeacherSeanceStatsDTO> stats = statistiquesService.getTeacherSeanceStatsList(
                enseignant.getId(), coursId, dateDebut, dateFin
        );
        String teacherName = enseignant.getPrenom() + " " + enseignant.getNom();
        ByteArrayInputStream in = PDFExportUtils.exportTeacherHistoryPdf(stats, teacherName);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=statistiques_cours_enseignant.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(in));
    }
}
