package com.Gestion_des_utilisateurs.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cours_ue")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoursUE {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String idCours;
    private String codeUE;
    private String intitule;
    private Integer volumeHoraire;

    // 🔹 Relations JPA
    @ManyToMany(mappedBy = "cours")
    private List<Classe> classes = new ArrayList<>();

    @ManyToMany(mappedBy = "coursEnseignes")
    private List<Enseignant> enseignants = new ArrayList<>();

    @OneToMany(mappedBy = "cours", cascade = CascadeType.ALL)
    private List<Seance> seances = new ArrayList<>();

    // 🔹 Méthodes de logique métier
    public void associerClasse() {
        // TODO: logique d'association avec une classe
    }

    public void associerEnseignant() {
        // TODO: logique d'association avec un enseignant
    }
}
