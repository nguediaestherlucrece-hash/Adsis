package com.ISI.Appel_classe.Gestion_des_justificatifs.service.serviceInterface;

import com.ISI.Appel_classe.Gestion_des_justificatifs.dto.request.AddComplementRequest;
import com.ISI.Appel_classe.Gestion_des_justificatifs.dto.request.CreateJustificatifRequest;
import com.ISI.Appel_classe.Gestion_des_justificatifs.dto.request.FilterJustificatifRequest;
import com.ISI.Appel_classe.Gestion_des_justificatifs.dto.request.UpdateJustificatifRequest;
import com.ISI.Appel_classe.Gestion_des_justificatifs.dto.response.JustificatifListResponse;
import com.ISI.Appel_classe.Gestion_des_justificatifs.dto.response.JustificatifResponse;
import com.ISI.Appel_classe.Gestion_des_justificatifs.model.Justificatif;

public interface JustificatifService {

    //Soumettre un justificatif (Étudiant)
    JustificatifResponse createJustificatif(CreateJustificatifRequest request, Long etudiantId) throws Exception;

    //Obtenir les justificatifs de l'étudiant connecté (filtrage par date)
    JustificatifListResponse getMyJustificatifs(Long etudiantId, FilterJustificatifRequest filter);

    //Obtenir un justificatif spécifique
    JustificatifResponse getJustificatifById(Long id);

    //Valider ou rejeter un justificatif (Assistant)
    JustificatifResponse validateJustificatif(Long id, UpdateJustificatifRequest request, Long assistantId);

    //Obtenir tous les justificatifs pour l'assistant (avec filtres multiples)
    JustificatifListResponse getAllJustificatifs(Long assistantId, FilterJustificatifRequest filter);

    //Ajouter un complément au justificatif (après rejet)
    JustificatifResponse addComplement(Long id, AddComplementRequest request, Long etudiantId) throws Exception;

    //Supprimer un justificatif
    void deleteJustificatif(Long id, Long etudiantId);
}
