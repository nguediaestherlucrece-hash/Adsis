package com.ISI.Appel_classe.Gestion_des_utilisateurs.model;

import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.CoursUE;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.EmploiDuTemps;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.CascadeType;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "classe")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Classe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String idClasse;

    private String nomClasse;
    private String anneeAcademique;

    // 🔹 Relations JPA
    @ManyToOne
    @JoinColumn(name = "specialite_id")
    private Specialite specialite;

    @OneToMany(mappedBy = "classe", cascade =CascadeType.ALL)
    private List<Etudiant> etudiants = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "emploi_du_temps_id", referencedColumnName = "id")
    private EmploiDuTemps emploiDuTemps;
    // 🔹 Méthodes métier (conservées)

    public void ajouterEtudiant(Etudiant etudiant) {
        if (etudiants == null)
            etudiants = new ArrayList<>();
        etudiants.add(etudiant);
        etudiant.setClasse(this);
    }
}
