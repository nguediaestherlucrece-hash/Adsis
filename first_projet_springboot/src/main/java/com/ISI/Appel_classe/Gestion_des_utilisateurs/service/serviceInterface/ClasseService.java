package com.ISI.Appel_classe.Gestion_des_utilisateurs.service.serviceInterface;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.ClasseCreateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.ClasseUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response.ClasseDTO;
import java.util.List;

public interface ClasseService {

    List<ClasseDTO> getAllClasses();

    ClasseDTO getClasseById(Long id);

    ClasseDTO createClasse(ClasseCreateDTO dto);

    ClasseDTO updateClasse(Long id, ClasseUpdateDTO dto);

    void deleteClasse(Long id);
}
