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

            // Heures précises depuis les créneaux horaires
            com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Presence pres = justificatif.getPresence();
            response.setHeuresAbsentes(pres.getHeuresAbsentes());
            response.setHeuresPresentes(pres.getHeuresPresentes());

            if (justificatif.getPresence().getSeance() != null) {
                com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.Seance seance =
                        justificatif.getPresence().getSeance();
                // Null-safe : cours peut être null si la séance est un événement sans UE
                if (seance.getCours() != null) {
                    String intitule = seance.getCours().getIntitule();
                    String code = seance.getCours().getCodeUE();
                    response.setCoursNom(
                        (intitule != null && !intitule.isBlank()) ? intitule
                        : (code != null && !code.isBlank()) ? code
                        : "—");
                } else {
                    response.setCoursNom("—");
                }
                response.setSeanceDate(seance.getDate());
                if (seance.getHeureDebut() != null) {
                    response.setSeanceHeureDebut(seance.getHeureDebut().toString());
                }
                if (seance.getHeureFin() != null) {
                    response.setSeanceHeureFin(seance.getHeureFin().toString());
                }
            }
            if (justificatif.getPresence().getEtudiant() != null) {
                response.setEtudiantNom(justificatif.getPresence().getEtudiant().getNom());
                response.setEtudiantPrenom(justificatif.getPresence().getEtudiant().getPrenom());
                response.setEtudiantMatricule(justificatif.getPresence().getEtudiant().getMatricule());
                if (justificatif.getPresence().getEtudiant().getClasse() != null) {
                    response.setEtudiantClasse(justificatif.getPresence().getEtudiant().getClasse().getNomClasse());
                }
            }
        }

        if (justificatif.getValidateur() != null) {
            response.setValidateurNom(justificatif.getValidateur().getNom());
        }

        return response;
    }
}
