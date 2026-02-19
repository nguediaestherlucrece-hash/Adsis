package com.ISI.Appel_classe.Gestion_des_utilisateurs.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "etablissement")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Etablissement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String idEtablissement;
    private String nomEtablissement;
    private String telephone;
    private String emailContact;
    private String anneeAcademiqueCourante;

    // 🔹 Relations JPA
    @OneToMany(mappedBy = "etablissement", cascade = CascadeType.ALL)
    private List<Filiere> filieres = new ArrayList<>();

    @OneToMany(mappedBy = "etablissement", cascade = CascadeType.ALL)
    private List<Classe> classes = new ArrayList<>();

    // 🔹 Méthodes de logique métier
    public void creerClasse() {
        // TODO: logique de création de classe
    }

    public void gererEmployes() {
        // TODO: logique de gestion des employés
    }

    public void configurerAnneeAcademique() {
        // TODO: logique de configuration de l'année académique
    }
}
