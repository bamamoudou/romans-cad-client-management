package com.romanscad.clientmanagement.cli.rest;

import com.romanscad.clientmanagement.cli.ServiceUnavailableException;
import com.romanscad.clientmanagement.core.exception.CustomerNotFoundException;
import com.romanscad.clientmanagement.core.exception.DuplicateGroupNameException;
import com.romanscad.clientmanagement.core.exception.GroupNotFoundException;
import com.romanscad.clientmanagement.core.exception.ValidationException;
import jakarta.json.JsonObject;

/**
 * Retraduit une réponse d'erreur de la passerelle (problem+json) en exception du domaine : l'inverse de
 * ce que fait la passerelle.
 *
 * On se fie au champ "code", un contrat stable, jamais au texte du message qui peut changer.
 * Les statuts 502, 503 et 504 signalent un problème en amont (la passerelle n'arrive pas à utiliser le
 * serveur SOAP) : c'est « service indisponible », pas un refus de la demande. Tout ce qui n'est pas
 * reconnu (mauvaise adresse, page d'erreur HTML...) est traité de la même façon, avec le statut HTTP.
 */
final class ProblemTranslator {

    private static final String CUSTOMER_NOT_FOUND = "CUSTOMER_NOT_FOUND";
    private static final String GROUP_NOT_FOUND = "GROUP_NOT_FOUND";
    private static final String DUPLICATE_GROUP_NAME = "DUPLICATE_GROUP_NAME";
    private static final String INVALID_REQUEST = "INVALID_REQUEST";

    private ProblemTranslator() {
        // Classe utilitaire : non instanciable
    }

    /** @param groupName nom du groupe en cours de création, ou null (la réponse ne le contient pas) */
    static RuntimeException translate(HttpResult response, String groupName) {
        JsonObject problem = RestJson.tryParse(response.body());
        String code = problem == null ? null : problem.getString("code", null);
        String detail = problem == null ? null : problem.getString("detail", null);
        Long resourceId = readResourceId(problem);

        if (CUSTOMER_NOT_FOUND.equals(code) && resourceId != null) {
            return new CustomerNotFoundException(resourceId);
        }
        if (GROUP_NOT_FOUND.equals(code) && resourceId != null) {
            return new GroupNotFoundException(resourceId);
        }
        if (DUPLICATE_GROUP_NAME.equals(code)) {
            return new DuplicateGroupNameException(groupName != null ? groupName : "(unknown)");
        }
        if (INVALID_REQUEST.equals(code)) {
            return new ValidationException(detail != null ? detail : "Invalid request");
        }

        String suffix = detail == null ? "" : ": " + detail;
        int status = response.status();
        if (status == 502 || status == 503 || status == 504) {
            return new ServiceUnavailableException(
                    "The REST gateway could not reach or use the SOAP service (HTTP " + status + ")" + suffix, null);
        }
        return new ServiceUnavailableException("Unexpected response from the server: HTTP " + status + suffix, null);
    }

    private static Long readResourceId(JsonObject problem) {
        if (problem == null || !problem.containsKey("resourceId")) {
            return null;
        }
        var value = problem.get("resourceId");
        return value instanceof jakarta.json.JsonNumber number ? number.longValue() : null;
    }
}