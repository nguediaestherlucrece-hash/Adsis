package com.ISI.Appel_classe.Gestion_des_utilisateurs.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.CascadeType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "niveau")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Niveau {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String idNiveau;

    private String nom;

    @ManyToOne
    @JoinColumn(name = "cycle_id")
    private Cycle cycle;

    @OneToMany(mappedBy = "niveau", cascade =CascadeType.ALL)
    private List<Filiere> filieres = new ArrayList<>();
}
