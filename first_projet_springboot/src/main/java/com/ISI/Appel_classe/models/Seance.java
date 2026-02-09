package com.ISI.Appel_classe.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "seance")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Seance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String idSeance;
    private LocalDate date; // Changé de String à LocalDate
    private LocalTime heureDebut; // Changé de String à LocalTime
    private LocalTime heureFin; // Changé de String à LocalTime
    private String statut; // "ouverte", "clôturée"

    // 🔹 Relations JPA
    @ManyToOne
    @JoinColumn(name = "cours_id")
    private CoursUE cours;

    @ManyToOne
    @JoinColumn(name = "enseignant_id")
    private Enseignant enseignant;

    @ManyToMany
    @JoinTable(name = "seance_surveillant", joinColumns = @JoinColumn(name = "seance_id"), inverseJoinColumns = @JoinColumn(name = "surveillant_id"))
    private List<Surveillant> surveillants = new ArrayList<>();

    @OneToMany(mappedBy = "seance", cascade = CascadeType.ALL)
    private List<Appel> appels = new ArrayList<>();

    @OneToMany(mappedBy = "seance")
    private List<Presence> presences = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "emploi_du_temps_id")
    private EmploiDuTemps emploiDuTemps;

    // 🔹 Méthodes de logique métier
    public void ouvrirSeance() {
        // TODO: logique d'ouverture de séance
    }

    public void cloturerSeance() {
        // TODO: logique de clôture de séance
    }
}