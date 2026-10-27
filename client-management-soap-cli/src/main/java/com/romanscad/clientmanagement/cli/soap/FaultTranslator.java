package com.romanscad.clientmanagement.cli.soap;

import com.romanscad.clientmanagement.cli.soap.generated.ClientManagementFault;
import com.romanscad.clientmanagement.cli.soap.generated.ErrorDetail;
import com.romanscad.clientmanagement.core.exception.ClientManagementException;
import com.romanscad.clientmanagement.core.exception.CustomerNotFoundException;
import com.romanscad.clientmanagement.core.exception.DuplicateGroupNameException;
import com.romanscad.clientmanagement.core.exception.GroupNotFoundException;
import com.romanscad.clientmanagement.core.exception.ValidationException;

/**
 * Retraduit une faute SOAP en exception du domaine : l'inverse de ce que fait le serveur.
 *
 * C'est ce qui permet au moteur de la ligne de commande de traiter toutes les erreurs de la même
 * façon, quel que soit le transport : pour lui, un client introuvable est toujours une
 * CustomerNotFoundException.
 *
 * Le switch sur le code d'erreur n'a pas de "default" : si le serveur ajoute un code et qu'on
 * régénère les stubs, cette classe ne compile plus tant que le nouveau code n'est pas traité.
 */
final class FaultTranslator {

    private FaultTranslator() {
        // Classe utilitaire : non instanciable
    }

    /**
     * @param fault     la faute reçue du serveur
     * @param groupName nom du groupe que l'appel essayait de créer, ou null pour les autres appels.
     *                  La faute ne contient pas ce nom, et l'exception de doublon en a besoin.
     */
    static ClientManagementException translate(ClientManagementFault fault, String groupName) {
        ErrorDetail detail = fault.getFaultInfo();
        if (detail == null || detail.getCode() == null) {
            throw new IllegalStateException("The server sent a fault without an error code: " + fault.getMessage(), fault);
        }
        return switch (detail.getCode()) {
            case CUSTOMER_NOT_FOUND -> new CustomerNotFoundException(requireResourceId(detail));
            case GROUP_NOT_FOUND -> new GroupNotFoundException(requireResourceId(detail));
            case DUPLICATE_GROUP_NAME -> new DuplicateGroupNameException(groupName != null ? groupName : "(unknown)");
            case INVALID_REQUEST -> new ValidationException(detail.getMessage());
        };
    }

    private static long requireResourceId(ErrorDetail detail) {
        if (detail.getResourceId() == null) {
            throw new IllegalStateException("The server sent a " + detail.getCode() + " fault without a resource id");
        }
        return detail.getResourceId();
    }
}