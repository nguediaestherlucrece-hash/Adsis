package com.ISI.Appel_classe.Gestion_des_utilisateurs.mappers;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.CycleCreateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.CycleUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response.CycleDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Cycle;
import org.springframework.stereotype.Component;

@Component
public class CycleMapper {

    public CycleDTO toDTO(Cycle cycle) {
        if (cycle == null)
            return null;
        CycleDTO dto = new CycleDTO();
        dto.setId(cycle.getId());
        dto.setIdCycle(cycle.getIdCycle());
        dto.setNomCycle(cycle.getNomCycle());
        dto.setAnneeDeCreation(cycle.getAnneeDeCreation());
        dto.setAnneeDebut(cycle.getAnneeDebut());
        dto.setAnneeFin(cycle.getAnneeFin());
        return dto;
    }

    public Cycle fromCreateDTO(CycleCreateDTO dto) {
        if (dto == null)
            return null;
        Cycle c = new Cycle();
        c.setIdCycle(dto.getIdCycle());
        c.setNomCycle(dto.getNomCycle());
        c.setAnneeDeCreation(dto.getAnneeDeCreation());
        c.setAnneeDebut(dto.getAnneeDebut());
        c.setAnneeFin(dto.getAnneeFin());
        return c;
    }

    public void updateFromDTO(Cycle cycle, CycleUpdateDTO dto) {
        if (dto.getIdCycle() != null)
            cycle.setIdCycle(dto.getIdCycle());
        if (dto.getNomCycle() != null)
            cycle.setNomCycle(dto.getNomCycle());
        if (dto.getAnneeDeCreation() != null)
            cycle.setAnneeDeCreation(dto.getAnneeDeCreation());
        if (dto.getAnneeDebut() != null)
            cycle.setAnneeDebut(dto.getAnneeDebut());
        if (dto.getAnneeFin() != null)
            cycle.setAnneeFin(dto.getAnneeFin());
    }
}