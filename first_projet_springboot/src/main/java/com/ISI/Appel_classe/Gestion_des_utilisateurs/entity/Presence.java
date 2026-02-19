package com.Gestion_des_utilisateurs.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "presence")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Presence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String idPresence;
    private LocalDateTime horodatage;
    private String statut; // "présent", "absent", "retard"
    private String modeEnregistrement; // "QR", "PIN", "manuel"
    private String localisation; // Optionnel

    // 🔹 Relations JPA
    @ManyToOne
    @JoinColumn(name = "etudiant_id")
    private Etudiant etudiant;

    @ManyToOne
    @JoinColumn(name = "seance_id")
    private Seance seance;

    @ManyToOne
    @JoinColumn(name = "appel_id")
    private Appel appel;

    @OneToOne(mappedBy = "presence", cascade = CascadeType.ALL)
    private Justificatif justificatif;
}
