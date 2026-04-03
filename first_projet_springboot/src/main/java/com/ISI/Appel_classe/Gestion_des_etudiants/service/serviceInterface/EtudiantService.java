package com.ISI.Appel_classe.Gestion_des_etudiants.service.serviceInterface;

import com.ISI.Appel_classe.Gestion_des_etudiants.dto.request.EtudiantCreateDTO;
import com.ISI.Appel_classe.Gestion_des_etudiants.dto.request.EtudiantUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_etudiants.dto.response.EtudiantDTO;
import com.ISI.Appel_classe.Gestion_des_etudiants.dto.response.EtudiantImportResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    /**
     * Importe une liste d'étudiants depuis un fichier Excel et les assigne à une classe
     * Le fichier doit contenir les colonnes: Nom, Prénom, Email, Password, Matricule, DateNaissance, Téléphone, Statut
     *
     * @param file Le fichier Excel (.xlsx ou .xls)
     * @param classeId L'ID de la classe à associer à tous les étudiants importés
     * @return Un DTO contenant les résultats de l'import (succès, échecs, etc.)
     */
    EtudiantImportResponseDTO importEtudiantsFromExcel(MultipartFile file, Long classeId);

    /**
     * Génère un fichier Excel modèle pour l'import d'étudiants
     * Le fichier contient les en-têtes et quelques lignes d'exemple
     *
     * @return Un tableau de bytes contenant le fichier Excel
     * @throws IOException En cas d'erreur lors de la génération
     */
    byte[] generateEtudiantTemplate() throws IOException;
}
