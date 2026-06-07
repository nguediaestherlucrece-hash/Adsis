package com.ISI.Appel_classe.Gestion_des_statistiques_de_cours.service.impl;

import com.ISI.Appel_classe.Gestion_des_statistiques_de_cours.dto.*;
import com.ISI.Appel_classe.Gestion_des_statistiques_de_cours.repository.PresenceStatsRepository;
import com.ISI.Appel_classe.Gestion_des_statistiques_de_cours.service.serviceInterface.StatistiquesService;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.CoursUE;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.Seance;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.repository.CoursUERepository;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.repository.SeanceRepository;
import com.ISI.Appel_classe.Gestion_des_etudiants.model.Classe;
import com.ISI.Appel_classe.Gestion_des_etudiants.model.Etudiant;
import com.ISI.Appel_classe.Gestion_des_etudiants.repository.ClasseRepository;
import com.ISI.Appel_classe.Gestion_des_etudiants.repository.EtudiantRepository;
import com.ISI.Appel_classe.Gestion_des_justificatifs.enums.StatutValidation;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Presence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatistiqueServiceImpl implements StatistiquesService {

    private final PresenceStatsRepository presenceStatsRepository;
    private final EtudiantRepository etudiantRepository;
    private final ClasseRepository classeRepository;
    private final CoursUERepository coursUERepository;
    private final SeanceRepository seanceRepository;

    @Override
    public double getHeuresEffectueesCours(Long coursId) {
        CoursUE cours = coursUERepository.findById(coursId)
                .orElseThrow(() -> new RuntimeException("Cours non trouvé : " + coursId));

        // Somme des durées des séances dont le statut est true (séance effectuée/clôturée)
        return cours.getSeances().stream()
                .filter(Seance::isStatut)
                .mapToDouble(this::getSeanceDuration)
                .sum();
    }

    @Override
    public boolean isMatiereAtteintVolumeHoraire(Long coursId) {
        CoursUE cours = coursUERepository.findById(coursId)
                .orElseThrow(() -> new RuntimeException("Cours non trouvé : " + coursId));

        double effectue = getHeuresEffectueesCours(coursId);
        double totalAttribue = (cours.getCredit() != null ? cours.getCredit() : 0) * 15.0;

        return effectue >= totalAttribue;
    }

    @Override
    public Page<PresenceHistoryDTO> getStudentPresenceHistory(
            Long studentId,
            LocalDate date,
            LocalDate dateDebut,
            LocalDate dateFin,
            Long semestreId,
            Long coursId,
            Pageable pageable
    ) {
        Page<Presence> presencesPage = presenceStatsRepository.findFilteredPresences(
                studentId, date, dateDebut, dateFin, semestreId, coursId, pageable
        );

        List<PresenceHistoryDTO> dtos = presencesPage.getContent().stream()
                .map(this::mapToPresenceHistoryDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, presencesPage.getTotalElements());
    }

    @Override
    public StudentPresenceStatsDTO getStudentPresenceStats(Long studentId) {
        List<Presence> presences = presenceStatsRepository.findAllByEtudiantId(studentId);

        double totalHeuresPresentes = 0;
        double totalHeuresAbsentes = 0;
        int totalSeancesAbsentesJustifiees = 0;
        int totalSeancesAbsentesNonJustifiees = 0;
        double totalHeuresAbsentesJustifiees = 0;
        double totalHeuresAbsentesNonJustifiees = 0;
        int totalRetards = 0;

        Map<String, List<Presence>> presencesParMatiere = new HashMap<>();

        for (Presence p : presences) {
            if (p.getSeance() == null || p.getSeance().getCours() == null) continue;

            String codeUE = p.getSeance().getCours().getCodeUE();
            if (codeUE != null) {
                presencesParMatiere.computeIfAbsent(codeUE, k -> new ArrayList<>()).add(p);
            }

            double duration = getSeanceDuration(p.getSeance());
            double presentH = 0;
            double absentH = 0;

            if (p.getPresencesHoraires() != null && !p.getPresencesHoraires().isEmpty()) {
                presentH = p.getHeuresPresentes();
                absentH = p.getHeuresAbsentes();
            } else {
                String statut = p.getStatut();
                if ("présent".equalsIgnoreCase(statut)) {
                    presentH = duration;
                } else if ("retard".equalsIgnoreCase(statut)) {
                    presentH = duration;
                } else if ("absent".equalsIgnoreCase(statut)) {
                    absentH = duration;
                } else if ("présent_partiel".equalsIgnoreCase(statut)) {
                    presentH = duration / 2.0;
                    absentH = duration / 2.0;
                } else {
                    presentH = duration;
                }
            }

            totalHeuresPresentes += presentH;
            totalHeuresAbsentes += absentH;

            boolean isJustified = p.getJustificatif() != null &&
                    StatutValidation.ACCEPTE.equals(p.getJustificatif().getStatutValidation());

            if ("absent".equalsIgnoreCase(p.getStatut()) || "présent_partiel".equalsIgnoreCase(p.getStatut()) || absentH > 0) {
                if (isJustified) {
                    totalSeancesAbsentesJustifiees++;
                    totalHeuresAbsentesJustifiees += absentH;
                } else {
                    totalSeancesAbsentesNonJustifiees++;
                    totalHeuresAbsentesNonJustifiees += absentH;
                }
            }

            if ("retard".equalsIgnoreCase(p.getStatut()) || p.isRetard()) {
                totalRetards++;
            }
        }

        double totalHeures = totalHeuresPresentes + totalHeuresAbsentes;
        double tauxPresenceGlobal = totalHeures > 0 ? (totalHeuresPresentes / totalHeures) * 100.0 : 100.0;

        Map<String, Double> tauxParMatiere = new HashMap<>();
        for (Map.Entry<String, List<Presence>> entry : presencesParMatiere.entrySet()) {
            double keyPresent = 0;
            double keyTotal = 0;
            for (Presence p : entry.getValue()) {
                double duration = getSeanceDuration(p.getSeance());
                if (p.getPresencesHoraires() != null && !p.getPresencesHoraires().isEmpty()) {
                    keyPresent += p.getHeuresPresentes();
                    keyTotal += (p.getHeuresPresentes() + p.getHeuresAbsentes());
                } else {
                    String statut = p.getStatut();
                    if ("présent".equalsIgnoreCase(statut) || "retard".equalsIgnoreCase(statut)) {
                        keyPresent += duration;
                    } else if ("présent_partiel".equalsIgnoreCase(statut)) {
                        keyPresent += duration / 2.0;
                    }
                    keyTotal += duration;
                }
            }
            double taux = keyTotal > 0 ? (keyPresent / keyTotal) * 100.0 : 100.0;
            tauxParMatiere.put(entry.getKey(), taux);
        }

        // Lateness history
        List<PresenceHistoryDTO> retardsList = presences.stream()
                .filter(p -> "retard".equalsIgnoreCase(p.getStatut()) || p.isRetard())
                .sorted(Comparator.comparing(
                        (Presence p) -> p.getSeance() != null && p.getSeance().getDate() != null ? p.getSeance().getDate() : LocalDate.MIN,
                        Comparator.reverseOrder()
                ))
                .map(this::mapToPresenceHistoryDTO)
                .collect(Collectors.toList());

        return StudentPresenceStatsDTO.builder()
                .tauxPresenceGlobal(tauxPresenceGlobal)
                .totalSeancesAbsentesJustifiees(totalSeancesAbsentesJustifiees)
                .totalSeancesAbsentesNonJustifiees(totalSeancesAbsentesNonJustifiees)
                .totalHeuresAbsentesJustifiees(totalHeuresAbsentesJustifiees)
                .totalHeuresAbsentesNonJustifiees(totalHeuresAbsentesNonJustifiees)
                .totalRetards(totalRetards)
                .tauxPresenceParMatiere(tauxParMatiere)
                .historiqueRetards(retardsList)
                .build();
    }

    @Override
    public List<CourseUEMinusStatsDTO> getStudentPenalites(Long studentId) {
        Etudiant etudiant = etudiantRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé : " + studentId));

        if (etudiant.getClasse() == null) return Collections.emptyList();

        List<CoursUE> matieres = coursUERepository.findBySpecialiteId(etudiant.getClasse().getSpecialite().getId());
        List<Presence> presences = presenceStatsRepository.findAllByEtudiantId(studentId);

        List<CourseUEMinusStatsDTO> stats = new ArrayList<>();

        for (CoursUE m : matieres) {
            double effectuees = getHeuresEffectueesCours(m.getId());
            double totalAttribue = (m.getCredit() != null ? m.getCredit() : 0) * 15.0;

            // Calcul des heures d'absence injustifiée de l'étudiant dans cette matière
            double heuresAbsenceInjustifiee = 0;
            for (Presence p : presences) {
                if (p.getSeance() != null && m.getId().equals(p.getSeance().getCours().getId())) {
                    boolean isJustified = p.getJustificatif() != null &&
                            StatutValidation.ACCEPTE.equals(p.getJustificatif().getStatutValidation());
                    if (!isJustified) {
                        double duration = getSeanceDuration(p.getSeance());
                        if (p.getPresencesHoraires() != null && !p.getPresencesHoraires().isEmpty()) {
                            heuresAbsenceInjustifiee += p.getHeuresAbsentes();
                        } else {
                            String statut = p.getStatut();
                            if ("absent".equalsIgnoreCase(statut)) {
                                heuresAbsenceInjustifiee += duration;
                            } else if ("présent_partiel".equalsIgnoreCase(statut)) {
                                heuresAbsenceInjustifiee += duration / 2.0;
                            }
                        }
                    }
                }
            }

            // Exceeds 10% limit for CC minus
            boolean aMalus = effectuees > 0 && (heuresAbsenceInjustifiee > (0.10 * effectuees));
            int pointsMalus = aMalus ? 1 : 0;

            // Exceeds 33% limit for SN admission
            boolean nonAdmisSN = effectuees > 0 && (heuresAbsenceInjustifiee > (0.33 * effectuees));
            String admisSN = nonAdmisSN ? "sn non" : "sn oui";

            stats.add(CourseUEMinusStatsDTO.builder()
                    .coursId(m.getId())
                    .codeUE(m.getCodeUE())
                    .intitule(m.getIntitule())
                    .credit(m.getCredit())
                    .volumeHoraireSemestre(totalAttribue)
                    .heuresEffectuees(effectuees)
                    .heuresAbsenceInjustifiee(heuresAbsenceInjustifiee)
                    .atteintTotaliteHeures(effectuees >= totalAttribue)
                    .aMalusCC(aMalus)
                    .pointsMalusCC(pointsMalus)
                    .admisSN(admisSN)
                    .build());
        }

        return stats;
    }

    @Override
    public Page<TeacherSeanceStatsDTO> getTeacherSeanceStats(
            Long teacherId,
            Long coursId,
            LocalDate dateDebut,
            LocalDate dateFin,
            Pageable pageable
    ) {
        Page<Seance> seancesPage = presenceStatsRepository.findTeacherSeances(
                teacherId, coursId, dateDebut, dateFin, pageable
        );

        List<TeacherSeanceStatsDTO> dtos = seancesPage.getContent().stream()
                .map(this::mapToTeacherSeanceStatsDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, seancesPage.getTotalElements());
    }

    @Override
    public List<TeacherSeanceStatsDTO> getTeacherSeanceStatsList(
            Long teacherId,
            Long coursId,
            LocalDate dateDebut,
            LocalDate dateFin
    ) {
        List<Seance> seances = presenceStatsRepository.findTeacherSeancesList(
                teacherId, coursId, dateDebut, dateFin
        );

        return seances.stream()
                .map(this::mapToTeacherSeanceStatsDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClassAbsenceReportDTO> getClassAbsenceReport(
            Long classeId,
            LocalDate dateDebut,
            LocalDate dateFin,
            Long semestreId
    ) {
        Classe classe = classeRepository.findById(classeId)
                .orElseThrow(() -> new RuntimeException("Classe non trouvée : " + classeId));

        List<Presence> classPresences = presenceStatsRepository.findClassPresences(
                classeId, dateDebut, dateFin, semestreId
        );

        // Group presences by student ID
        Map<Long, List<Presence>> presencesParEtudiant = classPresences.stream()
                .collect(Collectors.groupingBy(p -> p.getEtudiant().getId()));

        List<ClassAbsenceReportDTO> report = new ArrayList<>();

        for (Etudiant e : classe.getEtudiants()) {
            List<Presence> studentPresences = presencesParEtudiant.getOrDefault(e.getId(), Collections.emptyList());

            Map<Long, Double> heuresAbsencesParCours = new HashMap<>();
            Map<Long, Double> heuresJustifieesParCours = new HashMap<>();

            for (Presence p : studentPresences) {
                if (p.getSeance() == null || p.getSeance().getCours() == null) continue;
                Long coursId = p.getSeance().getCours().getId();

                double duration = getSeanceDuration(p.getSeance());
                double absentH = 0;

                if (p.getPresencesHoraires() != null && !p.getPresencesHoraires().isEmpty()) {
                    absentH = p.getHeuresAbsentes();
                } else {
                    String statut = p.getStatut();
                    if ("absent".equalsIgnoreCase(statut)) {
                        absentH = duration;
                    } else if ("présent_partiel".equalsIgnoreCase(statut)) {
                        absentH = duration / 2.0;
                    }
                }

                if (absentH > 0) {
                    boolean isJustified = p.getJustificatif() != null &&
                            StatutValidation.ACCEPTE.equals(p.getJustificatif().getStatutValidation());

                    heuresAbsencesParCours.put(coursId, heuresAbsencesParCours.getOrDefault(coursId, 0.0) + absentH);
                    if (isJustified) {
                        heuresJustifieesParCours.put(coursId, heuresJustifieesParCours.getOrDefault(coursId, 0.0) + absentH);
                    }
                }
            }

            report.add(ClassAbsenceReportDTO.builder()
                    .etudiantId(e.getId())
                    .matricule(e.getMatricule())
                    .nom(e.getNom())
                    .prenom(e.getPrenom())
                    .heuresAbsencesParCours(heuresAbsencesParCours)
                    .heuresJustifieesParCours(heuresJustifieesParCours)
                    .build());
        }

        return report;
    }

    @Override
    public List<ClassMinusReportDTO> getClassMinusReport(Long classeId) {
        Classe classe = classeRepository.findById(classeId)
                .orElseThrow(() -> new RuntimeException("Classe non trouvée : " + classeId));

        List<CoursUE> matieres = coursUERepository.findBySpecialiteId(classe.getSpecialite().getId());
        List<ClassMinusReportDTO> report = new ArrayList<>();

        for (Etudiant e : classe.getEtudiants()) {
            List<CourseUEMinusStatsDTO> studentPenalties = getStudentPenalites(e.getId());

            Map<Long, Double> heuresInjustifiees = new HashMap<>();
            Map<Long, Double> heuresEffectuees = new HashMap<>();
            Map<Long, Boolean> aMalus = new HashMap<>();

            for (CourseUEMinusStatsDTO penalty : studentPenalties) {
                heuresInjustifiees.put(penalty.getCoursId(), penalty.getHeuresAbsenceInjustifiee());
                heuresEffectuees.put(penalty.getCoursId(), penalty.getHeuresEffectuees());
                aMalus.put(penalty.getCoursId(), penalty.isAMalusCC());
            }

            report.add(ClassMinusReportDTO.builder()
                    .etudiantId(e.getId())
                    .matricule(e.getMatricule())
                    .nom(e.getNom())
                    .prenom(e.getPrenom())
                    .heuresInjustifieesParCours(heuresInjustifiees)
                    .heuresEffectueesParCours(heuresEffectuees)
                    .aMalusParCours(aMalus)
                    .build());
        }

        return report;
    }

    @Override
    public List<ClassSNAdmissionReportDTO> getClassSNAdmissionReport(Long classeId) {
        Classe classe = classeRepository.findById(classeId)
                .orElseThrow(() -> new RuntimeException("Classe non trouvée : " + classeId));

        List<ClassSNAdmissionReportDTO> report = new ArrayList<>();

        for (Etudiant e : classe.getEtudiants()) {
            List<CourseUEMinusStatsDTO> studentPenalties = getStudentPenalites(e.getId());

            Map<Long, String> admissionSN = new HashMap<>();

            for (CourseUEMinusStatsDTO penalty : studentPenalties) {
                admissionSN.put(penalty.getCoursId(), penalty.getAdmisSN());
            }

            report.add(ClassSNAdmissionReportDTO.builder()
                    .etudiantId(e.getId())
                    .matricule(e.getMatricule())
                    .nom(e.getNom())
                    .prenom(e.getPrenom())
                    .admissionSNParCours(admissionSN)
                    .build());
        }

        return report;
    }

    // ═══ HELPERS ══════════════════════════════════════════════════════════════

    private double getSeanceDuration(Seance s) {
        if (s == null || s.getHeureDebut() == null || s.getHeureFin() == null) {
            return 0.0;
        }
        Duration duration = Duration.between(s.getHeureDebut(), s.getHeureFin());
        return duration.toMinutes() / 60.0;
    }

    private PresenceHistoryDTO mapToPresenceHistoryDTO(Presence p) {
        if (p == null) return null;

        double duration = getSeanceDuration(p.getSeance());
        double presentH = 0;
        double absentH = 0;

        if (p.getPresencesHoraires() != null && !p.getPresencesHoraires().isEmpty()) {
            presentH = p.getHeuresPresentes();
            absentH = p.getHeuresAbsentes();
        } else {
            String statut = p.getStatut();
            if ("présent".equalsIgnoreCase(statut) || "retard".equalsIgnoreCase(statut)) {
                presentH = duration;
            } else if ("absent".equalsIgnoreCase(statut)) {
                absentH = duration;
            } else if ("présent_partiel".equalsIgnoreCase(statut)) {
                presentH = duration / 2.0;
                absentH = duration / 2.0;
            } else {
                presentH = duration;
            }
        }

        boolean justifiee = p.getJustificatif() != null &&
                StatutValidation.ACCEPTE.equals(p.getJustificatif().getStatutValidation());

        return PresenceHistoryDTO.builder()
                .presenceId(p.getId())
                .date(p.getSeance() != null ? p.getSeance().getDate() : null)
                .heureDebut(p.getSeance() != null && p.getSeance().getHeureDebut() != null ? p.getSeance().getHeureDebut().toString() : "")
                .heureFin(p.getSeance() != null && p.getSeance().getHeureFin() != null ? p.getSeance().getHeureFin().toString() : "")
                .matiereIntitule(p.getSeance() != null && p.getSeance().getCours() != null ? p.getSeance().getCours().getIntitule() : "Événement")
                .matiereCode(p.getSeance() != null && p.getSeance().getCours() != null ? p.getSeance().getCours().getCodeUE() : "")
                .statut(p.getStatut())
                .heuresPresentes(presentH)
                .heuresAbsentes(absentH)
                .justifiee(justifiee)
                .justificatifStatut(p.getJustificatif() != null ? p.getJustificatif().getStatutValidation().name() : null)
                .justificatifMotif(p.getJustificatif() != null ? p.getJustificatif().getMotif() : null)
                .build();
    }

    private TeacherSeanceStatsDTO mapToTeacherSeanceStatsDTO(Seance s) {
        if (s == null) return null;

        int totalEtudiants = s.getPresences() != null ? s.getPresences().size() : 0;
        int presents = 0;
        int absentsJustifies = 0;
        int absentsNonJustifies = 0;
        int retards = 0;

        if (s.getPresences() != null) {
            for (Presence p : s.getPresences()) {
                String statut = p.getStatut();
                boolean isJust = p.getJustificatif() != null &&
                        StatutValidation.ACCEPTE.equals(p.getJustificatif().getStatutValidation());

                if ("absent".equalsIgnoreCase(statut)) {
                    if (isJust) absentsJustifies++;
                    else absentsNonJustifies++;
                } else if ("retard".equalsIgnoreCase(statut) || p.isRetard()) {
                    presents++;
                    retards++;
                } else if ("présent_partiel".equalsIgnoreCase(statut)) {
                    presents++;
                    if (p.getHeuresAbsentes() > 0) {
                        if (isJust) absentsJustifies++;
                        else absentsNonJustifies++;
                    }
                } else {
                    presents++;
                }
            }
        }

        // Fallback for totalEtudiants if presence list is empty but class exists
        if (totalEtudiants == 0 && s.getEmploiDuTemps() != null && s.getEmploiDuTemps().getClasse() != null) {
            totalEtudiants = s.getEmploiDuTemps().getClasse().getEtudiants().size();
        }

        double tauxPresence = totalEtudiants > 0 ? ((double) presents / totalEtudiants) * 100.0 : 100.0;

        return TeacherSeanceStatsDTO.builder()
                .seanceId(s.getId())
                .date(s.getDate())
                .heureDebut(s.getHeureDebut() != null ? s.getHeureDebut().toString() : "")
                .heureFin(s.getHeureFin() != null ? s.getHeureFin().toString() : "")
                .codeUE(s.getCours() != null ? s.getCours().getCodeUE() : "")
                .intitule(s.getCours() != null ? s.getCours().getIntitule() : "Événement")
                .nomClasse(s.getEmploiDuTemps() != null && s.getEmploiDuTemps().getClasse() != null ? s.getEmploiDuTemps().getClasse().getNomClasse() : "—")
                .totalEtudiants(totalEtudiants)
                .presents(presents)
                .absentsJustifies(absentsJustifies)
                .absentsNonJustifies(absentsNonJustifies)
                .retards(retards)
                .tauxPresence(tauxPresence)
                .build();
    }
}
