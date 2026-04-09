package com.ISI.Appel_classe.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {

    @Email(message = "Email doit être valide")
    @NotBlank(message = "Email ne peut pas être vide")
    private String email;

    @NotBlank(message = "Mot de passe ne peut pas être vide")
    private String password;
}
