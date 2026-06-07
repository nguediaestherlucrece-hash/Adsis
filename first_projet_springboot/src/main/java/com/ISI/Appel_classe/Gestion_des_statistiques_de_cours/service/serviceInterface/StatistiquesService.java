package com.ISI.Appel_classe.Gestion_des_statistiques_de_cours.service.serviceInterface;

import com.ISI.Appel_classe.Gestion_des_statistiques_de_cours.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface StatistiquesService {

    /**
     * Calcule le nombre d'heures effectuées d'une matière/coursUE.
     */
    double getHeuresEffectueesCours(Long coursId);

    /**
     * Détermine si une matière a atteint la totalité des heures attribuées (nombre_credit * 15h).
     */
    boolean isMatiereAtteintVolumeHoraire(Long coursId);

    /**
     * Historique de présence filtré et détaillé pour un étudiant (par jour, semaine, semestre, UE) paginé.
     */
    Page<PresenceHistoryDTO> getStudentPresenceHistory(
            Long studentId,
            LocalDate date,
            LocalDate dateDebut,
            LocalDate dateFin,
            Long semestreId,
            Long coursId,
            Pageable pageable
    );

    /**
     * Statistiques personnelles de l'étudiant (taux de présence global, par matière, absences justifiées vs non-justifiées, retards).
     */
    StudentPresenceStatsDTO getStudentPresenceStats(Long studentId);

    /**
     * Récupère la liste des matières avec le malus sur la note du CC et l'état d'admission à la Session Normale (SN).
     */
    List<CourseUEMinusStatsDTO> getStudentPenalites(Long studentId);

    /**
     * Historique d'appel des cours pour un enseignant (statistiques par séance, UE, période) paginé.
     */
    Page<TeacherSeanceStatsDTO> getTeacherSeanceStats(
            Long teacherId,
            Long coursId,
            LocalDate dateDebut,
            LocalDate dateFin,
            Pageable pageable
    );

    /**
     * Liste complète (non paginée) pour l'export des statistiques d'enseignant.
     */
    List<TeacherSeanceStatsDTO> getTeacherSeanceStatsList(
            Long teacherId,
            Long coursId,
            LocalDate dateDebut,
            LocalDate dateFin
    );

    /**
     * Récupère les informations d'absence et heures justifiées d'une classe par matière (export 1 assistant).
     */
    List<ClassAbsenceReportDTO> getClassAbsenceReport(
            Long classeId,
            LocalDate dateDebut,
            LocalDate dateFin,
            Long semestreId
    );

    /**
     * Récupère les informations de malus CC pour chaque étudiant d'une classe (export 2 assistant).
     */
    List<ClassMinusReportDTO> getClassMinusReport(Long classeId);

    /**
     * Récupère l'admission à la SN par matière pour chaque étudiant d'une classe (export 3 assistant).
     */
    List<ClassSNAdmissionReportDTO> getClassSNAdmissionReport(Long classeId);
}
