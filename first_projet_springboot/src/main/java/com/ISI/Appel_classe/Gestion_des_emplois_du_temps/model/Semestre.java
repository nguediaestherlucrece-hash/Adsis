package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "semestre")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Semestre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String idSemestre;

    private String nomSemestre;
    private boolean statut;

    @ManyToOne
    @JoinColumn(name = "annee_academique_id")
    private AnneeAcademique annee;

    @OneToMany(mappedBy = "semestre", cascade =CascadeType.ALL)
    private List<EmploiDuTemps> emploiDuTemps = new ArrayList<>();
}
