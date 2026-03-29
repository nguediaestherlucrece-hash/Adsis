package com.ISI.Appel_classe.Gestion_des_utilisateurs.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.CascadeType;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "filiere")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Filiere {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String idFiliere;

    private String nomFiliere;
    private String description;

    // 🔹 Relations JPA
    @ManyToOne
    @JoinColumn(name = "cycle_id")
    private Cycle cycle;

    @ManyToMany(mappedBy = "filieres")
    private List<Niveau> niveaux = new ArrayList<>();


    @OneToMany(mappedBy = "filiere", cascade = CascadeType.ALL)
    private List<AssistantPedagogique> assistantPedagogiques = new ArrayList<>();

    @OneToMany(mappedBy = "filiere", cascade = CascadeType.ALL)
    private List<Surveillant> surveillants = new ArrayList<>();

}
