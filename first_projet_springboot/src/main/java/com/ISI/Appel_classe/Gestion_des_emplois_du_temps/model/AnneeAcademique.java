package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "annee_academique")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnneeAcademique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String idAnneeAcademique;

    private String nomAnneeAcademique;
    private LocalDate anneeDebut;
    private LocalDate anneeFin;

    @OneToMany(mappedBy = "annee", cascade =CascadeType.ALL)
    private List<Semestre> semestres = new ArrayList<>();

}
