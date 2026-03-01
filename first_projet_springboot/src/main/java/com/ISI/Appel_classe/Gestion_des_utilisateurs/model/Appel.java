package com.ISI.Appel_classe.Gestion_des_utilisateurs.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "appel")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Appel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String idAppel;
    private LocalDateTime dateHeureDebut;
    private LocalDateTime dateHeureFin;
    private String statut;

    // 🔹 Relations JPA
    @ManyToOne
    @JoinColumn(name = "seance_id")
    private Seance seance;

    @OneToMany(mappedBy = "appel", cascade = CascadeType.ALL)
    private List<Presence> presences = new ArrayList<>();

    // 🔹 Méthodes de logique métier
    public void demarrer() {
        this.statut = "ouvert";
        this.dateHeureDebut = LocalDateTime.now();
        // TODO: logique de démarrage d'appel
    }

    public void cloturer() {
        this.statut = "clôturé";
        this.dateHeureFin = LocalDateTime.now(); // Assuming cloturer sets the end time
        // TODO: logique de clôture d'appel
    }

    public void enregistrerPresence(Presence presence) {
        presences.add(presence);
        presence.setAppel(this);
        // TODO: logique d'enregistrement de présence
    }

    public void annulerEnregistrement(Presence presence) {
        presences.remove(presence);
        presence.setAppel(null);
        // TODO: logique d'annulation d'enregistrement
    }
}
