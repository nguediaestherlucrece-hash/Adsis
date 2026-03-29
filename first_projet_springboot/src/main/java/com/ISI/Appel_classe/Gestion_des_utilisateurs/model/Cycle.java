package com.ISI.Appel_classe.Gestion_des_utilisateurs.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.CascadeType;
import org.antlr.v4.runtime.misc.IntegerList;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "cycle")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String idCycle;

    private String nomCycle;
    private Integer anneeDeCreation;
    private Integer anneeDebut;
    private Integer anneeFin;

    // 🔹 Relations JPA
    @OneToMany(mappedBy = "cycle", cascade =CascadeType.ALL)
    private List<Filiere> filieres = new ArrayList<>();
}
