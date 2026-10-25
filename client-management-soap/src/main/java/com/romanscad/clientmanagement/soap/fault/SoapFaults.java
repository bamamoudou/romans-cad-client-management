package com.romanscad.clientmanagement.soap.fault;

import com.romanscad.clientmanagement.core.exception.ClientManagementException;
import com.romanscad.clientmanagement.core.exception.CustomerNotFoundException;
import com.romanscad.clientmanagement.core.exception.DuplicateGroupNameException;
import com.romanscad.clientmanagement.core.exception.GroupNotFoundException;
import com.romanscad.clientmanagement.core.exception.ValidationException;
import com.romanscad.clientmanagement.soap.dto.ErrorCode;
import com.romanscad.clientmanagement.soap.dto.ErrorDetail;

/**
 * Traduit les erreurs métier du domaine en fautes SOAP : c'est la frontière entre le coeur
 * et le contrat public.
 *
 * Le switch est exhaustif et n'a volontairement pas de "default" : la hiérarchie des erreurs
 * est scellée, donc le compilateur connaît tous les cas. Si on ajoute une erreur dans le
 * domaine, cette classe ne compile plus tant que la nouvelle erreur n'est pas traitée.
 */
public final class SoapFaults {

    private SoapFaults() {
        // Classe utilitaire : non instanciable
    }

    public static ClientManagementFault from(ClientManagementException exception) {
        ErrorDetail detail = switch (exception) {
            case CustomerNotFoundException e ->
                    new ErrorDetail(ErrorCode.CUSTOMER_NOT_FOUND, e.getMessage(), e.getCustomerId());
            case GroupNotFoundException e ->
                    new ErrorDetail(ErrorCode.GROUP_NOT_FOUND, e.getMessage(), e.getGroupId());
            case DuplicateGroupNameException e ->
                    new ErrorDetail(ErrorCode.DUPLICATE_GROUP_NAME, e.getMessage(), null);
            case ValidationException e ->
                    new ErrorDetail(ErrorCode.INVALID_REQUEST, e.getMessage(), null);
        };
        // L'exception d'origine est gardée comme cause (utile dans les journaux),
        // mais elle ne voyage jamais dans la réponse SOAP.
        return new ClientManagementFault(exception.getMessage(), detail, exception);
    }
}