package com.Gestion_des_utilisateurs.service.impl;

import com.Gestion_des_utilisateurs.dto.response.ClasseDTO;
import com.Gestion_des_utilisateurs.entity.Classe;
import com.Gestion_des_utilisateurs.repository.ClasseRepository;
import com.Gestion_des_utilisateurs.service.ClasseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClasseServiceImpl implements ClasseService {

    @Autowired
    private ClasseRepository repository;

    @Override
    public List<ClasseDTO> getAllClasses() {
        return repository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ClasseDTO convertToDTO(Classe classe) {
        ClasseDTO dto = new ClasseDTO();
        dto.setId(classe.getId());
        dto.setIdClasse(classe.getIdClasse());
        dto.setNomClasse(classe.getNomClasse());
        dto.setNiveau(classe.getNiveau());
        dto.setAnneeAcademique(classe.getAnneeAcademique());
        return dto;
    }
}
