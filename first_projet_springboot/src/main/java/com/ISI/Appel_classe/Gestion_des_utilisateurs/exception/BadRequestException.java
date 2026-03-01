package com.ISI.Appel_classe.Gestion_des_utilisateurs.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
