package com.Gestion_des_utilisateurs.service;

import com.Gestion_des_utilisateurs.dto.request.UtilisateurCreateDTO;
import com.Gestion_des_utilisateurs.dto.request.UtilisateurUpdateDTO;
import com.Gestion_des_utilisateurs.dto.response.UtilisateurDTO;

import java.util.List;

public interface UtilisateurService {
    List<UtilisateurDTO> getAllUtilisateurs();

    UtilisateurDTO getUtilisateurById(Long id);

    UtilisateurDTO getUtilisateurByEmail(String email);

    UtilisateurDTO createUtilisateur(UtilisateurCreateDTO dto);

    UtilisateurDTO updateUtilisateur(Long id, UtilisateurUpdateDTO dto);

    void deleteUtilisateur(Long id);

    UtilisateurDTO toggleStatut(String email);
}
