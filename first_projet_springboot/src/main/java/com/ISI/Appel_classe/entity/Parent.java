package com.ISI.Appel_classe.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "parent")
@DiscriminatorValue("PARENT")
@PrimaryKeyJoinColumn(name = "utilisateur_id")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Parent extends Utilisateur {

    private String lienParental; // "père", "mère", "tuteur"

    // 🔹 Relations JPA
    @ManyToMany
    @JoinTable(name = "parent_etudiant", joinColumns = @JoinColumn(name = "parent_id"), inverseJoinColumns = @JoinColumn(name = "etudiant_id"))
    private List<Etudiant> etudiants = new ArrayList<>();
}
