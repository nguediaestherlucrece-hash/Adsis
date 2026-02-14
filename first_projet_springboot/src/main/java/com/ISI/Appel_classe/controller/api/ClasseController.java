package com.ISI.Appel_classe.controller.api;

import com.ISI.Appel_classe.dto.response.ClasseDTO;
import com.ISI.Appel_classe.service.ClasseService;
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
