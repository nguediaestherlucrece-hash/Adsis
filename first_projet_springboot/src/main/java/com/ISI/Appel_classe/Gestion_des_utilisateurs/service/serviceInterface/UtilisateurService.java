package com.ISI.Appel_classe.Gestion_des_utilisateurs.service.serviceInterface;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.UtilisateurCreateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.UtilisateurUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response.UtilisateurDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UtilisateurService {

    Page<UtilisateurDTO> searchWithFilters(String recherche, String type, Long filiere, Pageable pageable);

    List<UtilisateurDTO> getAllUtilisateurs();

    UtilisateurDTO getUtilisateurById(Long id);

    UtilisateurDTO getUtilisateurByEmail(String email);

    UtilisateurDTO createUtilisateur(UtilisateurCreateDTO dto);

    UtilisateurDTO updateUtilisateur(Long id, UtilisateurUpdateDTO dto);

    void deleteUtilisateur(Long id);

    UtilisateurDTO toggleStatut(String email);

    void changePassword(String email, String ancienMotDePasse, String nouveauMotDePasse);
}
