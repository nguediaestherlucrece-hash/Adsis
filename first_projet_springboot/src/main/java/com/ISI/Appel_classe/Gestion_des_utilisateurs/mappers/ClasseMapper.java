package com.ISI.Appel_classe.Gestion_des_utilisateurs.mappers;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response.ClasseDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Classe;
import org.springframework.stereotype.Component;

@Component
public class ClasseMapper {

    public ClasseDTO toDTO(Classe classe) {
        if (classe == null) return null;
        ClasseDTO dto = new ClasseDTO();
        dto.setId(classe.getId());
        dto.setIdClasse(classe.getIdClasse());
        dto.setNomClasse(classe.getNomClasse());
        dto.setNiveau(classe.getNiveau());
        dto.setAnneeAcademique(classe.getAnneeAcademique());
        return dto;
    }
}
