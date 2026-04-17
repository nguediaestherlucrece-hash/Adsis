package com.ISI.Appel_classe.Gestion_des_appels.service.serviceInterface;

import com.ISI.Appel_classe.Gestion_des_appels.dto.request.DemarrerAppelRequest;
import com.ISI.Appel_classe.Gestion_des_appels.dto.request.EnregistrerPresenceRequest;
import com.ISI.Appel_classe.Gestion_des_appels.dto.request.EnregistrerPresencesBatchRequest;
import com.ISI.Appel_classe.Gestion_des_appels.dto.response.AppelDTO;
import com.ISI.Appel_classe.Gestion_des_appels.dto.response.EtudiantSeanceDTO;
import com.ISI.Appel_classe.Gestion_des_appels.dto.response.PresenceDTO;

import java.util.List;

public interface AppelService {
    
    /**
     * Démarre un nouvel appel pour une séance
     */
    AppelDTO demarrerAppel(DemarrerAppelRequest request);
    
    /**
     * Clôture un appel existant
     */
    AppelDTO cloturerAppel(Long appelId);
    
    /**
     * Enregistre une présence unique
     */
    PresenceDTO enregistrerPresence(Long appelId, EnregistrerPresenceRequest request);
    
    /**
     * Enregistre plusieurs présences en batch (pour synchronisation PWA)
     */
    List<PresenceDTO> enregistrerPresencesBatch(EnregistrerPresencesBatchRequest request);
    
    /**
     * Récupère un appel par son ID
     */
    AppelDTO getAppelById(Long appelId);
    
    /**
     * Récupère l'appel ouvert pour une séance (s'il existe)
     */
    AppelDTO getAppelOuvertBySeance(Long seanceId);
    
    /**
     * Récupère la liste des étudiants d'une séance avec leur statut de présence
     */
    List<EtudiantSeanceDTO> getEtudiantsSeanceAvecStatut(Long seanceId, Long appelId);
    
    /**
     * Valide un code PIN pour un appel
     */
    boolean validerCodePIN(Long appelId, String codePIN);
    
    /**
     * Régénère un QR code pour un appel
     */
    AppelDTO regenererQRCode(Long appelId);
}
