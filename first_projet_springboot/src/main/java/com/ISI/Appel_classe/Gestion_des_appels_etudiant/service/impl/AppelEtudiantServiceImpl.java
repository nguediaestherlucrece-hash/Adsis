package com.ISI.Appel_classe.Gestion_des_appels_etudiant.service.impl;

import com.ISI.Appel_classe.Gestion_des_appels_etudiant.dto.request.GenererCodesPresenceRequest;
import com.ISI.Appel_classe.Gestion_des_appels_etudiant.dto.request.MarquerPresentParCodePINRequest;
import com.ISI.Appel_classe.Gestion_des_appels_etudiant.dto.request.MarquerPresentParQRCodeRequest;
import com.ISI.Appel_classe.Gestion_des_appels_etudiant.dto.response.CodePresenceDTO;
import com.ISI.Appel_classe.Gestion_des_appels_etudiant.dto.response.PresenceEtudiantDTO;
import com.ISI.Appel_classe.Gestion_des_appels_etudiant.dto.response.StatutSeanceEtudiantDTO;
import com.ISI.Appel_classe.Gestion_des_appels_etudiant.mappers.CodePresenceMapper;
import com.ISI.Appel_classe.Gestion_des_appels_etudiant.mappers.PresenceEtudiantMapper;
import com.ISI.Appel_classe.Gestion_des_appels_etudiant.service.serviceInterface.AppelEtudiantService;
import com.ISI.Appel_classe.Gestion_des_appels_etudiant.util.QRCodeEtudiantService;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.Seance;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.repository.SeanceRepository;
import com.ISI.Appel_classe.Gestion_des_etudiants.model.Etudiant;
import com.ISI.Appel_classe.Gestion_des_etudiants.repository.EtudiantRepository;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.exception.BadRequestException;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.exception.ResourceNotFoundException;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Appel;
import com.ISI.Appel_classe.Gestion_des_appels_etudiant.model.CodePresence;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Presence;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.PresenceHoraire;
import com.ISI.Appel_classe.Gestion_des_appels_enseignant.repository.AppelRepository;
import com.ISI.Appel_classe.Gestion_des_appels_etudiant.repository.CodePresenceRepository;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.PresenceHoraireRepository;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.PresenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AppelEtudiantServiceImpl implements AppelEtudiantService {

    private final EtudiantRepository etudiantRepository;
    private final SeanceRepository seanceRepository;
    private final AppelRepository appelRepository;
    private final PresenceRepository presenceRepository;
    private final PresenceHoraireRepository presenceHoraireRepository;
    private final CodePresenceRepository codePresenceRepository;
    private final QRCodeEtudiantService qrCodeEtudiantService;
    private final CodePresenceMapper codePresenceMapper;
    private final PresenceEtudiantMapper presenceEtudiantMapper;

    @Override
    public CodePresenceDTO genererCodesPresence(Long etudiantId, GenererCodesPresenceRequest request) {
        // Vérifier que l'étudiant existe
        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant non trouvé"));

        // Vérifier que la séance existe
        Seance seance = seanceRepository.findById(request.getSeanceId())
                .orElseThrow(() -> new ResourceNotFoundException("Séance non trouvée"));

        // Déterminer la durée de validité (par défaut 5 minutes)
        int dureeValidite = request.getDureeValiditeMinutes() != null ? request.getDureeValiditeMinutes() : 5;
        LocalDateTime expiration = LocalDateTime.now().plusMinutes(dureeValidite);

        // Déterminer le type (par défaut BOTH)
        String type = request.getType() != null ? request.getType() : "BOTH";

        // Créer ou récupérer un code existant
        Optional<CodePresence> codeExistant = codePresenceRepository.findLatestCodeByEtudiant(etudiantId);

        CodePresence codePresence;
        if (codeExistant.isPresent() && codeExistant.get().estValide()
                && codeExistant.get().getSeance().getId().equals(request.getSeanceId())) {
            // Réutiliser le code existant s'il est valide pour la même séance
            codePresence = codeExistant.get();
        } else {
            // Créer un nouveau code
            codePresence = new CodePresence();
            codePresence.setEtudiant(etudiant);
            codePresence.setSeance(seance);
            codePresence.setDateCreation(LocalDateTime.now());
            codePresence.setDateExpiration(expiration);
            codePresence.setStatut("actif");
            codePresence.setUtilise(false);
            codePresence.setType(type);

            // Générer le code PIN si nécessaire
            if ("PIN".equals(type) || "BOTH".equals(type)) {
                String codePIN = qrCodeEtudiantService.generatePINCode();
                codePresence.setCodePIN(codePIN);
            }

            // Générer le QR token si nécessaire
            if ("QR".equals(type) || "BOTH".equals(type)) {
                String qrToken = qrCodeEtudiantService.generateQRToken(etudiantId, request.getSeanceId());
                codePresence.setQrToken(qrToken);
            }

            codePresence = codePresenceRepository.save(codePresence);
        }

        // Générer le QR code en base64 si le token existe
        String qrCodeBase64 = null;
        if (codePresence.getQrToken() != null) {
            qrCodeBase64 = qrCodeEtudiantService.generateQRCodeBase64(codePresence.getQrToken());
        }

        return codePresenceMapper.toDTO(codePresence, qrCodeBase64);
    }

    @Override
    public PresenceEtudiantDTO marquerPresentParCodePIN(Long etudiantId, MarquerPresentParCodePINRequest request) {
        // Vérifier l'existence de l'étudiant
        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant non trouvé"));

        // Vérifier l'existence de la séance
        Seance seance = seanceRepository.findById(request.getSeanceId())
                .orElseThrow(() -> new ResourceNotFoundException("Séance non trouvée"));

        // Chercher le code PIN généré par l'enseignant
        Optional<CodePresence> codeOpt = codePresenceRepository.findByCodePIN(request.getCodePIN());

        if (!codeOpt.isPresent()) {
            throw new BadRequestException("Code PIN invalide");
        }

        CodePresence code = codeOpt.get();

        // Vérifier que le code est valide
        if (!code.estValide()) {
            throw new BadRequestException("Code PIN expiré ou déjà utilisé");
        }

        // Créer ou mettre à jour la présence
        Presence presence = creerOuMettreAJourPresence(etudiant, seance, code.getAppel(), "présent", "PIN", request);

        // Marquer le code comme utilisé
        code.marquerUtilise();
        codePresenceRepository.save(code);

        return presenceEtudiantMapper.toDTO(presence);
    }

    @Override
    public PresenceEtudiantDTO marquerPresentParQRCode(Long etudiantId, MarquerPresentParQRCodeRequest request) {
        // Vérifier l'existence de l'étudiant
        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant non trouvé"));

        // Vérifier l'existence de la séance
        Seance seance = seanceRepository.findById(request.getSeanceId())
                .orElseThrow(() -> new ResourceNotFoundException("Séance non trouvée"));

        // Chercher le QR token généré par l'enseignant
        Optional<CodePresence> codeOpt = codePresenceRepository.findByQrToken(request.getQrToken());

        if (!codeOpt.isPresent()) {
            throw new BadRequestException("QR code invalide");
        }

        CodePresence code = codeOpt.get();

        // Vérifier que le code est valide
        if (!code.estValide()) {
            throw new BadRequestException("QR code expiré ou déjà utilisé");
        }

        // Créer ou mettre à jour la présence
        Presence presence = creerOuMettreAJourPresence(etudiant, seance, code.getAppel(), "présent", "QR", request);

        // Marquer le code comme utilisé
        code.marquerUtilise();
        codePresenceRepository.save(code);

        return presenceEtudiantMapper.toDTO(presence);
    }

    @Override
    @Transactional(readOnly = true)
    public CodePresenceDTO getCodesPresenceActifs(Long etudiantId, Long seanceId) {
        List<CodePresence> codes = codePresenceRepository.findCodesActifsByEtudiantAndSeance(etudiantId, seanceId);

        if (codes.isEmpty()) {
            throw new ResourceNotFoundException("Aucun code présence actif trouvé");
        }

        CodePresence code = codes.get(0); // Retourner le premier (le plus récent)
        String qrCodeBase64 = null;

        if (code.getQrToken() != null) {
            qrCodeBase64 = qrCodeEtudiantService.generateQRCodeBase64(code.getQrToken());
        }

        return codePresenceMapper.toDTO(code, qrCodeBase64);
    }

    @Override
    public PresenceEtudiantDTO validerCodePINEtudiant(Long appelId, Long etudiantId, String codePIN) {
        // Vérifier l'existence de l'appel
        Appel appel = appelRepository.findById(appelId)
                .orElseThrow(() -> new ResourceNotFoundException("Appel non trouvé"));

        if (!"ouvert".equals(appel.getStatut())) {
            throw new BadRequestException("Cet appel est clôturé");
        }

        // Vérifier l'existence de l'étudiant
        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant non trouvé"));

        // Chercher le code PIN généré par l'étudiant
        Optional<CodePresence> codeOpt = codePresenceRepository.findByCodePIN(codePIN);

        if (!codeOpt.isPresent()) {
            throw new BadRequestException("Code PIN invalide");
        }

        CodePresence code = codeOpt.get();

        // Vérifier que le code appartient à l'étudiant correct
        if (!code.getEtudiant().getId().equals(etudiantId)) {
            throw new BadRequestException("Code PIN invalide pour cet étudiant");
        }

        // Vérifier que le code est valide
        if (!code.estValide()) {
            throw new BadRequestException("Code PIN expiré ou déjà utilisé");
        }

        // Enregistrer la présence (recherche par séance pour éviter les doublons/violations d'unicité)
        Optional<Presence> presenceExistante = presenceRepository
                .findBySeanceIdAndEtudiantId(appel.getSeance().getId(), etudiantId);

        Presence presence;
        if (presenceExistante.isPresent()) {
            presence = presenceExistante.get();
            presence.setAppel(appel); // Associer à l'appel actif
            presence.setStatut("présent");
            presence.setModeEnregistrement("PIN");
            presence.setHorodatage(LocalDateTime.now());
        } else {
            presence = new Presence();
            presence.setIdPresence(UUID.randomUUID().toString());
            presence.setEtudiant(etudiant);
            presence.setSeance(appel.getSeance());
            presence.setAppel(appel);
            presence.setStatut("présent");
            presence.setModeEnregistrement("PIN");
            presence.setHorodatage(LocalDateTime.now());
        }

        // Générer les créneaux horaires par défaut si inexistants
        if (presence.getPresencesHoraires() == null || presence.getPresencesHoraires().isEmpty()) {
            List<CreneauPresence> creneaux = genererCreneauxSeance(appel.getSeance());
            for (CreneauPresence creneau : creneaux) {
                PresenceHoraire ph = new PresenceHoraire();
                ph.setHeureDebut(creneau.getHeureDebut());
                ph.setHeureFin(creneau.getHeureFin());
                ph.setPresent(true);
                ph.setPresence(presence);
                presence.getPresencesHoraires().add(ph);
            }
        }

        presence = presenceRepository.save(presence);

        // Marquer le code comme utilisé
        code.marquerUtilise();
        codePresenceRepository.save(code);

        return presenceEtudiantMapper.toDTO(presence);
    }

    @Override
    public PresenceEtudiantDTO validerQRCodeEtudiant(Long appelId, Long etudiantId, String qrToken) {
        // Vérifier l'existence de l'appel
        Appel appel = appelRepository.findById(appelId)
                .orElseThrow(() -> new ResourceNotFoundException("Appel non trouvé"));

        if (!"ouvert".equals(appel.getStatut())) {
            throw new BadRequestException("Cet appel est clôturé");
        }

        // Vérifier l'existence de l'étudiant
        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant non trouvé"));

        // Chercher le QR token généré par l'étudiant
        Optional<CodePresence> codeOpt = codePresenceRepository.findByQrToken(qrToken);

        if (!codeOpt.isPresent()) {
            throw new BadRequestException("QR code invalide");
        }

        CodePresence code = codeOpt.get();

        // Vérifier que le code appartient à l'étudiant correct
        if (!code.getEtudiant().getId().equals(etudiantId)) {
            throw new BadRequestException("QR code invalide pour cet étudiant");
        }

        // Vérifier que le code est valide
        if (!code.estValide()) {
            throw new BadRequestException("QR code expiré ou déjà utilisé");
        }

        // Enregistrer la présence (recherche par séance pour éviter les doublons/violations d'unicité)
        Optional<Presence> presenceExistante = presenceRepository
                .findBySeanceIdAndEtudiantId(appel.getSeance().getId(), etudiantId);

        Presence presence;
        if (presenceExistante.isPresent()) {
            presence = presenceExistante.get();
            presence.setAppel(appel); // Associer à l'appel actif
            presence.setStatut("présent");
            presence.setModeEnregistrement("QR");
            presence.setHorodatage(LocalDateTime.now());
        } else {
            presence = new Presence();
            presence.setIdPresence(UUID.randomUUID().toString());
            presence.setEtudiant(etudiant);
            presence.setSeance(appel.getSeance());
            presence.setAppel(appel);
            presence.setStatut("présent");
            presence.setModeEnregistrement("QR");
            presence.setHorodatage(LocalDateTime.now());
        }

        // Générer les créneaux horaires par défaut si inexistants
        if (presence.getPresencesHoraires() == null || presence.getPresencesHoraires().isEmpty()) {
            List<CreneauPresence> creneaux = genererCreneauxSeance(appel.getSeance());
            for (CreneauPresence creneau : creneaux) {
                PresenceHoraire ph = new PresenceHoraire();
                ph.setHeureDebut(creneau.getHeureDebut());
                ph.setHeureFin(creneau.getHeureFin());
                ph.setPresent(true);
                ph.setPresence(presence);
                presence.getPresencesHoraires().add(ph);
            }
        }

        presence = presenceRepository.save(presence);

        // Marquer le code comme utilisé
        code.marquerUtilise();
        codePresenceRepository.save(code);

        return presenceEtudiantMapper.toDTO(presence);
    }

    // Méthodes utilitaires privées

    private Presence creerOuMettreAJourPresence(
            Etudiant etudiant,
            Seance seance,
            Appel appel,
            String statut,
            String modeEnregistrement,
            Object request) {

        Optional<Presence> presenceExistante = presenceRepository
                .findBySeanceIdAndEtudiantId(seance.getId(), etudiant.getId());

        Presence presence;
        if (presenceExistante.isPresent()) {
            presence = presenceExistante.get();
            if (appel != null) {
                presence.setAppel(appel);
            }
        } else {
            presence = new Presence();
            presence.setIdPresence(UUID.randomUUID().toString());
            presence.setEtudiant(etudiant);
            presence.setSeance(seance);
            if (appel != null) {
                presence.setAppel(appel);
            }
        }

        presence.setStatut(statut);
        presence.setModeEnregistrement(modeEnregistrement);
        presence.setHorodatage(LocalDateTime.now());

        if (request instanceof MarquerPresentParCodePINRequest) {
            MarquerPresentParCodePINRequest req = (MarquerPresentParCodePINRequest) request;
            presence.setRetard(req.isRetard());
            presence.setLocalisation(req.getLocalisation());
        } else if (request instanceof MarquerPresentParQRCodeRequest) {
            MarquerPresentParQRCodeRequest req = (MarquerPresentParQRCodeRequest) request;
            presence.setRetard(req.isRetard());
            presence.setLocalisation(req.getLocalisation());
        }

        // Générer les créneaux horaires par défaut si inexistants
        if (presence.getPresencesHoraires() == null || presence.getPresencesHoraires().isEmpty()) {
            List<CreneauPresence> creneaux = genererCreneauxSeance(seance);
            for (CreneauPresence creneau : creneaux) {
                PresenceHoraire ph = new PresenceHoraire();
                ph.setHeureDebut(creneau.getHeureDebut());
                ph.setHeureFin(creneau.getHeureFin());
                ph.setPresent(true);
                ph.setPresence(presence);
                presence.getPresencesHoraires().add(ph);
            }
        }

        return presenceRepository.save(presence);
    }

    private List<CreneauPresence> genererCreneauxSeance(Seance seance) {
        List<CreneauPresence> creneaux = new ArrayList<>();

        if (seance.getHeureDebut() == null || seance.getHeureFin() == null) {
            return creneaux;
        }

        LocalTime current = seance.getHeureDebut();
        while (current.isBefore(seance.getHeureFin())) {
            LocalTime next = current.plusHours(1);
            if (next.isAfter(seance.getHeureFin())) {
                next = seance.getHeureFin();
            }
            creneaux.add(new CreneauPresence(current, next));
            current = next;
        }

        return creneaux;
    }

    // Classe utilitaire pour les créneaux
    private static class CreneauPresence {
        LocalTime heureDebut;
        LocalTime heureFin;

        CreneauPresence(LocalTime heureDebut, LocalTime heureFin) {
            this.heureDebut = heureDebut;
            this.heureFin = heureFin;
        }

        public LocalTime getHeureDebut() { return heureDebut; }
        public LocalTime getHeureFin() { return heureFin; }
    }

    // =========================================================================
    // STATUT APPEL POUR L'ÉTUDIANT
    // =========================================================================

    /**
     * Retourne le statut d’un appel pour un étudiant donné sur une séance :
     * - appelOuvert  : y a-t-il un appel actuellement ouvert ?
     * - dejaPresent  : l’étudiant a-t-il répondu à CET appel (pas les précédents) ?
     * - messageStatut: message lisible pour l’affichage étudiant
     */
    @Override
    @Transactional(readOnly = true)
    public StatutSeanceEtudiantDTO getStatutSeancePourEtudiant(Long seanceId, Long etudiantId) {
        StatutSeanceEtudiantDTO dto = new StatutSeanceEtudiantDTO();

        // 1. Y a-t-il un appel ouvert pour cette séance ?
        Optional<Appel> appelOuvert = appelRepository.findAppelOuvertBySeanceId(seanceId);
        dto.setAppelOuvert(appelOuvert.isPresent());

        if (!appelOuvert.isPresent()) {
            // Aucun appel ouvert — chercher la dernière présence enregistrée sur la séance
            Optional<Presence> dernierePresence = presenceRepository
                .findTopBySeanceIdAndEtudiantIdOrderByIdDesc(seanceId, etudiantId);
            if (dernierePresence.isPresent()) {
                Presence p = dernierePresence.get();
                dto.setDejaPresent(true);
                dto.setStatut(p.getStatut());
                dto.setRetard(p.isRetard());
                dto.setHeuresPresentes(p.getHeuresPresentes());
                dto.setHeuresAbsentes(p.getHeuresAbsentes());
                if (p.getHorodatage() != null) {
                    dto.setHeurePresence(
                        p.getHorodatage().format(DateTimeFormatter.ofPattern("HH:mm")));
                }
                dto.setMessageStatut(construireMessageStatut(p, dto.getHeurePresence()));
            }
            return dto;
        }

        Appel appel = appelOuvert.get();
        dto.setAppelId(appel.getId());

        // 2. L’étudiant a-t-il déjà répondu à CET appel ?
        Optional<Presence> presenceOpt = presenceRepository
            .findByAppelIdAndEtudiantId(appel.getId(), etudiantId);

        if (!presenceOpt.isPresent()) {
            // Pas encore répondu — boutons actifs
            dto.setDejaPresent(false);
            return dto;
        }

        // 3. L’étudiant a répondu — construire le statut détaillé
        Presence presence = presenceOpt.get();
        dto.setDejaPresent(true);
        dto.setStatut(presence.getStatut());
        dto.setRetard(presence.isRetard());
        dto.setHeuresPresentes(presence.getHeuresPresentes());
        dto.setHeuresAbsentes(presence.getHeuresAbsentes());

        // Heure de réponse formatée
        if (presence.getHorodatage() != null) {
            dto.setHeurePresence(
                presence.getHorodatage().format(DateTimeFormatter.ofPattern("HH:mm")));
        }

        // Message lisible
        dto.setMessageStatut(construireMessageStatut(presence, dto.getHeurePresence()));

        return dto;
    }

    /**
     * Construit un message lisible pour l’affichage côté étudiant.
     * Exemples :
     *   "\u2705 Présent \u00b7 Enregistré \u00e0 08h10"
     *   "\u26a0\ufe0f Arrivé en retard \u00b7 Enregistré \u00e0 08h25 \u00b7 3h présent(es)"
     *   "\u26a1 Présence partielle \u00b7 1h absent(e) \u00b7 3h présent(es)"
     */
    private String construireMessageStatut(Presence presence, String heureFormatee) {
        StringBuilder sb = new StringBuilder();
        String statut = presence.getStatut();

        if ("retard".equals(statut) || presence.isRetard()) {
            sb.append("\u26a0\ufe0f Arrivé en retard");
        } else if ("présent".equals(statut)) {
            sb.append("\u2705 Présent");
        } else if ("présent_partiel".equals(statut)) {
            sb.append("\u26a1 Présence partielle");
        } else if ("absent".equals(statut)) {
            sb.append("\u274c Absent");
        } else {
            sb.append("\u2753 Statut inconnu");
        }

        if (heureFormatee != null && !heureFormatee.isEmpty()) {
            sb.append(" \u00b7 Enregistré \u00e0 ").append(heureFormatee.replace(":", "h"));
        }

        int heuresAbsentes  = presence.getHeuresAbsentes();
        int heuresPresentes = presence.getHeuresPresentes();

        if (heuresAbsentes > 0) {
            sb.append(" \u00b7 ").append(heuresAbsentes)
              .append("h absent").append(heuresAbsentes > 1 ? "es" : "e");
        }
        if (heuresPresentes > 0) {
            sb.append(" \u00b7 ").append(heuresPresentes)
              .append("h présent").append(heuresPresentes > 1 ? "es" : "e");
        }

        return sb.toString();
    }
}
