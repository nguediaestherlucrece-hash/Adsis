package com.ISI.Appel_classe.Gestion_des_utilisateurs.model;

import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.CoursUE;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.CascadeType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "specialite")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Specialite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String idSpecialite;

    private String nom;

    @ManyToOne
    @JoinColumn(name = "filiere_id")
    private Filiere filiere;

    @OneToMany(mappedBy = "specialite", cascade =CascadeType.ALL)
    private List<Classe> classes = new ArrayList<>();

    @OneToMany(mappedBy = "specialite", cascade =CascadeType.ALL)
    private List<CoursUE> cours = new ArrayList<>();

}
