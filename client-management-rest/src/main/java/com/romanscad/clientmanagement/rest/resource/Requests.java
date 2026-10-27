package com.romanscad.clientmanagement.rest.resource;

import com.romanscad.clientmanagement.rest.dto.MembershipRequest;
import com.romanscad.clientmanagement.rest.error.ApiException;
import java.util.List;

/** Contrôles de forme des requêtes. La validation métier (email valide, taille maximale...) reste au serveur SOAP. */
final class Requests {

    private Requests() {
        // Classe utilitaire : non instanciable
    }

    /** Un corps JSON absent ou "null" n'a pas de sens : 400 plutôt qu'une NullPointerException. */
    static <T> T requireBody(T body) {
        if (body == null) {
            throw new ApiException(400, "INVALID_REQUEST", "A JSON request body is required");
        }
        return body;
    }

    static List<Long> requireCustomerIds(MembershipRequest request) {
        List<Long> ids = requireBody(request).customerIds();
        if (ids == null || ids.stream().anyMatch(id -> id == null)) {
            throw new ApiException(400, "INVALID_REQUEST", "'customerIds' must be a list of customer identifiers");
        }
        return ids;
    }
}