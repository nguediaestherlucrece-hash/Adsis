package com.ISI.Appel_classe.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "enseignant")
@DiscriminatorValue("ENSEIGNANT")
@PrimaryKeyJoinColumn(name = "utilisateur_id")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Enseignant extends Utilisateur {

    private String specialite;
    private String grade;

    // 🔹 Relations JPA
    @ManyToMany
    @JoinTable(name = "enseignant_cours", joinColumns = @JoinColumn(name = "enseignant_id"), inverseJoinColumns = @JoinColumn(name = "cours_id"))
    private List<CoursUE> coursEnseignes = new ArrayList<>();

    @OneToMany(mappedBy = "enseignant")
    private List<Seance> seances = new ArrayList<>();
}