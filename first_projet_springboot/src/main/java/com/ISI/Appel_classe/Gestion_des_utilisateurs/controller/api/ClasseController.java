package com.Gestion_des_utilisateurs.controller.api;

import com.Gestion_des_utilisateurs.dto.response.ClasseDTO;
import com.Gestion_des_utilisateurs.service.ClasseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
public class ClasseController {

    @Autowired
    private ClasseService service;

    @GetMapping
    public ResponseEntity<List<ClasseDTO>> getAllClasses() {
        return ResponseEntity.ok(service.getAllClasses());
    }
}
