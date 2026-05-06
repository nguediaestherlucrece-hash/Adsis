package com.ISI.Appel_classe.Gestion_des_justificatifs.model;

import com.ISI.Appel_classe.Gestion_des_justificatifs.enums.StatutValidation;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.AssistantPedagogique;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Presence;
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
    private String commentaire;
    private String documentUrl;

    @Enumerated(EnumType.STRING)
    private StatutValidation statutValidation;

    private String motifRejet;
    private LocalDate dateSoumission;
    private LocalDate dateValidation;

    private String complementDocumentUrl;
    private String complementCommentaire;
    private LocalDate dateComplement;

    // 🔹 Relations JPA
    @OneToOne
    @JoinColumn(name = "presence_id")
    private Presence presence;

    @ManyToOne
    @JoinColumn(name = "validateur_id")
    private AssistantPedagogique validateur;
}
