package com.ISI.Appel_classe.Gestion_des_appels_etudiant.mappers;

import com.ISI.Appel_classe.Gestion_des_appels_etudiant.dto.response.CodePresenceDTO;
import com.ISI.Appel_classe.Gestion_des_appels_etudiant.model.CodePresence;
import org.springframework.stereotype.Component;

@Component
public class CodePresenceMapper {

    public CodePresenceDTO toDTO(CodePresence codePresence, String qrCodeBase64) {
        if (codePresence == null) {
            return null;
        }

        CodePresenceDTO dto = new CodePresenceDTO();
        dto.setId(codePresence.getId());
        dto.setCodePIN(codePresence.getCodePIN());
        dto.setQrToken(codePresence.getQrToken());
        dto.setQrCodeBase64(qrCodeBase64);
        dto.setType(codePresence.getType());
        dto.setDateCreation(codePresence.getDateCreation());
        dto.setDateExpiration(codePresence.getDateExpiration());
        dto.setStatut(codePresence.getStatut());
        dto.setUtilise(codePresence.isUtilise());

        if (codePresence.getEtudiant() != null) {
            dto.setEtudiantId(codePresence.getEtudiant().getId());
        }

        if (codePresence.getSeance() != null) {
            dto.setSeanceId(codePresence.getSeance().getId());
        }

        return dto;
    }
}
