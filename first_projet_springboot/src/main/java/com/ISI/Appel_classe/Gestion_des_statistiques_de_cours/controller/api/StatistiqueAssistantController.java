package com.ISI.Appel_classe.Gestion_des_statistiques_de_cours.controller.api;

import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.CoursUE;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.Semestre;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.repository.CoursUERepository;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.repository.SemestreRepository;
import com.ISI.Appel_classe.Gestion_des_etudiants.model.Classe;
import com.ISI.Appel_classe.Gestion_des_etudiants.repository.ClasseRepository;
import com.ISI.Appel_classe.Gestion_des_statistiques_de_cours.dto.*;
import com.ISI.Appel_classe.Gestion_des_statistiques_de_cours.service.serviceInterface.StatistiquesService;
import com.ISI.Appel_classe.Gestion_des_statistiques_de_cours.util.ExcelExportUtils;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/statistiques/assistant")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ASSISTANT_PEDAGOGIQUE')")
public class StatistiqueAssistantController {

    private final StatistiquesService statistiquesService;
    private final ClasseRepository classeRepository;
    private final CoursUERepository coursUERepository;
    private final SemestreRepository semestreRepository;

    @GetMapping("/export/absences")
    public ResponseEntity<Resource> exportAbsences(
            @RequestParam Long classeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestParam(required = false) Long semestreId
    ) {
        Classe classe = classeRepository.findById(classeId)
                .orElseThrow(() -> new ResourceNotFoundException("Classe non trouvée avec l'id : " + classeId));

        List<CoursUE> subjects = coursUERepository.findBySpecialiteId(classe.getSpecialite().getId());
        List<ClassAbsenceReportDTO> report = statistiquesService.getClassAbsenceReport(classeId, dateDebut, dateFin, semestreId);

        String periodName = "Toutes périodes";
        if (semestreId != null) {
            Semestre sem = semestreRepository.findById(semestreId).orElse(null);
            if (sem != null) periodName = sem.getNomSemestre();
        } else if (dateDebut != null && dateFin != null) {
            periodName = "Du " + dateDebut + " au " + dateFin;
        }

        ByteArrayInputStream in = ExcelExportUtils.exportClassAbsenceReport(report, subjects, classe.getNomClasse(), periodName);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=absences_classe_" + classe.getNomClasse() + ".xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @GetMapping("/export/malus")
    public ResponseEntity<Resource> exportMalus(@RequestParam Long classeId) {
        Classe classe = classeRepository.findById(classeId)
                .orElseThrow(() -> new ResourceNotFoundException("Classe non trouvée avec l'id : " + classeId));

        List<CoursUE> subjects = coursUERepository.findBySpecialiteId(classe.getSpecialite().getId());
        List<ClassMinusReportDTO> report = statistiquesService.getClassMinusReport(classeId);

        ByteArrayInputStream in = ExcelExportUtils.exportClassMinusReport(report, subjects, classe.getNomClasse());

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=malus_cc_classe_" + classe.getNomClasse() + ".xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @GetMapping("/export/sn")
    public ResponseEntity<Resource> exportSN(@RequestParam Long classeId) {
        Classe classe = classeRepository.findById(classeId)
                .orElseThrow(() -> new ResourceNotFoundException("Classe non trouvée avec l'id : " + classeId));

        List<CoursUE> subjects = coursUERepository.findBySpecialiteId(classe.getSpecialite().getId());
        List<ClassSNAdmissionReportDTO> report = statistiquesService.getClassSNAdmissionReport(classeId);

        ByteArrayInputStream in = ExcelExportUtils.exportClassSNAdmissionReport(report, subjects, classe.getNomClasse());

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=admissions_sn_classe_" + classe.getNomClasse() + ".xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }
}
