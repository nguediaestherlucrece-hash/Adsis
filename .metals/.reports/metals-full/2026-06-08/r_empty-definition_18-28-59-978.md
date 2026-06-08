error id: file:///C:/Users/joetech/OneDrive/Dokumente/projet%20tutore/now/25_26_ING_3_ISI_FR_GROUPE6/first_projet_springboot/src/main/java/com/ISI/Appel_classe/Gestion_des_emplois_du_temps/model/Seance.java:com/ISI/Appel_classe/Gestion_des_utilisateurs/model/Surveillant#
file:///C:/Users/joetech/OneDrive/Dokumente/projet%20tutore/now/25_26_ING_3_ISI_FR_GROUPE6/first_projet_springboot/src/main/java/com/ISI/Appel_classe/Gestion_des_emplois_du_temps/model/Seance.java
empty definition using pc, found symbol in pc: com/ISI/Appel_classe/Gestion_des_utilisateurs/model/Surveillant#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 336
uri: file:///C:/Users/joetech/OneDrive/Dokumente/projet%20tutore/now/25_26_ING_3_ISI_FR_GROUPE6/first_projet_springboot/src/main/java/com/ISI/Appel_classe/Gestion_des_emplois_du_temps/model/Seance.java
text:
```scala
package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Appel;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Enseignant;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Presence;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.@@Surveillant;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.CascadeType;

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

    @Column(unique = true)
    private String idSeance;

    private LocalDate date;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private boolean statut;
    private String couleur;   // Couleur hex (ex: "#0f22f1")
    private String groupId;   // Identifiant de groupe pour séances multi-jour

    // 🔹 Relations JPA
    @ManyToOne
    @JoinColumn(name = "cours_id")
    private CoursUE cours;

    @ManyToMany
    @JoinTable(name = "seance_enseignant", joinColumns = @JoinColumn(name = "seance_id"), inverseJoinColumns = @JoinColumn(name = "enseignant_id"))
    private List<Enseignant> enseignants = new ArrayList<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "seance_surveillant", joinColumns = @JoinColumn(name = "seance_id"), inverseJoinColumns = @JoinColumn(name = "surveillant_id"))
    private List<Surveillant> surveillants = new ArrayList<>();

    @OneToMany(mappedBy = "seance", cascade = CascadeType.ALL)
    private List<Appel> appels = new ArrayList<>();

    @OneToMany(mappedBy = "seance", cascade = CascadeType.ALL, orphanRemoval = true)
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

```


#### Short summary: 

empty definition using pc, found symbol in pc: com/ISI/Appel_classe/Gestion_des_utilisateurs/model/Surveillant#