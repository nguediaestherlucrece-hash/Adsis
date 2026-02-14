package com.ISI.Appel_classe.service.impl;

import com.ISI.Appel_classe.dto.response.ClasseDTO;
import com.ISI.Appel_classe.entity.Classe;
import com.ISI.Appel_classe.repository.ClasseRepository;
import com.ISI.Appel_classe.service.ClasseService;
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
