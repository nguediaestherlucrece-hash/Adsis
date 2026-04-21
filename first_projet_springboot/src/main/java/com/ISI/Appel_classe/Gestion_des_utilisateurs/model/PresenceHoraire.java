package com.ISI.Appel_classe.Gestion_des_utilisateurs.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Table(name = "presence_horaire")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresenceHoraire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalTime heureDebut;  // ex: 14:00
    private LocalTime heureFin;    // ex: 15:00
    private boolean present;       // true = présent sur ce créneau

    @ManyToOne
    @JoinColumn(name = "presence_id")
    private Presence presence;
}
