package com.ISI.Appel_classe.Gestion_des_appels.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnregistrerPresencesBatchRequest {
    
    @NotNull(message = "L'ID de l'appel est obligatoire")
    private Long appelId;
    
    @NotEmpty(message = "La liste des présences ne peut pas être vide")
    private List<EnregistrerPresenceRequest> presences;
}
