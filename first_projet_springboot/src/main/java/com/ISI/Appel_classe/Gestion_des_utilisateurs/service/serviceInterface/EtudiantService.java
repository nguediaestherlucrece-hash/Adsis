package com.ISI.Appel_classe.Gestion_des_utilisateurs.service.serviceInterface;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.EtudiantCreateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.EtudiantUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response.EtudiantDTO;

import java.util.List;

/**
 * Service pour la gestion des étudiants
 * Gère TOUS les attributs hérités de Utilisateur + les attributs spécifiques
 */
public interface EtudiantService {

    /**
     * Récupère tous les étudiants
     */
    List<EtudiantDTO> getAllEtudiants();

    /**
     * Récupère un étudiant par son ID
     */
    EtudiantDTO getEtudiantById(Long id);

    /**
     * Récupère un étudiant par son matricule
     */
    EtudiantDTO getEtudiantByMatricule(String matricule);

    /**
     * Récupère un étudiant par son email
     */
    EtudiantDTO getEtudiantByEmail(String email);

    /**
     * Récupère tous les étudiants d'une classe
     */
    List<EtudiantDTO> getEtudiantsByClasse(Long classeId);

    /**
     * Crée un nouvel étudiant avec TOUS les attributs (Utilisateur + Etudiant)
     */
    EtudiantDTO createEtudiant(EtudiantCreateDTO dto);

    /**
     * Met à jour un étudiant (TOUS les attributs modifiables)
     */
    EtudiantDTO updateEtudiant(Long id, EtudiantUpdateDTO dto);

    /**
     * Supprime un étudiant
     */
    void deleteEtudiant(Long id);

    /**
     * Assigne un étudiant à une classe
     */
    EtudiantDTO assignerClasse(Long etudiantId, Long classeId);
}
