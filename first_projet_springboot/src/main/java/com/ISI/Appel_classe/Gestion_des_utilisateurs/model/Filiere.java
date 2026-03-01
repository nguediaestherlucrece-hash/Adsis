package com.ISI.Appel_classe.Gestion_des_utilisateurs.model;

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

    @Column(unique = true)
    private String idFiliere;

    private String nomFiliere;
    private String description;

    // 🔹 Relations JPA
    @ManyToOne
    @JoinColumn(name = "cycle")
    private Cycle cycle;

    @OneToMany(mappedBy = "filiere", cascade = CascadeType.ALL)
    private List<Classe> classes = new ArrayList<>();

    @OneToMany(mappedBy = "filiere", cascade = CascadeType.ALL)
    private List<AssistantPedagogique> assistantPedagogiques = new ArrayList<>();

    @OneToMany(mappedBy = "filiere", cascade = CascadeType.ALL)
    private List<Surveillant> surveillants = new ArrayList<>();

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
