package com.ISI.Appel_classe.dto.response;

import com.ISI.Appel_classe.entity.Statut;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class AssistantPedagogiqueDTO {

    // Attributs hérités
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private Statut statut;
    private Boolean enabled;
    private Boolean accountNonLocked;
    private Boolean premiereConnexion;
    private LocalDateTime createdAt;
    private Set<String> roleNames;

    // Attributs spécifiques
    private String departement;

    // Relations
    private Integer nombreJustificatifsValides;
    private Integer nombreEmploisDuTemps;
}
