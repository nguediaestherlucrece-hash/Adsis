package com.ISI.Appel_classe.Gestion_des_justificatifs.mappers;

import com.ISI.Appel_classe.Gestion_des_justificatifs.dto.response.JustificatifResponse;
import com.ISI.Appel_classe.Gestion_des_justificatifs.model.Justificatif;
import org.springframework.stereotype.Component;

@Component
public class JustificatifMapper {

    public JustificatifResponse toDTO(Justificatif justificatif) {
        if (justificatif == null) {
            return null;
        }

        JustificatifResponse response = new JustificatifResponse();
        response.setId(justificatif.getId());
        response.setIdJustificatif(justificatif.getIdJustificatif());
        response.setMotif(justificatif.getMotif());
        response.setCommentaire(justificatif.getCommentaire());
        response.setDocumentUrl(justificatif.getDocumentUrl());
        response.setStatutValidation(justificatif.getStatutValidation());
        response.setMotifRejet(justificatif.getMotifRejet());
        response.setDateSoumission(justificatif.getDateSoumission());
        response.setDateValidation(justificatif.getDateValidation());
        response.setComplementDocumentUrl(justificatif.getComplementDocumentUrl());
        response.setComplementCommentaire(justificatif.getComplementCommentaire());
        response.setDateComplement(justificatif.getDateComplement());

        // Relations
        if (justificatif.getPresence() != null) {
            response.setPresenceId(justificatif.getPresence().getId());
            response.setPresenceStatut(justificatif.getPresence().getStatut());
            if (justificatif.getPresence().getSeance() != null) {
                response.setCoursNom(justificatif.getPresence().getSeance().getCours().getIntitule());
            }
            if (justificatif.getPresence().getEtudiant() != null) {
                response.setEtudiantNom(justificatif.getPresence().getEtudiant().getNom());
                response.setEtudiantPrenom(justificatif.getPresence().getEtudiant().getPrenom());
            }
        }

        if (justificatif.getValidateur() != null) {
            response.setValidateurNom(justificatif.getValidateur().getNom());
        }

        return response;
    }
}
