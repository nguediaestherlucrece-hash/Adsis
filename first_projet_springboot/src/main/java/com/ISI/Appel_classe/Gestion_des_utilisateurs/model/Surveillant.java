package com.ISI.Appel_classe.Gestion_des_utilisateurs.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "surveillant")
@DiscriminatorValue("SURVEILLANT")
@PrimaryKeyJoinColumn(name = "utilisateur_id")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Surveillant extends Utilisateur {

    private String affectation;

    // 🔹 Relations JPA
    @ManyToMany(mappedBy = "surveillants")
    private List<Seance> seances = new ArrayList<>();
}
