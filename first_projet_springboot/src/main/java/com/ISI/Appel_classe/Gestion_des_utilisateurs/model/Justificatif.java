package com.ISI.Appel_classe.Gestion_des_utilisateurs.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "justificatif")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Justificatif {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String idJustificatif;

    private String motif;
    private String documentUrl;
    private String statutValidation;
    private LocalDate dateSoumission;

    // 🔹 Relations JPA
    @OneToOne
    @JoinColumn(name = "presence_id")
    private Presence presence;

    @ManyToOne
    @JoinColumn(name = "validateur_id")
    private AssistantPedagogique validateur;

    public void validerJustificatif() {
        this.statutValidation = "validé";
    }

    public void rejeterJustificatif() {
        this.statutValidation = "rejeté";
    }
}
