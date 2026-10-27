package com.romanscad.clientmanagement.rest.error;

/**
 * Erreur destinée à devenir une réponse HTTP. Les couches internes la lèvent ; seul
 * ApiExceptionMapper sait la transformer en réponse, ce qui garde le HTTP hors de la logique.
 */
public final class ApiException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int status;
    private final String code;
    private final Long resourceId;

    public ApiException(int status, String code, String message) {
        this(status, code, message, null, null);
    }

    public ApiException(int status, String code, String message, Throwable cause) {
        this(status, code, message, cause, null);
    }

    /** @param resourceId identifiant de la ressource concernée (pour les « introuvable »), ou null */
    public ApiException(int status, String code, String message, Throwable cause, Long resourceId) {
        super(message, cause);
        this.status = status;
        this.code = code;
        this.resourceId = resourceId;
    }

    /** Code de statut HTTP. */
    public int status() {
        return status;
    }

    /** Code d'erreur applicatif stable, destiné aux programmes (par exemple CUSTOMER_NOT_FOUND). */
    public String code() {
        return code;
    }

    /** Identifiant de la ressource introuvable, ou null. */
    public Long resourceId() {
        return resourceId;
    }
}