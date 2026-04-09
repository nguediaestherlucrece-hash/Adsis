package com.ISI.Appel_classe.Gestion_des_utilisateurs.mappers;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.NiveauCreateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.NiveauUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response.NiveauDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Niveau;
import org.springframework.stereotype.Component;

@Component
public class NiveauMapper {

    public NiveauDTO toDTO(Niveau niveau) {
        if (niveau == null)
            return null;
        NiveauDTO dto = new NiveauDTO();
        dto.setId(niveau.getId());
        dto.setIdNiveau(niveau.getIdNiveau());
        dto.setNom(niveau.getNom());
        if (niveau.getFiliere() != null) {
            dto.setFiliereId(niveau.getFiliere().getId());
        }
        return dto;
    }

    public Niveau fromCreateDTO(NiveauCreateDTO dto) {
        if (dto == null)
            return null;
        Niveau n = new Niveau();
        n.setIdNiveau(dto.getIdNiveau());
        n.setNom(dto.getNom());
        return n;
    }

    public void updateFromDTO(Niveau niveau, NiveauUpdateDTO dto) {
        if (niveau == null || dto == null)
            return;
        if (dto.getIdNiveau() != null)
            niveau.setIdNiveau(dto.getIdNiveau());
        if (dto.getNom() != null)
            niveau.setNom(dto.getNom());
    }
}
