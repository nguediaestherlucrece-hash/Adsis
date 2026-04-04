package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.*;
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
    private Integer credit;

    // Relations
    @ManyToOne
    @JoinColumn(name = "specialite_id")
    private Specialite specialite;

    @ManyToMany(mappedBy = "coursEnseignes")
    private List<Enseignant> enseignants = new ArrayList<>();

    @OneToMany(mappedBy = "cours", cascade = CascadeType.ALL)
    private List<Seance> seances = new ArrayList<>();

    // methode 
    public void associerClasse() {
        
    }

    public void associerEnseignant() {
       
    }
}
