package com.Gestion_des_utilisateurs.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    private String idFiliere;
    private String nomFiliere;
    private String description;

    // 🔹 Relations JPA
    @ManyToOne
    @JoinColumn(name = "etablissement_id")
    private Etablissement etablissement;

    @OneToMany(mappedBy = "filiere", cascade = CascadeType.ALL)
    private List<Classe> classes = new ArrayList<>();

    // 🔹 Méthodes de logique métier
    public void ajouterClasse(Classe classe) {
        classes.add(classe);
        classe.setFiliere(this);
    }

    public void retirerClasse(Classe classe) {
        classes.remove(classe);
        classe.setFiliere(null);
    }

    public void affecterResponsable() {
        // TODO: logique d'affectation de responsable
    }
}
