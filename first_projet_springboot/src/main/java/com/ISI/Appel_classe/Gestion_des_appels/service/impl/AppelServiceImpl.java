package com.ISI.Appel_classe.Gestion_des_appels.service.impl;

import com.ISI.Appel_classe.Gestion_des_appels.dto.request.CreneauPresenceDTO;
import com.ISI.Appel_classe.Gestion_des_appels.dto.request.DemarrerAppelRequest;
import com.ISI.Appel_classe.Gestion_des_appels.dto.request.EnregistrerPresenceRequest;
import com.ISI.Appel_classe.Gestion_des_appels.dto.request.EnregistrerPresencesBatchRequest;
import com.ISI.Appel_classe.Gestion_des_appels.dto.response.AppelDTO;
import com.ISI.Appel_classe.Gestion_des_appels.dto.response.EtudiantSeanceDTO;
import com.ISI.Appel_classe.Gestion_des_appels.dto.response.PresenceDTO;
import com.ISI.Appel_classe.Gestion_des_appels.repository.AppelRepository;
import com.ISI.Appel_classe.Gestion_des_appels.service.QRCodeService;
import com.ISI.Appel_classe.Gestion_des_appels.service.serviceInterface.AppelService;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.EmploiDuTemps;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.Seance;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.repository.SeanceRepository;
import com.ISI.Appel_classe.Gestion_des_etudiants.model.Classe;
import com.ISI.Appel_classe.Gestion_des_etudiants.model.Etudiant;
import com.ISI.Appel_classe.Gestion_des_etudiants.repository.EtudiantRepository;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.exception.BadRequestException;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.exception.ResourceNotFoundException;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Appel;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Presence;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.PresenceHoraire;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.PresenceHoraireRepository;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.PresenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AppelServiceImpl implements AppelService {
    
    private final AppelRepository appelRepository;
    private final SeanceRepository seanceRepository;
    private final PresenceRepository presenceRepository;
    private final PresenceHoraireRepository presenceHoraireRepository;
    private final EtudiantRepository etudiantRepository;
    private final QRCodeService qrCodeService;
    
    // Cache temporaire pour les codes PIN (en production, utiliser Redis)
    private Map<Long, PINData> pinCache = new HashMap<>();
    
    @Override
    public AppelDTO demarrerAppel(DemarrerAppelRequest request) {
        // Vérifier que la séance existe
        Seance seance = seanceRepository.findById(request.getSeanceId())
            .orElseThrow(() -> new ResourceNotFoundException("Séance non trouvée"));
        
        // Vérifier qu'il n'y a pas déjà un appel ouvert pour cette séance
        Optional<Appel> appelExistant = appelRepository.findAppelOuvertBySeanceId(request.getSeanceId());
        if (appelExistant.isPresent()) {
            throw new BadRequestException("Un appel est déjà ouvert pour cette séance");
        }
        
        // Créer le nouvel appel
        Appel appel = new Appel();
        appel.setIdAppel(UUID.randomUUID().toString());
        appel.setSeance(seance);
        appel.setStatut("ouvert");
        appel.setDateHeureDebut(LocalDateTime.now());
        
        appel = appelRepository.save(appel);
        
        // Générer QR code et/ou PIN selon le mode
        AppelDTO appelDTO = convertToDTO(appel);
        
        if (request.getModeAppel() == null || request.getModeAppel().equalsIgnoreCase("QR")) {
            String qrContent = generateQRContent(appel);
            String qrCodeBase64 = qrCodeService.generateQRCodeBase64(qrContent);
            appelDTO.setQrCodeBase64(qrCodeBase64);
        }
        
        if (request.getModeAppel() != null && request.getModeAppel().equalsIgnoreCase("PIN")) {
            String codePIN = qrCodeService.generatePINCode();
            int dureeValidite = request.getDureeValiditePIN() != null ? request.getDureeValiditePIN() : 5;
            LocalDateTime expiration = LocalDateTime.now().plusMinutes(dureeValidite);
            
            // Stocker le PIN en cache
            pinCache.put(appel.getId(), new PINData(codePIN, expiration));
            
            appelDTO.setCodePIN(codePIN);
            appelDTO.setExpirationPIN(expiration);
        }
        
        return appelDTO;
    }
    
    @Override
    public AppelDTO cloturerAppel(Long appelId) {
        Appel appel = appelRepository.findById(appelId)
            .orElseThrow(() -> new ResourceNotFoundException("Appel non trouvé"));
        
        if (!"ouvert".equals(appel.getStatut())) {
            throw new BadRequestException("Cet appel est déjà clôturé");
        }
        
        appel.setStatut("clôturé");
        appel.setDateHeureFin(LocalDateTime.now());
        
        // Marquer comme absents tous les étudiants non enregistrés
        marquerAbsentsNonEnregistres(appel);
        
        appel = appelRepository.save(appel);
        
        // Nettoyer le cache PIN
        pinCache.remove(appelId);
        
        return convertToDTO(appel);
    }
    
    @Override
    public PresenceDTO enregistrerPresence(Long appelId, EnregistrerPresenceRequest request) {
        Appel appel = appelRepository.findById(appelId)
            .orElseThrow(() -> new ResourceNotFoundException("Appel non trouvé"));
        
        if (!"ouvert".equals(appel.getStatut())) {
            throw new BadRequestException("Cet appel est clôturé");
        }
        
        Etudiant etudiant = etudiantRepository.findById(request.getEtudiantId())
            .orElseThrow(() -> new ResourceNotFoundException("Étudiant non trouvé"));
        
        // Vérifier si l'étudiant n'est pas déjà enregistré
        Optional<Presence> presenceExistante = presenceRepository
            .findByAppelIdAndEtudiantId(appelId, request.getEtudiantId());
        
        if (presenceExistante.isPresent()) {
            // Mettre à jour la présence existante
            Presence presence = presenceExistante.get();
            presence.setStatut(request.getStatut());
            presence.setHorodatage(LocalDateTime.now());
            presence.setModeEnregistrement(request.getModeEnregistrement());
            presence.setLocalisation(request.getLocalisation());
            presence.setRetard(request.isRetard());
            
            // Mettre à jour les créneaux horaires si fournis
            if (request.getCreneaux() != null && !request.getCreneaux().isEmpty()) {
                // Supprimer les anciens créneaux
                presenceHoraireRepository.deleteAll(presence.getPresencesHoraires());
                presence.getPresencesHoraires().clear();
                
                // Créer les nouveaux créneaux
                for (CreneauPresenceDTO creneauDTO : request.getCreneaux()) {
                    PresenceHoraire ph = new PresenceHoraire();
                    ph.setHeureDebut(creneauDTO.getHeureDebut());
                    ph.setHeureFin(creneauDTO.getHeureFin());
                    ph.setPresent(creneauDTO.isPresent());
                    ph.setPresence(presence);
                    presence.getPresencesHoraires().add(ph);
                }
                
                // Recalculer le statut
                presence.recalculerStatut();
            }
            
            presence = presenceRepository.save(presence);
            return convertPresenceToDTO(presence);
        }
        
        // Créer une nouvelle présence
        Presence presence = new Presence();
        presence.setIdPresence(UUID.randomUUID().toString());
        presence.setEtudiant(etudiant);
        presence.setSeance(appel.getSeance());
        presence.setAppel(appel);
        presence.setStatut(request.getStatut());
        presence.setHorodatage(LocalDateTime.now());
        presence.setModeEnregistrement(request.getModeEnregistrement());
        presence.setLocalisation(request.getLocalisation());
        presence.setRetard(request.isRetard());
        
        // Créer les créneaux horaires si fournis
        if (request.getCreneaux() != null && !request.getCreneaux().isEmpty()) {
            for (CreneauPresenceDTO creneauDTO : request.getCreneaux()) {
                PresenceHoraire ph = new PresenceHoraire();
                ph.setHeureDebut(creneauDTO.getHeureDebut());
                ph.setHeureFin(creneauDTO.getHeureFin());
                ph.setPresent(creneauDTO.isPresent());
                ph.setPresence(presence);
                presence.getPresencesHoraires().add(ph);
            }
            // Recalculer le statut basé sur les créneaux
            presence.recalculerStatut();
        }
        
        presence = presenceRepository.save(presence);
        
        return convertPresenceToDTO(presence);
    }
    
    @Override
    public List<PresenceDTO> enregistrerPresencesBatch(EnregistrerPresencesBatchRequest request) {
        List<PresenceDTO> presencesEnregistrees = new ArrayList<>();
        
        for (EnregistrerPresenceRequest presenceRequest : request.getPresences()) {
            try {
                PresenceDTO presence = enregistrerPresence(request.getAppelId(), presenceRequest);
                presencesEnregistrees.add(presence);
            } catch (Exception e) {
                // Log l'erreur mais continue avec les autres
                System.err.println("Erreur lors de l'enregistrement de la présence pour l'étudiant " 
                    + presenceRequest.getEtudiantId() + ": " + e.getMessage());
            }
        }
        
        return presencesEnregistrees;
    }
    
    @Override
    @Transactional(readOnly = true)
    public AppelDTO getAppelById(Long appelId) {
        Appel appel = appelRepository.findById(appelId)
            .orElseThrow(() -> new ResourceNotFoundException("Appel non trouvé"));
        
        return convertToDTO(appel);
    }
    
    @Override
    @Transactional(readOnly = true)
    public AppelDTO getAppelOuvertBySeance(Long seanceId) {
        Optional<Appel> appel = appelRepository.findAppelOuvertBySeanceId(seanceId);
        return appel.map(this::convertToDTO).orElse(null);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<EtudiantSeanceDTO> getEtudiantsSeanceAvecStatut(Long seanceId, Long appelId) {
        Seance seance = seanceRepository.findById(seanceId)
            .orElseThrow(() -> new ResourceNotFoundException("Séance non trouvée"));
        
        // Récupérer la classe via l'emploi du temps
        EmploiDuTemps emploiDuTemps = seance.getEmploiDuTemps();
        if (emploiDuTemps == null || emploiDuTemps.getClasse() == null) {
            throw new BadRequestException("Aucune classe associée à cette séance");
        }
        
        Classe classe = emploiDuTemps.getClasse();
        List<Etudiant> etudiants = etudiantRepository.findByClasseActuelleId(classe.getId());
        
        // Récupérer les présences existantes pour cet appel
        Map<Long, Presence> presencesMap = new HashMap<>();
        if (appelId != null) {
            List<Presence> presences = presenceRepository.findByAppelId(appelId);
            presencesMap = presences.stream()
                .collect(Collectors.toMap(p -> p.getEtudiant().getId(), p -> p));
        }
        
        // Générer les créneaux par défaut pour la séance
        List<CreneauPresenceDTO> creneauxDefaut = genererCreneauxSeance(seance);
        
        // Construire la liste des étudiants avec leur statut
        List<EtudiantSeanceDTO> result = new ArrayList<>();
        for (Etudiant etudiant : etudiants) {
            EtudiantSeanceDTO dto = new EtudiantSeanceDTO();
            dto.setId(etudiant.getId());
            dto.setMatricule(etudiant.getMatricule());
            dto.setNom(etudiant.getNom());
            dto.setPrenom(etudiant.getPrenom());
            dto.setEmail(etudiant.getEmail());
            
            Presence presence = presencesMap.get(etudiant.getId());
            if (presence != null) {
                dto.setStatut(presence.getStatut());
                dto.setPresenceId(presence.getId());
                dto.setRetard(presence.isRetard());
                
                // Convertir les créneaux horaires existants
                if (presence.getPresencesHoraires() != null && !presence.getPresencesHoraires().isEmpty()) {
                    dto.setCreneaux(presence.getPresencesHoraires().stream()
                        .map(ph -> {
                            CreneauPresenceDTO c = new CreneauPresenceDTO();
                            c.setHeureDebut(ph.getHeureDebut());
                            c.setHeureFin(ph.getHeureFin());
                            c.setPresent(ph.isPresent());
                            return c;
                        })
                        .collect(Collectors.toList()));
                } else {
                    dto.setCreneaux(creneauxDefaut);
                }
            } else {
                dto.setStatut("non_enregistré");
                dto.setCreneaux(creneauxDefaut);
            }
            
            result.add(dto);
        }
        
        return result;
    }
    
    @Override
    public boolean validerCodePIN(Long appelId, String codePIN) {
        PINData pinData = pinCache.get(appelId);
        
        if (pinData == null) {
            return false;
        }
        
        // Vérifier l'expiration
        if (LocalDateTime.now().isAfter(pinData.getExpiration())) {
            pinCache.remove(appelId);
            return false;
        }
        
        return pinData.getCode().equals(codePIN);
    }
    
    @Override
    public AppelDTO regenererQRCode(Long appelId) {
        Appel appel = appelRepository.findById(appelId)
            .orElseThrow(() -> new ResourceNotFoundException("Appel non trouvé"));
        
        if (!"ouvert".equals(appel.getStatut())) {
            throw new BadRequestException("Impossible de régénérer le QR code pour un appel clôturé");
        }
        
        AppelDTO appelDTO = convertToDTO(appel);
        String qrContent = generateQRContent(appel);
        String qrCodeBase64 = qrCodeService.generateQRCodeBase64(qrContent);
        appelDTO.setQrCodeBase64(qrCodeBase64);
        
        return appelDTO;
    }

    @Override
    public AppelDTO genererPINPourAppel(Long appelId, Integer dureeMinutes) {
        Appel appel = appelRepository.findById(appelId)
            .orElseThrow(() -> new ResourceNotFoundException("Appel non trouvé"));
        
        if (!"ouvert".equals(appel.getStatut())) {
            throw new BadRequestException("Impossible de générer un PIN pour un appel clôturé");
        }

        String codePIN = qrCodeService.generatePINCode();
        int duree = (dureeMinutes != null && dureeMinutes > 0) ? dureeMinutes : 5;
        LocalDateTime expiration = LocalDateTime.now().plusMinutes(duree);

        pinCache.put(appelId, new PINData(codePIN, expiration));

        AppelDTO dto = convertToDTO(appel);
        dto.setCodePIN(codePIN);
        dto.setExpirationPIN(expiration);

        return dto;
    }

    @Override
    public AppelDTO validerQRCode(Long appelId, String qrToken) {
        Appel appel = appelRepository.findById(appelId)
            .orElseThrow(() -> new ResourceNotFoundException("Appel non trouvé"));

        if (!"ouvert".equals(appel.getStatut())) {
            throw new BadRequestException("Cet appel est clôturé");
        }

        // Le QR token contient le format: APPEL:{appelId}:{timestamp}
        // On vérifie que le token correspond bien à cet appel
        if (qrToken == null || !qrToken.startsWith("APPEL:" + appelId + ":")) {
            throw new BadRequestException("QR code invalide pour cet appel");
        }

        return convertToDTO(appel);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PresenceDTO> getPresencesByAppelId(Long appelId) {
        Appel appel = appelRepository.findById(appelId)
            .orElseThrow(() -> new ResourceNotFoundException("Appel non trouvé"));

        List<Presence> presences = presenceRepository.findByAppelId(appelId);
        return presences.stream()
            .map(this::convertPresenceToDTO)
            .collect(Collectors.toList());
    }
    
    // Méthodes utilitaires privées
    
    /**
     * Génère les créneaux d'1 heure à partir des horaires de la séance
     */
    private List<CreneauPresenceDTO> genererCreneauxSeance(Seance seance) {
        List<CreneauPresenceDTO> creneaux = new ArrayList<>();
        
        if (seance.getHeureDebut() == null || seance.getHeureFin() == null) {
            return creneaux;
        }

        LocalTime current = seance.getHeureDebut();
        while (current.isBefore(seance.getHeureFin())) {
            CreneauPresenceDTO creneau = new CreneauPresenceDTO();
            creneau.setHeureDebut(current);
            LocalTime next = current.plusHours(1);
            // Ne pas dépasser l'heure de fin
            if (next.isAfter(seance.getHeureFin())) {
                next = seance.getHeureFin();
            }
            creneau.setHeureFin(next);
            creneau.setPresent(false); // Non enregistré par défaut
            creneaux.add(creneau);
            current = next;
        }
        
        return creneaux;
    }
    
    private void marquerAbsentsNonEnregistres(Appel appel) {
        EmploiDuTemps emploiDuTemps = appel.getSeance().getEmploiDuTemps();
        if (emploiDuTemps == null || emploiDuTemps.getClasse() == null) {
            return;
        }
        
        Classe classe = emploiDuTemps.getClasse();
        List<Etudiant> etudiants = etudiantRepository.findByClasseActuelleId(classe.getId());
        
        // Récupérer les étudiants déjà enregistrés
        List<Presence> presencesExistantes = presenceRepository.findByAppelId(appel.getId());
        Set<Long> etudiantsEnregistres = presencesExistantes.stream()
            .map(p -> p.getEtudiant().getId())
            .collect(Collectors.toSet());

        // Générer les créneaux pour la séance
        List<CreneauPresenceDTO> creneauxSeance = genererCreneauxSeance(appel.getSeance());
        
        // Marquer les absents
        for (Etudiant etudiant : etudiants) {
            if (!etudiantsEnregistres.contains(etudiant.getId())) {
                Presence absence = new Presence();
                absence.setIdPresence(UUID.randomUUID().toString());
                absence.setEtudiant(etudiant);
                absence.setSeance(appel.getSeance());
                absence.setAppel(appel);
                absence.setStatut("absent");
                absence.setHorodatage(LocalDateTime.now());
                absence.setModeEnregistrement("automatique");
                absence.setRetard(false);
                
                // Créer les PresenceHoraire en absent pour chaque créneau
                for (CreneauPresenceDTO creneau : creneauxSeance) {
                    PresenceHoraire ph = new PresenceHoraire();
                    ph.setHeureDebut(creneau.getHeureDebut());
                    ph.setHeureFin(creneau.getHeureFin());
                    ph.setPresent(false);
                    ph.setPresence(absence);
                    absence.getPresencesHoraires().add(ph);
                }
                
                presenceRepository.save(absence);
            }
        }
    }
    
    private String generateQRContent(Appel appel) {
        // Format: APPEL:{appelId}:{timestamp}
        return String.format("APPEL:%d:%d", appel.getId(), System.currentTimeMillis());
    }
    
    private AppelDTO convertToDTO(Appel appel) {
        AppelDTO dto = new AppelDTO();
        dto.setId(appel.getId());
        dto.setIdAppel(appel.getIdAppel());
        dto.setDateHeureDebut(appel.getDateHeureDebut());
        dto.setDateHeureFin(appel.getDateHeureFin());
        dto.setStatut(appel.getStatut());
        dto.setSeanceId(appel.getSeance().getId());
        
        // Ajouter le PIN s'il existe dans le cache
        PINData pinData = pinCache.get(appel.getId());
        if (pinData != null && LocalDateTime.now().isBefore(pinData.getExpiration())) {
            dto.setCodePIN(pinData.getCode());
            dto.setExpirationPIN(pinData.getExpiration());
        }
        
        return dto;
    }
    
    private PresenceDTO convertPresenceToDTO(Presence presence) {
        PresenceDTO dto = new PresenceDTO();
        dto.setId(presence.getId());
        dto.setIdPresence(presence.getIdPresence());
        dto.setHorodatage(presence.getHorodatage());
        dto.setStatut(presence.getStatut());
        dto.setModeEnregistrement(presence.getModeEnregistrement());
        dto.setLocalisation(presence.getLocalisation());
        dto.setRetard(presence.isRetard());
        
        if (presence.getEtudiant() != null) {
            dto.setEtudiantId(presence.getEtudiant().getId());
            dto.setEtudiantNom(presence.getEtudiant().getNom());
            dto.setEtudiantPrenom(presence.getEtudiant().getPrenom());
            dto.setEtudiantMatricule(presence.getEtudiant().getMatricule());
        }
        
        if (presence.getSeance() != null) {
            dto.setSeanceId(presence.getSeance().getId());
        }
        
        if (presence.getAppel() != null) {
            dto.setAppelId(presence.getAppel().getId());
        }

        // Convertir les créneaux horaires
        if (presence.getPresencesHoraires() != null && !presence.getPresencesHoraires().isEmpty()) {
            dto.setCreneaux(presence.getPresencesHoraires().stream()
                .map(ph -> {
                    CreneauPresenceDTO c = new CreneauPresenceDTO();
                    c.setHeureDebut(ph.getHeureDebut());
                    c.setHeureFin(ph.getHeureFin());
                    c.setPresent(ph.isPresent());
                    return c;
                })
                .collect(Collectors.toList()));
            dto.setHeuresPresentes(presence.getHeuresPresentes());
            dto.setHeuresAbsentes(presence.getHeuresAbsentes());
        }
        
        return dto;
    }
    
    // Classe interne pour stocker les données PIN
    private static class PINData {
        private final String code;
        private final LocalDateTime expiration;
        
        public PINData(String code, LocalDateTime expiration) {
            this.code = code;
            this.expiration = expiration;
        }
        
        public String getCode() {
            return code;
        }
        
        public LocalDateTime getExpiration() {
            return expiration;
        }
    }
}
