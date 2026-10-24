package com.romanscad.clientmanagement.core.exception;

/** Donnée d'entrée invalide : champ vide, trop long, limite de pagination incorrecte... */
public final class ValidationException extends ClientManagementException {

    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message);
    }
}