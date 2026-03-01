package com.ISI.Appel_classe.Gestion_des_utilisateurs.mappers;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.ClasseCreateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.ClasseUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response.ClasseDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Classe;
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
        dto.setNiveau(classe.getNiveau());
        dto.setAnneeAcademique(classe.getAnneeAcademique());
        // Mapper l'ID de la filière associée
        if (classe.getFiliere() != null) {
            dto.setFiliereId(classe.getFiliere().getId());
        }
        return dto;
    }

    public Classe fromCreateDTO(ClasseCreateDTO dto) {
        if (dto == null)
            return null;
        Classe c = new Classe();
        c.setIdClasse(dto.getIdClasse());
        c.setNomClasse(dto.getNomClasse());
        c.setNiveau(dto.getNiveau());
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
        if (dto.getNiveau() != null)
            c.setNiveau(dto.getNiveau());
        if (dto.getAnneeAcademique() != null)
            c.setAnneeAcademique(dto.getAnneeAcademique());
    }
}
