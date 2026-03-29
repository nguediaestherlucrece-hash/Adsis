package com.ISI.Appel_classe.Gestion_des_utilisateurs.mappers;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.SpecialiteCreateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.SpecialiteUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response.SpecialiteDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Specialite;
import org.springframework.stereotype.Component;

@Component
public class SpecialiteMapper {

    public SpecialiteDTO toDTO(Specialite specialite) {
        if (specialite == null) return null;
        SpecialiteDTO dto = new SpecialiteDTO();
        dto.setId(specialite.getId());
        dto.setIdSpecialite(specialite.getIdSpecialite());
        dto.setNom(specialite.getNom());
        dto.setNiveauId(specialite.getNiveau() != null ? specialite.getNiveau().getId() : null);
        return dto;
    }

    public Specialite fromCreateDTO(SpecialiteCreateDTO dto) {
        if (dto == null) return null;
        Specialite s = new Specialite();
        s.setIdSpecialite(dto.getIdSpecialite());
        s.setNom(dto.getNom());
        return s;
    }

    public void updateFromDTO(Specialite specialite, SpecialiteUpdateDTO dto) {
        if (specialite == null || dto == null) return;
        if (dto.getIdSpecialite() != null) specialite.setIdSpecialite(dto.getIdSpecialite());
        if (dto.getNom() != null) specialite.setNom(dto.getNom());
    }
}
