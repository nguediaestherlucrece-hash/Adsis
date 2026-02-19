package com.ISI.Appel_classe.Gestion_des_utilisateurs.service.impl;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response.ClasseDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.ClasseRepository;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.service.serviceInterface.ClasseService;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.mappers.ClasseMapper;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClasseServiceImpl implements ClasseService {

    private final ClasseRepository repository;
    private final ClasseMapper mapper;

    @Override
    public List<ClasseDTO> getAllClasses() {
        return repository.findAll().stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }
}
