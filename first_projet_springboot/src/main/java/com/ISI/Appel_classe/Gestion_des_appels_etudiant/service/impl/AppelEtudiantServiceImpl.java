package com.ISI.Appel_classe.Gestion_des_appels_etudiant.service.impl;

import com.ISI.Appel_classe.Gestion_des_appels_etudiant.dto.request.GenererCodesPresenceRequest;
import com.ISI.Appel_classe.Gestion_des_appels_etudiant.dto.request.MarquerPresentParCodePINRequest;
import com.ISI.Appel_classe.Gestion_des_appels_etudiant.dto.request.MarquerPresentParQRCodeRequest;
import com.ISI.Appel_classe.Gestion_des_appels_etudiant.dto.response.CodePresenceDTO;
import com.ISI.Appel_classe.Gestion_des_appels_etudiant.dto.response.PresenceEtudiantDTO;
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

        // Enregistrer la présence
        Optional<Presence> presenceExistante = presenceRepository
                .findByAppelIdAndEtudiantId(appelId, etudiantId);

        Presence presence;
        if (presenceExistante.isPresent()) {
            presence = presenceExistante.get();
            presence.setStatut("présent");
            presence.setModeEnregistrement("PIN");
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

        // Enregistrer la présence
        Optional<Presence> presenceExistante = presenceRepository
                .findByAppelIdAndEtudiantId(appelId, etudiantId);

        Presence presence;
        if (presenceExistante.isPresent()) {
            presence = presenceExistante.get();
            presence.setStatut("présent");
            presence.setModeEnregistrement("QR");
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
}
