package com.ISI.Appel_classe.security.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest {
    /**
     * Le refresh token à utiliser pour obtenir un nouveau access token
     */
    private String refreshToken;
}

