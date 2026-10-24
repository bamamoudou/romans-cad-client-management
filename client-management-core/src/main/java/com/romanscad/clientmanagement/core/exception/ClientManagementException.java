package com.romanscad.clientmanagement.core.exception;

/**
 * Racine de toutes les erreurs métier du module core.
 *
 * La hiérarchie est scellée (sealed) : la liste des erreurs possibles est fermée et connue
 * du compilateur. Les couches SOAP et REST pourront donc les traduire avec un switch
 * exhaustif : une nouvelle erreur non traitée empêchera la compilation.
 *
 * Les exceptions sont non vérifiées (RuntimeException) : elles ne polluent pas les signatures
 * du domaine. C'est la couche d'exposition qui les convertit en faute SOAP ou en code HTTP.
 */
public abstract sealed class ClientManagementException extends RuntimeException
        permits CustomerNotFoundException, GroupNotFoundException,
        DuplicateGroupNameException, ValidationException {

    private static final long serialVersionUID = 1L;

    protected ClientManagementException(String message) {
        super(message);
    }
}