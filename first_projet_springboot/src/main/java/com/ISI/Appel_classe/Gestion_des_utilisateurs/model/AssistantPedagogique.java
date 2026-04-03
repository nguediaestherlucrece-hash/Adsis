package com.ISI.Appel_classe.Gestion_des_utilisateurs.model;

import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.EmploiDuTemps;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assistant_pedagogique")
@DiscriminatorValue("ASSISTANT")
@PrimaryKeyJoinColumn(name = "utilisateur_id")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AssistantPedagogique extends Utilisateur {

    // 🔹 Relations JPA
    @ManyToOne
    @JoinColumn(name = "filiere_id")
    private Filiere filiere;

    @OneToMany(mappedBy = "validateur") // Nécessite que le champ 'validateur' existe dans Justificatif
    private List<Justificatif> justificatifsValides = new ArrayList<>();

    @OneToMany(mappedBy = "gestionnaire") // Nécessite que le champ 'gestionnaire' existe dans EmploiDuTemps
    private List<EmploiDuTemps> emploisDuTemps = new ArrayList<>();
}
