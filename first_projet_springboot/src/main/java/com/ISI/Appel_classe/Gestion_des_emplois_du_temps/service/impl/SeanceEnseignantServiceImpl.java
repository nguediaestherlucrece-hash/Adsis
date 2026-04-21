package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.service.impl;

import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.response.DashboardEnseignantDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.response.SeanceEnseignantDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.EmploiDuTemps;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.Seance;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.repository.SeanceRepository;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.service.serviceInterface.SeanceEnseignantService;
import com.ISI.Appel_classe.Gestion_des_etudiants.model.Classe;
import com.ISI.Appel_classe.Gestion_des_etudiants.repository.EtudiantRepository;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Appel;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.PresenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeanceEnseignantServiceImpl implements SeanceEnseignantService {
    
    private final SeanceRepository seanceRepository;
    private final PresenceRepository presenceRepository;
    private final EtudiantRepository etudiantRepository;
    
    @Override
    public List<SeanceEnseignantDTO> getSeancesEnseignantParDate(Long enseignantId, LocalDate date) {
        List<Seance> seances = seanceRepository.findByEnseignantIdAndDate(enseignantId, date);
        return seances.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<SeanceEnseignantDTO> getSeancesEnseignantSemaine(Long enseignantId) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);
        
        return getSeancesEnseignantPeriode(enseignantId, startOfWeek, endOfWeek);
    }
    
    @Override
    public List<SeanceEnseignantDTO> getSeancesEnseignantPeriode(Long enseignantId, LocalDate dateDebut, LocalDate dateFin) {
        List<Seance> seances = seanceRepository.findByEnseignantIdAndDateBetween(enseignantId, dateDebut, dateFin);
        return seances.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Override
    public DashboardEnseignantDTO getDashboardData(Long enseignantId) {
        DashboardEnseignantDTO dashboard = new DashboardEnseignantDTO();
        
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);

        // 1. Récupérer les séances de la semaine
        List<SeanceEnseignantDTO> seancesSemaine = getSeancesEnseignantPeriode(enseignantId, startOfWeek, endOfWeek);

        // 2. Calculer les heures de la semaine
        int heuresSemaine = 0;
        for (SeanceEnseignantDTO s : seancesSemaine) {
            if (s.getHeureDebut() != null && s.getHeureFin() != null) {
                heuresSemaine += (int) ChronoUnit.HOURS.between(s.getHeureDebut(), s.getHeureFin());
            }
        }
        dashboard.setHeuresSemaine(heuresSemaine);

        // 3. Cours du jour
        List<SeanceEnseignantDTO> coursJour = seancesSemaine.stream()
            .filter(s -> s.getDate() != null && s.getDate().equals(today))
            .sorted(Comparator.comparing(SeanceEnseignantDTO::getHeureDebut, Comparator.nullsLast(Comparator.naturalOrder())))
            .collect(Collectors.toList());
        dashboard.setCoursAujourdhui(coursJour.size());
        dashboard.setCoursJour(coursJour);

        // 4. Prochain cours : la première séance du jour dont l'heure de fin est dans le futur
        LocalTime now = LocalTime.now();
        SeanceEnseignantDTO prochainCours = coursJour.stream()
            .filter(s -> s.getHeureFin() != null && s.getHeureFin().isAfter(now))
            .findFirst()
            .orElse(null);
        dashboard.setProchainCours(prochainCours);

        // 5. Étudiants total (distinct par classe)
        Set<Long> classeIds = new HashSet<>();
        for (SeanceEnseignantDTO s : seancesSemaine) {
            if (s.getClasseId() != null) {
                classeIds.add(s.getClasseId());
            }
        }
        int etudiantsTotal = 0;
        for (Long classeId : classeIds) {
            etudiantsTotal += etudiantRepository.findByClasseActuelleId(classeId).size();
        }
        dashboard.setEtudiantsTotal(etudiantsTotal);

        // 6. Taux de présence moyen (sur les séances avec appels clôturés)
        long totalPresents = 0;
        long totalEnregistres = 0;
        for (SeanceEnseignantDTO s : seancesSemaine) {
            if (s.getAppelId() != null && "clôturé".equals(s.getAppelStatut())) {
                int presents = (s.getNombrePresents() != null ? s.getNombrePresents() : 0);
                int retards = (s.getNombreRetards() != null ? s.getNombreRetards() : 0);
                int absents = (s.getNombreAbsents() != null ? s.getNombreAbsents() : 0);
                totalPresents += presents + retards; // retards comptent comme présents partiels
                totalEnregistres += presents + retards + absents;
            }
        }
        double tauxPresence = totalEnregistres > 0 
            ? Math.round((double) totalPresents / totalEnregistres * 1000.0) / 10.0 
            : 0.0;
        dashboard.setTauxPresence(tauxPresence);

        return dashboard;
    }
    
    private SeanceEnseignantDTO convertToDTO(Seance seance) {
        SeanceEnseignantDTO dto = new SeanceEnseignantDTO();
        
        dto.setId(seance.getId());
        dto.setIdSeance(seance.getIdSeance());
        dto.setDate(seance.getDate());
        dto.setHeureDebut(seance.getHeureDebut());
        dto.setHeureFin(seance.getHeureFin());
        dto.setStatut(seance.isStatut());
        dto.setCouleur(seance.getCouleur());
        
        // Informations du cours
        if (seance.getCours() != null) {
            dto.setCoursId(seance.getCours().getId());
            dto.setCoursCode(seance.getCours().getCodeUE());
            dto.setCoursIntitule(seance.getCours().getIntitule());
        }
        
        // Informations de la classe
        EmploiDuTemps emploiDuTemps = seance.getEmploiDuTemps();
        if (emploiDuTemps != null && emploiDuTemps.getClasse() != null) {
            Classe classe = emploiDuTemps.getClasse();
            dto.setClasseId(classe.getId());
            dto.setClasseNom(classe.getNomClasse());
            
            // Nombre total d'étudiants
            int nombreTotal = etudiantRepository.findByClasseActuelleId(classe.getId()).size();
            dto.setNombreTotal(nombreTotal);
        }
        
        // Informations de l'appel (dernier appel de la séance)
        if (!seance.getAppels().isEmpty()) {
            Appel dernierAppel = seance.getAppels().stream()
                .max((a1, a2) -> a1.getDateHeureDebut().compareTo(a2.getDateHeureDebut()))
                .orElse(null);
            
            if (dernierAppel != null) {
                dto.setAppelId(dernierAppel.getId());
                dto.setAppelStatut(dernierAppel.getStatut());
                
                // Statistiques de présence
                long nombrePresents = presenceRepository.countByAppelIdAndStatut(dernierAppel.getId(), "présent");
                long nombreAbsents = presenceRepository.countByAppelIdAndStatut(dernierAppel.getId(), "absent");
                long nombreRetards = presenceRepository.countByAppelIdAndStatut(dernierAppel.getId(), "retard");
                
                dto.setNombrePresents((int) nombrePresents);
                dto.setNombreAbsents((int) nombreAbsents);
                dto.setNombreRetards((int) nombreRetards);
            }
        }
        
        return dto;
    }
}
