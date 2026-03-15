package com.ISI.Appel_classe.Gestion_des_utilisateurs.mappers;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.FiliereCreateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.FiliereUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response.FiliereDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Filiere;
import org.springframework.stereotype.Component;

@Component
public class FiliereMapper {

    public FiliereDTO toDTO(Filiere filiere) {
        if (filiere == null)
            return null;
        FiliereDTO dto = new FiliereDTO();
        dto.setId(filiere.getId());
        dto.setIdFiliere(filiere.getIdFiliere());
        dto.setNomFiliere(filiere.getNomFiliere());
        dto.setDescription(filiere.getDescription());
        if (filiere.getNiveaux() != null && !filiere.getNiveaux().isEmpty()) {
            dto.setNiveauIds(filiere.getNiveaux().stream().map(com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Niveau::getId).collect(java.util.stream.Collectors.toList()));
        }
        return dto;
    }

    public Filiere fromCreateDTO(FiliereCreateDTO dto) {
        if (dto == null)
            return null;
        Filiere f = new Filiere();
        f.setIdFiliere(dto.getIdFiliere());
        f.setNomFiliere(dto.getNomFiliere());
        f.setDescription(dto.getDescription());
        return f;
    }

    public void updateFromDTO(Filiere filiere, FiliereUpdateDTO dto) {
        if (dto.getIdFiliere() != null)
            filiere.setIdFiliere(dto.getIdFiliere());
        if (dto.getNomFiliere() != null)
            filiere.setNomFiliere(dto.getNomFiliere());
        if (dto.getDescription() != null)
            filiere.setDescription(dto.getDescription());
    }
}

