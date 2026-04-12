package com.ISI.Appel_classe.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String accessToken;

    private String refreshToken;

    private String tokenType = "Bearer";

    private Long expiresIn;

    private Long id;

    private String email;

    private String nom;

    private String prenom;

    /** Ex: ["ENSEIGNANT"] */
    private List<String> roles;

    /** Permissions effectives (rôle - overrides retirées) */
    private List<String> permissions;
}
