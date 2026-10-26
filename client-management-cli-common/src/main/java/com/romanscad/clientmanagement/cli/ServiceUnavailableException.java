package com.romanscad.clientmanagement.cli;

/**
 * Le serveur n'a pas pu être joint ou n'a pas répondu à temps (réseau coupé, serveur arrêté,
 * adresse incorrecte, délai dépassé).
 *
 * Elle est lancée par les adaptateurs SOAP et REST, jamais par le domaine : c'est une panne de
 * transport, pas une erreur métier. Le programme l'affiche clairement et sort avec un code dédié.
 */
public final class ServiceUnavailableException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}