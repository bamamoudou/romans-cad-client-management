package com.romanscad.clientmanagement.rest.error;

/**
 * Erreur destinée à devenir une réponse HTTP. Les couches internes la lèvent ; seul
 * ApiExceptionMapper sait la transformer en réponse, ce qui garde le HTTP hors de la logique.
 */
public final class ApiException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int status;
    private final String code;

    public ApiException(int status, String code, String message) {
        this(status, code, message, null);
    }

    public ApiException(int status, String code, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.code = code;
    }

    /** Code de statut HTTP. */
    public int status() {
        return status;
    }

    /** Code d'erreur applicatif stable, destiné aux programmes (par exemple CUSTOMER_NOT_FOUND). */
    public String code() {
        return code;
    }
}