package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.AssistantPedagogique;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Classe;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Seance;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "emploi_du_temps")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmploiDuTemps {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String idEmploi;

    private String periode; 
    private String creneaux;

    
    @OneToOne(mappedBy = "emploiDuTemps") 
    private Classe classe;

    @ManyToOne
    @JoinColumn(name = "gestionnaire_id")
    private AssistantPedagogique gestionnaire;

    @OneToMany(mappedBy = "emploiDuTemps")
    private List<Seance> seances = new ArrayList<>();

    public void planifierSeance() {
    }

    public void modifierSeance() {
    }
}
