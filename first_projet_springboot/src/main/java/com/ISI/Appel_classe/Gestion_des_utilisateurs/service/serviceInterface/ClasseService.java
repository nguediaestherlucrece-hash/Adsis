package com.ISI.Appel_classe.Gestion_des_utilisateurs.service.serviceInterface;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response.ClasseDTO;
import java.util.List;

public interface ClasseService {
    List<ClasseDTO> getAllClasses();
}
