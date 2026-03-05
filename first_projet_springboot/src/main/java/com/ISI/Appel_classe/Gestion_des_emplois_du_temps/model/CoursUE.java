package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Classe;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Enseignant;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Seance;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.CascadeType;

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

    @Column(unique = true)
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
