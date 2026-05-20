package com.ISI.Appel_classe.Gestion_des_justificatifs.service.impl;

import com.ISI.Appel_classe.Gestion_des_justificatifs.dto.request.AddComplementRequest;
import com.ISI.Appel_classe.Gestion_des_justificatifs.dto.request.CreateJustificatifRequest;
import com.ISI.Appel_classe.Gestion_des_justificatifs.dto.request.FilterJustificatifRequest;
import com.ISI.Appel_classe.Gestion_des_justificatifs.dto.request.UpdateJustificatifRequest;
import com.ISI.Appel_classe.Gestion_des_justificatifs.dto.response.JustificatifListResponse;
import com.ISI.Appel_classe.Gestion_des_justificatifs.dto.response.JustificatifResponse;
import com.ISI.Appel_classe.Gestion_des_justificatifs.enums.StatutValidation;
import com.ISI.Appel_classe.Gestion_des_justificatifs.mappers.JustificatifMapper;
import com.ISI.Appel_classe.Gestion_des_justificatifs.model.Justificatif;
import com.ISI.Appel_classe.Gestion_des_justificatifs.repository.JustificatifRepository;
import com.ISI.Appel_classe.Gestion_des_justificatifs.service.serviceInterface.JustificatifService;
import com.ISI.Appel_classe.Gestion_des_justificatifs.util.SaveDocument;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.AssistantPedagogique;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Presence;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.PresenceRepository;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.AssistantPedagogiqueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class JustificatifServiceImpl implements JustificatifService {

    @Autowired
    private JustificatifRepository justificatifRepository;

    @Autowired
    private PresenceRepository presenceRepository;

    @Autowired
    private AssistantPedagogiqueRepository assistantPedagogiqueRepository;

    @Autowired
    private JustificatifMapper justificatifMapper;

    @Override
    public JustificatifResponse createJustificatif(CreateJustificatifRequest request, Long etudiantId) throws Exception {
        // Récupérer la presence
        Presence presence = presenceRepository.findById(request.getPresenceId())
                .orElseThrow(() -> new Exception("Presence non trouvée"));

        // Vérifier que la présence appartient à l'étudiant
        if (!presence.getEtudiant().getId().equals(etudiantId)) {
            throw new Exception("Accès refusé: cette présence ne vous appartient pas");
        }

        // Vérifier que la présence n'a pas déjà de justificatif
        if (presence.getJustificatif() != null) {
            throw new Exception("Cette absence/retard a déjà un justificatif");
        }

        // Créer le justificatif
        Justificatif justificatif = new Justificatif();
        justificatif.setIdJustificatif("JUST-" + UUID.randomUUID().toString());
        justificatif.setMotif(request.getMotif());
        justificatif.setCommentaire(request.getCommentaire());
        justificatif.setStatutValidation(StatutValidation.EN_ATTENTE);
        justificatif.setDateSoumission(LocalDate.now());
        justificatif.setPresence(presence);

        // Sauvegarder le document si fourni
        if (request.getDocument() != null && !request.getDocument().isEmpty()) {
            SaveDocument saveDocumentUtil = new SaveDocument();
            String documentUrl = saveDocumentUtil.saveDocument(request.getDocument());
            justificatif.setDocumentUrl(documentUrl);
        }

        Justificatif saved = justificatifRepository.save(justificatif);
        return justificatifMapper.toDTO(saved);
    }

    @Override
    public JustificatifListResponse getMyJustificatifs(Long etudiantId, FilterJustificatifRequest filter) {
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize());

        Page<Justificatif> page;

        // Si les deux dates de soumission sont fournies
        if (filter.getDateDebutSoumission() != null && filter.getDateFinSoumission() != null) {
            page = justificatifRepository.findByEtudiantIdAndDateSoumissionBetween(
                    etudiantId,
                    filter.getDateDebutSoumission(),
                    filter.getDateFinSoumission(),
                    pageable
            );
        } else {
            page = justificatifRepository.findByEtudiantId(etudiantId, pageable);
        }

        return buildListResponse(page);
    }

    @Override
    public JustificatifResponse getJustificatifById(Long id) {
        Justificatif justificatif = justificatifRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Justificatif non trouvé"));
        return justificatifMapper.toDTO(justificatif);
    }

    @Override
    public JustificatifResponse validateJustificatif(Long id, UpdateJustificatifRequest request, Long assistantId) {
        Justificatif justificatif = justificatifRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Justificatif non trouvé"));

        AssistantPedagogique assistant = assistantPedagogiqueRepository.findById(assistantId)
                .orElseThrow(() -> new RuntimeException("Assistant non trouvé"));

        // Vérifier que l'assistant gère cette filière (optional, selon votre logique)

        // Mettre à jour le statut
        justificatif.setStatutValidation(request.getStatutValidation());
        justificatif.setDateValidation(LocalDate.now());
        justificatif.setValidateur(assistant);

        // Si rejeté, ajouter le motif
        if (request.getStatutValidation() == StatutValidation.REJETE) {
            justificatif.setMotifRejet(request.getMotifRejet());
        }

        Justificatif updated = justificatifRepository.save(justificatif);
        return justificatifMapper.toDTO(updated);
    }

    @Override
    public JustificatifListResponse getAllJustificatifs(Long assistantId, FilterJustificatifRequest filter) {
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize());

        Page<Justificatif> page = justificatifRepository.findAllWithFilters(
                filter.getEtudiantId(),
                filter.getDateDebutSoumission(),
                filter.getDateFinSoumission(),
                filter.getDateDebutValidation(),
                filter.getDateFinValidation(),
                filter.getStatutValidation(),
                pageable
        );

        return buildListResponse(page);
    }

    @Override
    public JustificatifResponse addComplement(Long id, AddComplementRequest request, Long etudiantId) throws Exception {
        Justificatif justificatif = justificatifRepository.findById(id)
                .orElseThrow(() -> new Exception("Justificatif non trouvé"));

        // Vérifier que le justificatif appartient à l'étudiant
        if (!justificatif.getPresence().getEtudiant().getId().equals(etudiantId)) {
            throw new Exception("Accès refusé: ce justificatif ne vous appartient pas");
        }

        // Vérifier que le statut est rejeté
        if (justificatif.getStatutValidation() != StatutValidation.REJETE) {
            throw new Exception("Vous ne pouvez ajouter un complément que si le justificatif a été rejeté");
        }

        // Ajouter le complément
        justificatif.setComplementCommentaire(request.getCommentaire());
        justificatif.setDateComplement(LocalDate.now());
        justificatif.setStatutValidation(StatutValidation.EN_ATTENTE); // Repasser en attente

        if (request.getDocument() != null && !request.getDocument().isEmpty()) {
            SaveDocument saveDocumentUtil = new SaveDocument();
            String documentUrl = saveDocumentUtil.saveDocument(request.getDocument());
            justificatif.setComplementDocumentUrl(documentUrl);
        }

        Justificatif updated = justificatifRepository.save(justificatif);
        return justificatifMapper.toDTO(updated);
    }

    @Override
    public void deleteJustificatif(Long id, Long etudiantId) {
        Justificatif justificatif = justificatifRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Justificatif non trouvé"));

        // Vérifier que le justificatif appartient à l'étudiant
        if (!justificatif.getPresence().getEtudiant().getId().equals(etudiantId)) {
            throw new RuntimeException("Accès refusé: ce justificatif ne vous appartient pas");
        }

        // Vérifier que le statut est en attente (avant validation)
        if (justificatif.getStatutValidation() != StatutValidation.EN_ATTENTE) {
            throw new RuntimeException("Vous ne pouvez supprimer que les justificatifs en attente de validation");
        }

        // Supprimer les fichiers
        if (justificatif.getDocumentUrl() != null) {
            deleteFile(justificatif.getDocumentUrl());
        }
        if (justificatif.getComplementDocumentUrl() != null) {
            deleteFile(justificatif.getComplementDocumentUrl());
        }

        justificatifRepository.deleteById(id);
    }

    // ============ MÉTHODES UTILITAIRES ============

    private void deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Files.delete(path);
            }
        } catch (Exception e) {
            // Log mais ne pas bloquer
            System.err.println("Erreur lors de la suppression du fichier: " + e.getMessage());
        }
    }

    private JustificatifListResponse buildListResponse(Page<Justificatif> page) {
        List<JustificatifResponse> content = page.getContent()
                .stream()
                .map(justificatifMapper::toDTO)
                .collect(Collectors.toList());

        JustificatifListResponse response = new JustificatifListResponse();
        response.setContent(content);
        response.setCurrentPage(page.getNumber());
        response.setTotalPages(page.getTotalPages());
        response.setTotalElements(page.getTotalElements());
        response.setPageSize(page.getSize());

        return response;
    }
}
