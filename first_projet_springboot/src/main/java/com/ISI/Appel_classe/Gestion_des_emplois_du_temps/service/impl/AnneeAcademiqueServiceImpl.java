package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.service.impl;

import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.request.AnneeAcademiqueCreateDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.request.AnneeAcademiqueUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.response.AnneeAcademiqueDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.mappers.AnneeAcademiqueMapper;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.AnneeAcademique;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.repository.AnneeAcademiqueRepository;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.service.serviceInterface.AnneeAcademiqueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AnneeAcademiqueServiceImpl implements AnneeAcademiqueService {

    private final AnneeAcademiqueRepository anneeAcademiqueRepository;
    private final AnneeAcademiqueMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<AnneeAcademiqueDTO> getAllAnnees() {
        return anneeAcademiqueRepository.findAll()
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AnneeAcademiqueDTO getAnneeById(Long id) {
        AnneeAcademique annee = anneeAcademiqueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Année académique non trouvée avec l'id: " + id));
        return mapper.toDTO(annee);
    }

    @Override
    @Transactional(readOnly = true)
    public AnneeAcademiqueDTO getAnneeByCode(String idAnnee) {
        AnneeAcademique annee = anneeAcademiqueRepository.findByIdAnneeAcademique(idAnnee)
                .orElseThrow(() -> new RuntimeException("Année académique non trouvée avec le code: " + idAnnee));
        return mapper.toDTO(annee);
    }

    @Override
    @Transactional(readOnly = true)
    public AnneeAcademiqueDTO createAnnee(AnneeAcademiqueCreateDTO dto) {
        AnneeAcademique annee = new AnneeAcademique();
        annee.setIdAnneeAcademique(dto.getIdAnneeAcademique());
        annee.setNomAnneeAcademique(dto.getNomAnneeAcademique());
        annee.setAnneeDebut(dto.getAnneeDebut());
        annee.setAnneeFin(dto.getAnneeFin());
        annee.setActif(dto.isActif());

        AnneeAcademique saved = anneeAcademiqueRepository.save(annee);
        log.info("Année académique créée avec l'id {}", saved.getId());
        return mapper.toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AnneeAcademiqueDTO updateAnnee(Long id, AnneeAcademiqueUpdateDTO dto) {
        AnneeAcademique annee = anneeAcademiqueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Année académique non trouvée avec l'id: " + id));

        annee.setIdAnneeAcademique(dto.getIdAnneeAcademique());
        annee.setNomAnneeAcademique(dto.getNomAnneeAcademique());
        annee.setAnneeDebut(dto.getAnneeDebut());
        annee.setAnneeFin(dto.getAnneeFin());
        annee.setActif(dto.isActif());

        AnneeAcademique updated = anneeAcademiqueRepository.save(annee);
        log.info("Année académique mise à jour avec l'id {}", updated.getId());
        return mapper.toDTO(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public void deleteAnnee(Long id) {
        if (!anneeAcademiqueRepository.existsById(id)) {
            throw new RuntimeException("Année académique non trouvée avec l'id: " + id);
        }
        anneeAcademiqueRepository.deleteById(id);
        log.info("Année académique supprimée avec l'id {}", id);
    }

    @Override
    @Transactional
    public AnneeAcademiqueDTO activerAnnee(Long id) {
        AnneeAcademique annee = anneeAcademiqueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Année académique non trouvée avec l'id: " + id));

        // Désactiver toutes les années
        anneeAcademiqueRepository.deactivateAll();

        // Activer celle demandée
        annee.setActif(true);
        AnneeAcademique saved = anneeAcademiqueRepository.save(annee);
        log.info("Année académique activée avec l'id {}", saved.getId());
        return mapper.toDTO(saved);
    }
}
