package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.service.impl;

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
import java.util.List;
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
