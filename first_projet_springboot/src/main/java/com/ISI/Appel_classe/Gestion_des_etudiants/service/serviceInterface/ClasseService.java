package com.ISI.Appel_classe.Gestion_des_etudiants.service.serviceInterface;

import com.ISI.Appel_classe.Gestion_des_etudiants.dto.request.ClasseCreateDTO;
import com.ISI.Appel_classe.Gestion_des_etudiants.dto.request.ClasseUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_etudiants.dto.response.ClasseDTO;
import java.util.List;

public interface ClasseService {

    List<ClasseDTO> getAllClasses();

    ClasseDTO getClasseById(Long id);

    ClasseDTO createClasse(ClasseCreateDTO dto);

    ClasseDTO updateClasse(Long id, ClasseUpdateDTO dto);

    void deleteClasse(Long id);
}
