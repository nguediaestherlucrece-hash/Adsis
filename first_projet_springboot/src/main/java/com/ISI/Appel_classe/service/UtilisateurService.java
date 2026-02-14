package com.ISI.Appel_classe.service;

import com.ISI.Appel_classe.dto.request.UtilisateurCreateDTO;
import com.ISI.Appel_classe.dto.request.UtilisateurUpdateDTO;
import com.ISI.Appel_classe.dto.response.UtilisateurDTO;

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
