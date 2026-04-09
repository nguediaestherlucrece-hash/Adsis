package com.ISI.Appel_classe.Gestion_des_etudiants.model;

import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.AnneeAcademique;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.Semestre;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "historique_classe")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoriqueClasse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Etudiant etudiant;

    @ManyToOne
    @JoinColumn(name = "classe_id", nullable = false)
    private Classe classe; // L'ancienne classe

    // ======= REPÈRES TEMPORELS GLOBAUX =======
    @ManyToOne
    @JoinColumn(name = "annee_academique_id")
    private AnneeAcademique anneeAcademique;

    @ManyToOne
    @JoinColumn(name = "semestre_id")
    private Semestre semestre;

    private LocalDate dateEntree;  // Date d'arrivée dans cette classe
    private LocalDate dateSortie;  // Date de départ de cette classe
    private String motif;          // "Migration", "Promotion", "Redoublement", etc.
}
