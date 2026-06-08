package com.ISI.Appel_classe.Gestion_des_appels_enseignant.service.impl;

import com.ISI.Appel_classe.Gestion_des_appels_enseignant.dto.request.CreneauPresenceDTO;
import com.ISI.Appel_classe.Gestion_des_appels_enseignant.dto.request.DemarrerAppelRequest;
import com.ISI.Appel_classe.Gestion_des_appels_enseignant.dto.request.EnregistrerPresenceRequest;
import com.ISI.Appel_classe.Gestion_des_appels_enseignant.dto.request.EnregistrerPresencesBatchRequest;
import com.ISI.Appel_classe.Gestion_des_appels_enseignant.dto.response.AppelDTO;
import com.ISI.Appel_classe.Gestion_des_appels_enseignant.dto.response.EtudiantSeanceDTO;
import com.ISI.Appel_classe.Gestion_des_appels_enseignant.dto.response.PresenceDTO;
import com.ISI.Appel_classe.Gestion_des_appels_enseignant.repository.AppelRepository;
import com.ISI.Appel_classe.Gestion_des_appels_enseignant.util.QRCodeService;
import com.ISI.Appel_classe.Gestion_des_appels_enseignant.service.serviceInterface.AppelService;
import com.ISI.Appel_classe.Gestion_des_appels_etudiant.model.CodePresence;
import com.ISI.Appel_classe.Gestion_des_appels_etudiant.repository.CodePresenceRepository;
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
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Surveillant;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.PresenceHoraireRepository;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.PresenceRepository;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.SurveillantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
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
    private final SurveillantRepository surveillantRepository;
    private final QRCodeService qrCodeService;
    private final CodePresenceRepository codePresenceRepository;

    // Cache temporaire pour les codes PIN (en production, utiliser Redis)
    private final Map<Long, PINData> pinCache = new HashMap<>();

    // =========================================================================
    // SEUILS MÉTIER (relatifs à l'heure de début de SÉANCE)
    // =========================================================================
    /** Minutes après le début de séance au-delà desquelles on est en retard */
    private static final int SEUIL_RETARD_MIN = 10;
    /** Minutes après le début de séance au-delà desquelles le 1er créneau est absent */
    private static final int SEUIL_ABSENT_MIN = 45;

    private boolean isCurrentUserSurveillant() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_SURVEILLANT"));
    }

    private void verifierAccesSurveillant(Seance seance) {
        if (!isCurrentUserSurveillant()) {
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new BadRequestException("Utilisateur non authentifié");
        }

        String email = authentication.getName();
        Surveillant surveillant = surveillantRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Surveillant non trouvé pour l'email: " + email));

        boolean aAccesDirect = seance.getSurveillants().stream()
                .anyMatch(s -> s.getId().equals(surveillant.getId()));

        boolean aAccesFiliere = false;
        if (surveillant.getFiliere() != null && seance.getEmploiDuTemps() != null
                && seance.getEmploiDuTemps().getClasse() != null
                && seance.getEmploiDuTemps().getClasse().getSpecialite() != null
                && seance.getEmploiDuTemps().getClasse().getSpecialite().getNiveau() != null
                && seance.getEmploiDuTemps().getClasse().getSpecialite().getNiveau().getFiliere() != null) {
            aAccesFiliere = surveillant.getFiliere().getId()
                    .equals(seance.getEmploiDuTemps().getClasse().getSpecialite().getNiveau().getFiliere().getId());
        }

        if (!aAccesDirect && !aAccesFiliere) {
            throw new BadRequestException("Vous n'êtes pas autorisé à gérer l'appel de cette séance");
        }
    }

    private Seance getSeanceOrThrow(Long seanceId) {
        return seanceRepository.findById(seanceId)
            .orElseThrow(() -> new ResourceNotFoundException("Séance non trouvée"));
    }

    private Appel getAppelOrThrow(Long appelId) {
        return appelRepository.findById(appelId)
            .orElseThrow(() -> new ResourceNotFoundException("Appel non trouvé"));
    }

    private void verifierAccesSurveillantParSeance(Long seanceId) {
        verifierAccesSurveillant(getSeanceOrThrow(seanceId));
    }

    private void verifierAccesSurveillantParAppel(Long appelId) {
        verifierAccesSurveillant(getAppelOrThrow(appelId).getSeance());
    }

    // =========================================================================
    // DÉMARRER / CLÔTURER
    // =========================================================================

    @Override
    public AppelDTO demarrerAppel(DemarrerAppelRequest request) {
        // Vérifier que la séance existe
        Seance seance = getSeanceOrThrow(request.getSeanceId());
        verifierAccesSurveillant(seance);

        // Vérifier qu'il n'y a pas d'appel ACTUELLEMENT ouvert (un appel clôturé ne bloque pas)
        Optional<Appel> appelOuvert = appelRepository.findAppelOuvertBySeanceId(request.getSeanceId());
        if (appelOuvert.isPresent()) {
            throw new BadRequestException(
                "Un appel est déjà en cours. Clôturez-le avant d'en lancer un nouveau.");
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
            pinCache.put(appel.getId(), new PINData(codePIN, expiration));
            appelDTO.setCodePIN(codePIN);
            appelDTO.setExpirationPIN(expiration);
        }

        return appelDTO;
    }

    @Override
    public AppelDTO cloturerAppel(Long appelId) {
        Appel appel = getAppelOrThrow(appelId);
        verifierAccesSurveillant(appel.getSeance());

        if (!"ouvert".equals(appel.getStatut())) {
            throw new BadRequestException("Cet appel est déjà clôturé");
        }

        appel.setStatut("clôturé");
        appel.setDateHeureFin(LocalDateTime.now());

        // Marquer comme absents tous les étudiants non enregistrés SUR CET APPEL
        marquerAbsentsNonEnregistres(appel);

        appel = appelRepository.save(appel);

        // Nettoyer le cache PIN
        pinCache.remove(appelId);

        return convertToDTO(appel);
    }

    // =========================================================================
    // ENREGISTREMENT DES PRÉSENCES
    // =========================================================================

    @Override
    public PresenceDTO enregistrerPresence(Long appelId, EnregistrerPresenceRequest request) {
        Appel appel = getAppelOrThrow(appelId);
        verifierAccesSurveillant(appel.getSeance());

        if (!"ouvert".equals(appel.getStatut())) {
            throw new BadRequestException("Cet appel est clôturé");
        }

        Etudiant etudiant = etudiantRepository.findById(request.getEtudiantId())
            .orElseThrow(() -> new ResourceNotFoundException("Étudiant non trouvé"));

        // Heure de réponse : celle fournie, sinon maintenant
        LocalDateTime heureReponse = request.getHeureReponse() != null
            ? request.getHeureReponse()
            : LocalDateTime.now();

        // Vérifier si l'étudiant est déjà enregistré pour cette séance
        Optional<Presence> presenceExistante = presenceRepository
            .findBySeanceIdAndEtudiantId(appel.getSeance().getId(), request.getEtudiantId());

        if (presenceExistante.isPresent()) {
            // Mise à jour de la présence existante
            Presence presence = presenceExistante.get();
            presence.setAppel(appel); // Associer au nouvel appel actif
            presence.setStatut(request.getStatut());
            presence.setHorodatage(heureReponse);
            presence.setModeEnregistrement(request.getModeEnregistrement());
            presence.setLocalisation(request.getLocalisation());

            if (request.getCreneaux() != null && !request.getCreneaux().isEmpty()) {
                // Créneaux fournis manuellement par l'enseignant
                presenceHoraireRepository.deleteAll(presence.getPresencesHoraires());
                presence.getPresencesHoraires().clear();
                for (CreneauPresenceDTO creneauDTO : request.getCreneaux()) {
                    PresenceHoraire ph = new PresenceHoraire();
                    ph.setHeureDebut(creneauDTO.getHeureDebut());
                    ph.setHeureFin(creneauDTO.getHeureFin());
                    ph.setPresent(creneauDTO.isPresent());
                    ph.setPresence(presence);
                    presence.getPresencesHoraires().add(ph);
                }
                presence.recalculerStatut();
            } else {
                // Recalcul automatique (QR / PIN)
                calculerCreneauxAutomatiques(presence, appel, heureReponse);
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
        presence.setHorodatage(heureReponse);
        presence.setModeEnregistrement(request.getModeEnregistrement());
        presence.setLocalisation(request.getLocalisation());

        if (request.getCreneaux() != null && !request.getCreneaux().isEmpty()) {
            // Créneaux fournis manuellement
            presence.setStatut(request.getStatut());
            presence.setRetard(request.isRetard());
            for (CreneauPresenceDTO creneauDTO : request.getCreneaux()) {
                PresenceHoraire ph = new PresenceHoraire();
                ph.setHeureDebut(creneauDTO.getHeureDebut());
                ph.setHeureFin(creneauDTO.getHeureFin());
                ph.setPresent(creneauDTO.isPresent());
                ph.setPresence(presence);
                presence.getPresencesHoraires().add(ph);
            }
            presence.recalculerStatut();
        } else {
            // Calcul automatique (QR / PIN étudiant)
            calculerCreneauxAutomatiques(presence, appel, heureReponse);
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
                System.err.println("Erreur lors de l'enregistrement de la présence pour l'étudiant "
                    + presenceRequest.getEtudiantId() + ": " + e.getMessage());
            }
        }
        return presencesEnregistrees;
    }

    // =========================================================================
    // LECTURE
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public AppelDTO getAppelById(Long appelId) {
        Appel appel = getAppelOrThrow(appelId);
        verifierAccesSurveillant(appel.getSeance());
        return convertToDTO(appel);
    }

    @Override
    @Transactional(readOnly = true)
    public AppelDTO getAppelOuvertBySeance(Long seanceId) {
        verifierAccesSurveillantParSeance(seanceId);
        Optional<Appel> appel = appelRepository.findAppelOuvertBySeanceId(seanceId);
        return appel.map(this::convertToDTO).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppelDTO> getHistoriqueAppels(Long seanceId) {
        verifierAccesSurveillantParSeance(seanceId);
        List<Appel> appels = appelRepository.findAllBySeanceIdOrderByDateHeureDebutAsc(seanceId);
        return appels.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EtudiantSeanceDTO> getEtudiantsSeanceAvecStatut(Long seanceId, Long appelId) {
        Seance seance = getSeanceOrThrow(seanceId);
        verifierAccesSurveillant(seance);

        EmploiDuTemps emploiDuTemps = seance.getEmploiDuTemps();
        if (emploiDuTemps == null || emploiDuTemps.getClasse() == null) {
            throw new BadRequestException("Aucune classe associée à cette séance");
        }

        Classe classe = emploiDuTemps.getClasse();
        List<Etudiant> etudiants = etudiantRepository.findByClasseActuelleId(classe.getId());

        // --- Fusion multi-appel ---
        // 1. Pré-charger les présences positives des appels PRÉCÉDENTS clôturés
        Map<Long, Presence> presencesMap = new HashMap<>();

        if (appelId != null) {
            Appel appelCourant = appelRepository.findById(appelId).orElse(null);
            if (appelCourant != null) {
                // Présences des appels antérieurs (pour affichage créneaux du matin)
                List<Presence> presencesAnterieures = presenceRepository
                    .findPresencesAvant(seanceId, appelCourant.getDateHeureDebut());
                presencesAnterieures.forEach(p -> presencesMap.put(p.getEtudiant().getId(), p));
            }
            // Superposer les présences de l'appel courant (priorité maximale)
            List<Presence> presencesCourantes = presenceRepository.findByAppelId(appelId);
            presencesCourantes.forEach(p -> presencesMap.put(p.getEtudiant().getId(), p));
        }

        // Générer les créneaux par défaut pour la séance entière
        List<CreneauPresenceDTO> creneauxDefaut = genererCreneauxSeance(seance);

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
                dto.setModeEnregistrement(presence.getModeEnregistrement());
                // Bilan horaire cumulé de la séance
                dto.setHeuresPresentes(presence.getHeuresPresentes());
                dto.setHeuresAbsentes(presence.getHeuresAbsentes());

                if (presence.getPresencesHoraires() != null && !presence.getPresencesHoraires().isEmpty()) {
                    dto.setCreneaux(fusionnerCreneaux(creneauxDefaut, presence.getPresencesHoraires()));
                } else {
                    // Pas de créneaux stockés → déduire depuis le statut global
                    final boolean isPresent = "présent".equals(presence.getStatut())
                            || "retard".equals(presence.getStatut());
                    dto.setCreneaux(creneauxDefaut.stream()
                        .map(c -> {
                            CreneauPresenceDTO copy = new CreneauPresenceDTO();
                            copy.setHeureDebut(c.getHeureDebut());
                            copy.setHeureFin(c.getHeureFin());
                            copy.setPresent(isPresent);
                            return copy;
                        })
                        .collect(Collectors.toList()));
                }
            } else {
                dto.setStatut("non_enregistré");
                dto.setCreneaux(creneauxDefaut);
            }

            result.add(dto);
        }

        return result;
    }

    // =========================================================================
    // CODES PIN / QR
    // =========================================================================

    @Override
    public boolean validerCodePIN(Long appelId, String codePIN) {
        PINData pinData = pinCache.get(appelId);
        if (pinData == null) return false;
        if (LocalDateTime.now().isAfter(pinData.getExpiration())) {
            pinCache.remove(appelId);
            return false;
        }
        return pinData.getCode().equals(codePIN);
    }

    @Override
    public AppelDTO regenererQRCode(Long appelId) {
        Appel appel = getAppelOrThrow(appelId);
        verifierAccesSurveillant(appel.getSeance());
        if (!"ouvert".equals(appel.getStatut())) {
            throw new BadRequestException("Impossible de régénérer le QR code pour un appel clôturé");
        }
        AppelDTO appelDTO = convertToDTO(appel);
        String qrContent = generateQRContent(appel);
        appelDTO.setQrCodeBase64(qrCodeService.generateQRCodeBase64(qrContent));
        return appelDTO;
    }

    @Override
    public AppelDTO genererPINPourAppel(Long appelId, Integer dureeMinutes) {
        Appel appel = getAppelOrThrow(appelId);
        verifierAccesSurveillant(appel.getSeance());
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
        Appel appel = getAppelOrThrow(appelId);
        verifierAccesSurveillant(appel.getSeance());
        if (!"ouvert".equals(appel.getStatut())) {
            throw new BadRequestException("Cet appel est clôturé");
        }
        if (qrToken == null || !qrToken.startsWith("APPEL:" + appelId + ":")) {
            throw new BadRequestException("QR code invalide pour cet appel");
        }

        // Valider le timestamp pour rejeter les tokens expirés (plus de 2 minutes / 120000 ms)
        String[] parts = qrToken.split(":");
        if (parts.length >= 3) {
            try {
                long timestamp = Long.parseLong(parts[2]);
                long now = System.currentTimeMillis();
                if (now - timestamp > 120000) {
                    throw new BadRequestException("Le QR code a expiré. Veuillez scanner le nouveau QR code.");
                }
            } catch (NumberFormatException e) {
                throw new BadRequestException("QR code invalide (timestamp illisible)");
            }
        } else {
            throw new BadRequestException("Format de QR code corrompu");
        }

        return convertToDTO(appel);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PresenceDTO> getPresencesByAppelId(Long appelId) {
        verifierAccesSurveillantParAppel(appelId);
        return presenceRepository.findByAppelId(appelId).stream()
            .map(this::convertPresenceToDTO)
            .collect(Collectors.toList());
    }

    @Override
    public PresenceDTO validerCodePINEtudiant(Long appelId, String codePIN) {
        Appel appel = getAppelOrThrow(appelId);
        verifierAccesSurveillant(appel.getSeance());
        if (!"ouvert".equals(appel.getStatut())) {
            throw new BadRequestException("Cet appel est clôturé");
        }
        Optional<CodePresence> codeOpt = codePresenceRepository.findByCodePIN(codePIN);
        if (!codeOpt.isPresent()) throw new BadRequestException("Code PIN invalide");
        CodePresence code = codeOpt.get();
        if (!code.estValide()) throw new BadRequestException("Code PIN expiré ou déjà utilisé");

        Etudiant etudiant = code.getEtudiant();
        LocalDateTime heureReponse = LocalDateTime.now();

        Optional<Presence> presenceExistante = presenceRepository
            .findBySeanceIdAndEtudiantId(appel.getSeance().getId(), etudiant.getId());

        Presence presence;
        if (presenceExistante.isPresent()) {
            presence = presenceExistante.get();
            presence.setAppel(appel); // Associer au nouvel appel actif
            presence.setModeEnregistrement("PIN");
            presence.setHorodatage(heureReponse);
            calculerCreneauxAutomatiques(presence, appel, heureReponse);
        } else {
            presence = new Presence();
            presence.setIdPresence(UUID.randomUUID().toString());
            presence.setEtudiant(etudiant);
            presence.setSeance(appel.getSeance());
            presence.setAppel(appel);
            presence.setModeEnregistrement("PIN");
            presence.setHorodatage(heureReponse);
            calculerCreneauxAutomatiques(presence, appel, heureReponse);
        }

        presence = presenceRepository.save(presence);
        code.marquerUtilise();
        codePresenceRepository.save(code);
        return convertPresenceToDTO(presence);
    }

    @Override
    public PresenceDTO validerQRCodeEtudiant(Long appelId, String qrToken) {
        Appel appel = getAppelOrThrow(appelId);
        verifierAccesSurveillant(appel.getSeance());
        if (!"ouvert".equals(appel.getStatut())) {
            throw new BadRequestException("Cet appel est clôturé");
        }
        Optional<CodePresence> codeOpt = codePresenceRepository.findByQrToken(qrToken);
        if (!codeOpt.isPresent()) throw new BadRequestException("QR code invalide");
        CodePresence code = codeOpt.get();
        if (!code.estValide()) throw new BadRequestException("QR code expiré ou déjà utilisé");

        Etudiant etudiant = code.getEtudiant();
        LocalDateTime heureReponse = LocalDateTime.now();

        Optional<Presence> presenceExistante = presenceRepository
            .findBySeanceIdAndEtudiantId(appel.getSeance().getId(), etudiant.getId());

        Presence presence;
        if (presenceExistante.isPresent()) {
            presence = presenceExistante.get();
            presence.setAppel(appel); // Associer au nouvel appel actif
            presence.setModeEnregistrement("QR");
            presence.setHorodatage(heureReponse);
            calculerCreneauxAutomatiques(presence, appel, heureReponse);
        } else {
            presence = new Presence();
            presence.setIdPresence(UUID.randomUUID().toString());
            presence.setEtudiant(etudiant);
            presence.setSeance(appel.getSeance());
            presence.setAppel(appel);
            presence.setModeEnregistrement("QR");
            presence.setHorodatage(heureReponse);
            calculerCreneauxAutomatiques(presence, appel, heureReponse);
        }

        presence = presenceRepository.save(presence);
        code.marquerUtilise();
        codePresenceRepository.save(code);
        return convertPresenceToDTO(presence);
    }

    // =========================================================================
    // MÉTHODES PRIVÉES — CALCUL AUTOMATIQUE PRÉSENCE/RETARD/ABSENCE
    // =========================================================================

    /**
     * Calcule automatiquement les PresenceHoraire en fonction de l'heure de réponse.
     *
     * Règles (seuils relatifs à l'heure de début de SÉANCE) :
     *  - réponse avant  heureDebutSeance + SEUIL_RETARD_MIN → présent, pas de retard
     *  - réponse entre  SEUIL_RETARD_MIN et SEUIL_ABSENT_MIN → présent + flag retard (1er appel seulement)
     *  - réponse après  SEUIL_ABSENT_MIN → 1er créneau absent, créneaux suivants présents
     *
     * Exemple (séance 8h–12h, seuils 10 min / 45 min) :
     *  - réponse à 8h05 → [8-9 ✅, 9-10 ✅, 10-11 ✅, 11-12 ✅], retard=false
     *  - réponse à 8h25 → [8-9 ✅, 9-10 ✅, 10-11 ✅, 11-12 ✅], retard=true
     *  - réponse à 8h50 → [8-9 ❌, 9-10 ✅, 10-11 ✅, 11-12 ✅], retard=false
     */
    private void calculerCreneauxAutomatiques(Presence presence, Appel appel, LocalDateTime heureReponse) {
        Seance seance = appel.getSeance();

        // 1. Sauvegarder les états des créneaux existants avant de vider la liste
        Map<LocalTime, Boolean> existingSlots = new HashMap<>();
        if (presence.getPresencesHoraires() != null) {
            for (PresenceHoraire ph : presence.getPresencesHoraires()) {
                existingSlots.put(ph.getHeureDebut(), ph.isPresent());
            }
            presenceHoraireRepository.deleteAll(presence.getPresencesHoraires());
            presence.getPresencesHoraires().clear();
        }

        if (seance.getHeureDebut() == null || seance.getHeureFin() == null || seance.getDate() == null) {
            // Fallback : marquer présent sur tous les créneaux sans calcul
            genererCreneauxSeance(seance).forEach(c -> {
                PresenceHoraire ph = new PresenceHoraire();
                ph.setHeureDebut(c.getHeureDebut());
                ph.setHeureFin(c.getHeureFin());
                ph.setPresent(true);
                ph.setPresence(presence);
                presence.getPresencesHoraires().add(ph);
            });
            presence.setStatut("présent");
            return;
        }

        LocalDate dateSeance = seance.getDate();
        LocalDateTime heureDebutSeanceDT = LocalDateTime.of(dateSeance, seance.getHeureDebut());

        // Minutes écoulées depuis le début de la séance
        long minutesDepuisDebut = Duration.between(heureDebutSeanceDT, heureReponse).toMinutes();

        // Retard : uniquement sur le 1er appel de la séance
        boolean estPremierAppel = verifierPremierAppel(appel);
        if (estPremierAppel && minutesDepuisDebut > SEUIL_RETARD_MIN) {
            presence.setRetard(true);
        }

        // Génération des créneaux d'1h avec fusion intelligente
        LocalTime responseLocalTime = heureReponse.toLocalTime();
        LocalTime current = seance.getHeureDebut();
        boolean premierCreneau = true;

        while (current.isBefore(seance.getHeureFin())) {
            LocalTime finCreneau = current.plusHours(1);
            if (finCreneau.isAfter(seance.getHeureFin())) {
                finCreneau = seance.getHeureFin();
            }

            PresenceHoraire ph = new PresenceHoraire();
            ph.setHeureDebut(current);
            ph.setHeureFin(finCreneau);
            ph.setPresence(presence);

            // Déterminer le type de créneau (passé, courant, futur)
            if (responseLocalTime.isAfter(finCreneau) || responseLocalTime.equals(finCreneau)) {
                // Créneau passé : Conserver la valeur existante si présente, sinon absent (false)
                boolean present = existingSlots.getOrDefault(current, false);
                ph.setPresent(present);
            } else if (!responseLocalTime.isBefore(current) && responseLocalTime.isBefore(finCreneau)) {
                // Créneau courant : Présent, mais soumis au seuil d'absence tardive si c'est le premier créneau de la séance
                if (premierCreneau && minutesDepuisDebut > SEUIL_ABSENT_MIN) {
                    ph.setPresent(false);
                } else {
                    ph.setPresent(true);
                }
            } else {
                // Créneau futur : Présent par défaut (en attendant les appels futurs)
                ph.setPresent(true);
            }

            presence.getPresencesHoraires().add(ph);
            current = finCreneau;
            premierCreneau = false;
        }

        // Recalculer le statut global (tient compte du flag retard)
        presence.recalculerStatut();
    }

    /**
     * Retourne true si cet appel est le 1er lancé pour sa séance.
     * (rang = 1 → pas encore d'appel antérieur)
     */
    private boolean verifierPremierAppel(Appel appel) {
        long rang = appelRepository.countAppelsAvant(
            appel.getSeance().getId(), appel.getDateHeureDebut());
        return rang <= 1;
    }

    /**
     * Génère les créneaux d'1 heure pour toute la durée de la séance (tous absents par défaut).
     */
    private List<CreneauPresenceDTO> genererCreneauxSeance(Seance seance) {
        List<CreneauPresenceDTO> creneaux = new ArrayList<>();
        if (seance.getHeureDebut() == null || seance.getHeureFin() == null) return creneaux;

        LocalTime current = seance.getHeureDebut();
        while (current.isBefore(seance.getHeureFin())) {
            CreneauPresenceDTO creneau = new CreneauPresenceDTO();
            creneau.setHeureDebut(current);
            LocalTime next = current.plusHours(1);
            if (next.isAfter(seance.getHeureFin())) next = seance.getHeureFin();
            creneau.setHeureFin(next);
            creneau.setPresent(false);
            creneaux.add(creneau);
            current = next;
        }
        return creneaux;
    }

    /**
     * Fusionne les créneaux réels enregistrés en base de données pour un étudiant
     * avec les créneaux par défaut de la séance. Garantit que chaque étudiant a
     * exactement la structure horaire complète et cohérente.
     */
    private List<CreneauPresenceDTO> fusionnerCreneaux(List<CreneauPresenceDTO> creneauxDefaut, Collection<PresenceHoraire> presencesHoraires) {
        Map<LocalTime, Boolean> phMap = new HashMap<>();
        if (presencesHoraires != null) {
            for (PresenceHoraire ph : presencesHoraires) {
                phMap.put(ph.getHeureDebut(), ph.isPresent());
            }
        }

        List<CreneauPresenceDTO> fusionnes = new ArrayList<>();
        for (CreneauPresenceDTO defaut : creneauxDefaut) {
            CreneauPresenceDTO copy = new CreneauPresenceDTO();
            copy.setHeureDebut(defaut.getHeureDebut());
            copy.setHeureFin(defaut.getHeureFin());
            copy.setPresent(phMap.getOrDefault(defaut.getHeureDebut(), false));
            fusionnes.add(copy);
        }
        return fusionnes;
    }

    /**
     * À la clôture d'un appel : marque comme absents les étudiants non enregistrés
     * UNIQUEMENT sur les créneaux couverts par la plage de cet appel.
     * Ne touche pas aux créneaux des appels précédents.
     */
    private void marquerAbsentsNonEnregistres(Appel appel) {
        EmploiDuTemps emploiDuTemps = appel.getSeance().getEmploiDuTemps();
        if (emploiDuTemps == null || emploiDuTemps.getClasse() == null) return;

        Classe classe = emploiDuTemps.getClasse();
        List<Etudiant> etudiants = etudiantRepository.findByClasseActuelleId(classe.getId());

        // Étudiants déjà enregistrés sur CET appel
        List<Presence> presencesCourantes = presenceRepository.findByAppelId(appel.getId());
        Set<Long> etudiantsEnregistres = presencesCourantes.stream()
            .map(p -> p.getEtudiant().getId())
            .collect(Collectors.toSet());

        // Plage temporelle couverte par cet appel (jusqu'à la fin de la séance pour marquer absents les créneaux futurs)
        LocalTime debutAppelTime = appel.getDateHeureDebut().toLocalTime();
        LocalTime finAppelTime = (appel.getSeance() != null && appel.getSeance().getHeureFin() != null)
            ? appel.getSeance().getHeureFin()
            : ((appel.getDateHeureFin() != null) ? appel.getDateHeureFin().toLocalTime() : LocalTime.now());

        // Générer les créneaux de cet appel (tranches d'1h à partir du début de l'appel jusqu'à la fin de la séance)
        List<CreneauPresenceDTO> creneauxAppel = genererCreneauxPourPlage(
            appel.getSeance(), debutAppelTime, finAppelTime);

        for (Etudiant etudiant : etudiants) {
            if (!etudiantsEnregistres.contains(etudiant.getId())) {

                // Chercher une présence existante sur la séance (appels précédents)
                Optional<Presence> presenceExistante = presenceRepository
                    .findBySeanceIdAndEtudiantId(appel.getSeance().getId(), etudiant.getId());

                Presence presence;
                if (presenceExistante.isPresent()) {
                    // Ajouter les créneaux absents sans écraser les créneaux existants
                    presence = presenceExistante.get();
                    ajouterCreneauxAbsents(presence, creneauxAppel);
                } else {
                    // Créer une nouvelle absence
                    presence = new Presence();
                    presence.setIdPresence(UUID.randomUUID().toString());
                    presence.setEtudiant(etudiant);
                    presence.setSeance(appel.getSeance());
                    presence.setAppel(appel);
                    presence.setStatut("absent");
                    presence.setHorodatage(LocalDateTime.now());
                    presence.setModeEnregistrement("automatique");
                    presence.setRetard(false);

                    for (CreneauPresenceDTO creneau : creneauxAppel) {
                        PresenceHoraire ph = new PresenceHoraire();
                        ph.setHeureDebut(creneau.getHeureDebut());
                        ph.setHeureFin(creneau.getHeureFin());
                        ph.setPresent(false);
                        ph.setPresence(presence);
                        presence.getPresencesHoraires().add(ph);
                    }
                }
                presence.recalculerStatut();
                presenceRepository.save(presence);
            }
        }
    }

    /**
     * Génère les créneaux d'1h entre debutPlage et finPlage (dans les bornes de la séance).
     */
    private List<CreneauPresenceDTO> genererCreneauxPourPlage(
            Seance seance, LocalTime debutPlage, LocalTime finPlage) {
        List<CreneauPresenceDTO> creneaux = new ArrayList<>();
        if (seance.getHeureDebut() == null || seance.getHeureFin() == null) return creneaux;

        // Borner la plage aux horaires de la séance
        LocalTime debut = debutPlage.isBefore(seance.getHeureDebut()) ? seance.getHeureDebut() : debutPlage;
        LocalTime fin   = finPlage.isAfter(seance.getHeureFin())      ? seance.getHeureFin()   : finPlage;

        // Aligner sur les créneaux d'heures pleines de la séance
        LocalTime current = seance.getHeureDebut();
        while (current.isBefore(seance.getHeureFin())) {
            LocalTime next = current.plusHours(1);
            if (next.isAfter(seance.getHeureFin())) next = seance.getHeureFin();
            // Inclure ce créneau s'il chevauche la plage de l'appel
            if (!next.isBefore(debut) && !current.isAfter(fin)) {
                CreneauPresenceDTO c = new CreneauPresenceDTO();
                c.setHeureDebut(current);
                c.setHeureFin(next);
                c.setPresent(false);
                creneaux.add(c);
            }
            current = next;
        }
        return creneaux;
    }

    /**
     * Ajoute ou met à jour des créneaux absents pour une présence existante.
     * IMPORTANT : Ne force PAS à absent un créneau déjà marqué présent lors d'un appel antérieur.
     * Seuls les créneaux inexistants ou déjà absents sont touchés.
     */
    private void ajouterCreneauxAbsents(Presence presence, List<CreneauPresenceDTO> creneauxAppel) {
        Map<LocalTime, PresenceHoraire> mapExistants = new HashMap<>();
        if (presence.getPresencesHoraires() != null) {
            for (PresenceHoraire ph : presence.getPresencesHoraires()) {
                mapExistants.put(ph.getHeureDebut(), ph);
            }
        }

        for (CreneauPresenceDTO creneau : creneauxAppel) {
            PresenceHoraire ph = mapExistants.get(creneau.getHeureDebut());
            if (ph != null) {
                // Si l'étudiant est absent à cet appel spécifique, le créneau couvert par cet appel
                // doit être mis à absent (false), même s'il était présent (true) par défaut.
                ph.setPresent(false);
            } else {
                // Le créneau n'existe pas encore, on le crée absent
                ph = new PresenceHoraire();
                ph.setHeureDebut(creneau.getHeureDebut());
                ph.setHeureFin(creneau.getHeureFin());
                ph.setPresent(false);
                ph.setPresence(presence);
                presence.getPresencesHoraires().add(ph);
            }
        }
    }

    // =========================================================================
    // CONVERSION DTO
    // =========================================================================

    private String generateQRContent(Appel appel) {
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

        // Numéro de rang dans la séance
        if (appel.getDateHeureDebut() != null) {
            long rang = appelRepository.countAppelsAvant(
                appel.getSeance().getId(), appel.getDateHeureDebut());
            dto.setNumeroAppel((int) rang);
            int total = appelRepository.findAllBySeanceIdOrderByDateHeureDebutAsc(
                appel.getSeance().getId()).size();
            dto.setTotalAppels(total);
        }

        // PIN depuis le cache
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
        if (presence.getSeance() != null) dto.setSeanceId(presence.getSeance().getId());
        if (presence.getAppel() != null)  dto.setAppelId(presence.getAppel().getId());

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

    // =========================================================================
    // CLASSE INTERNE — PINData
    // =========================================================================

    private static class PINData {
        private final String code;
        private final LocalDateTime expiration;

        public PINData(String code, LocalDateTime expiration) {
            this.code = code;
            this.expiration = expiration;
        }

        public String getCode() { return code; }
        public LocalDateTime getExpiration() { return expiration; }
    }
}
