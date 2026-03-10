package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.service.serviceInterface;

import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.request.coursUECreateDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.request.coursUEUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.response.CoursUEDTO;

import java.util.List;

public interface CourUEService {

    List<CoursUEDTO> getAllCours();

    CoursUEDTO getCoursById(Long id);

    CoursUEDTO getCoursByCode(String idCours);

    CoursUEDTO createCours(coursUECreateDTO dto);

    CoursUEDTO updateCours(Long id, coursUEUpdateDTO dto);

    void deleteCours(Long id);
}
