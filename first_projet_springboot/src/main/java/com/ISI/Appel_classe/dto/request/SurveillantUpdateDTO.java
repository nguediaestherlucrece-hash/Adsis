package com.ISI.Appel_classe.dto.request;

import com.ISI.Appel_classe.entity.Statut;
import lombok.Data;

import java.util.Set;

@Data
public class SurveillantUpdateDTO {

    private String nom;
    private String prenom;
    private String email;
    private Statut statut;
    private Set<String> roleNames;
    private String affectation;
}
