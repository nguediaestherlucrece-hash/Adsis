package com.ISI.Appel_classe.Gestion_des_etudiants.mappers;

import com.ISI.Appel_classe.Gestion_des_etudiants.dto.request.ClasseCreateDTO;
import com.ISI.Appel_classe.Gestion_des_etudiants.dto.request.ClasseUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_etudiants.dto.response.ClasseDTO;
import com.ISI.Appel_classe.Gestion_des_etudiants.model.Classe;
import org.springframework.stereotype.Component;

@Component
public class ClasseMapper {

    public ClasseDTO toDTO(Classe classe) {
        if (classe == null)
            return null;
        ClasseDTO dto = new ClasseDTO();
        dto.setId(classe.getId());
        dto.setIdClasse(classe.getIdClasse());
        dto.setNomClasse(classe.getNomClasse());
        dto.setAnneeAcademique(classe.getAnneeAcademique());
        dto.setSpecialiteId(classe.getSpecialite() != null ? classe.getSpecialite().getId() : null);
        return dto;
    }

    public Classe fromCreateDTO(ClasseCreateDTO dto) {
        if (dto == null)
            return null;
        Classe c = new Classe();
        c.setIdClasse(dto.getIdClasse());
        c.setNomClasse(dto.getNomClasse());
        c.setAnneeAcademique(dto.getAnneeAcademique());
        return c;
    }

    public void updateFromDTO(Classe c, ClasseUpdateDTO dto) {
        if (dto == null || c == null)
            return;
        if (dto.getIdClasse() != null)
            c.setIdClasse(dto.getIdClasse());
        if (dto.getNomClasse() != null)
            c.setNomClasse(dto.getNomClasse());
        if (dto.getAnneeAcademique() != null)
            c.setAnneeAcademique(dto.getAnneeAcademique());
    }
}
