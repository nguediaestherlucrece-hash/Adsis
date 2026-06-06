package com.ISI.Appel_classe.Gestion_des_justificatifs.dto.response;

import com.ISI.Appel_classe.Gestion_des_justificatifs.enums.StatutValidation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JustificatifResponse {

    private Long id;
    private String idJustificatif;
    private String motif;
    private String commentaire;
    private String documentUrl;
    private StatutValidation statutValidation;
    private String motifRejet;
    private LocalDate dateSoumission;
    private LocalDate dateValidation;

    // Complément
    private String complementDocumentUrl;
    private String complementCommentaire;
    private LocalDate dateComplement;

    // Relations
    private Long presenceId;
    private String presenceStatut;
    private String etudiantNom;
    private String etudiantPrenom;
    private String coursNom;
    private String validateurNom;

    // Champs supplémentaires pour l'assistant
    private LocalDate seanceDate;
    private String seanceHeureDebut;
    private String seanceHeureFin;
    private String etudiantClasse;
    private String etudiantMatricule;
    /** Heures effectivement absentes sur cette séance */
    private int heuresAbsentes;
    /** Heures effectivement présentes sur cette séance */
    private int heuresPresentes;
}
