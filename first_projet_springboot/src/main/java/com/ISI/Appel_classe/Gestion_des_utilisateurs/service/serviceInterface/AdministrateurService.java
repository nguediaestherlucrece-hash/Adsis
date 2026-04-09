package com.ISI.Appel_classe.Gestion_des_utilisateurs.service.serviceInterface;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.AdministrateurCreateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.AdministrateurUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response.AdministrateurDTO;

import java.util.List;

/**
 * Service pour la gestion des administrateurs
 */
public interface AdministrateurService {

    List<AdministrateurDTO> getAllAdministrateurs();

    AdministrateurDTO getAdministrateurById(Long id);

    AdministrateurDTO getAdministrateurByEmail(String email);

    AdministrateurDTO createAdministrateur(AdministrateurCreateDTO dto);

    AdministrateurDTO updateAdministrateur(Long id, AdministrateurUpdateDTO dto);

    void deleteAdministrateur(Long id);
}
