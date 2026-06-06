package com.ISI.Appel_classe.Gestion_des_appels_etudiant.service.serviceInterface;

import com.ISI.Appel_classe.Gestion_des_appels_etudiant.dto.request.GenererCodesPresenceRequest;
import com.ISI.Appel_classe.Gestion_des_appels_etudiant.dto.request.MarquerPresentParCodePINRequest;
import com.ISI.Appel_classe.Gestion_des_appels_etudiant.dto.request.MarquerPresentParQRCodeRequest;
import com.ISI.Appel_classe.Gestion_des_appels_etudiant.dto.response.CodePresenceDTO;
import com.ISI.Appel_classe.Gestion_des_appels_etudiant.dto.response.PresenceEtudiantDTO;
import com.ISI.Appel_classe.Gestion_des_appels_etudiant.dto.response.StatutSeanceEtudiantDTO;

public interface AppelEtudiantService {

    /**
     * Génère un code PIN et/ou QR code pour un étudiant afin qu'un enseignant puisse le marquer présent
     */
    CodePresenceDTO genererCodesPresence(Long etudiantId, GenererCodesPresenceRequest request);

    /**
     * Marque l'étudiant présent en utilisant un code PIN généré par l'enseignant
     */
    PresenceEtudiantDTO marquerPresentParCodePIN(Long etudiantId, MarquerPresentParCodePINRequest request);

    /**
     * Marque l'étudiant présent en utilisant un QR code généré par l'enseignant
     */
    PresenceEtudiantDTO marquerPresentParQRCode(Long etudiantId, MarquerPresentParQRCodeRequest request);

    /**
     * Récupère les codes présence actifs de l'étudiant pour une séance
     */
    CodePresenceDTO getCodesPresenceActifs(Long etudiantId, Long seanceId);

    /**
     * Valide et utilise un code PIN générée par l'étudiant (côté enseignant)
     */
    PresenceEtudiantDTO validerCodePINEtudiant(Long appelId, Long etudiantId, String codePIN);

    /**
     * Valide et utilise un QR code généré par l'étudiant (côté enseignant)
     */
    PresenceEtudiantDTO validerQRCodeEtudiant(Long appelId, Long etudiantId, String qrToken);

    /**
     * Retourne le statut d’un appel pour un étudiant donné sur une séance :
     * - Y a-t-il un appel ouvert ?
     * - L’étudiant a-t-il déjà répondu à CET appel ?
     * - Quel est son statut détaillé ?
     * Utilisé par le frontend étudiant pour activer/désactiver les boutons QR/PIN.
     */
    StatutSeanceEtudiantDTO getStatutSeancePourEtudiant(Long seanceId, Long etudiantId);
}
