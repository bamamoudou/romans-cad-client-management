package com.romanscad.clientmanagement.rest.error;

import jakarta.ws.rs.core.Response;

/**
 * Corps d'erreur normalisé : « Problem Details for HTTP APIs » (RFC 9457).
 *
 * @param type       "about:blank" : le statut HTTP suffit à décrire le problème
 * @param title      libellé standard du statut (par exemple "Not Found")
 * @param status     code HTTP
 * @param detail     explication lisible, propre à cette occurrence
 * @param code       extension : code d'erreur stable pour les programmes (omis quand il n'y en a pas)
 * @param resourceId extension : identifiant de la ressource introuvable (omis sinon)
 */
public record ProblemDetail(String type, String title, int status, String detail, String code, Long resourceId) {

    public static final String MEDIA_TYPE = "application/problem+json";

    /** Construit la réponse HTTP complète : statut, type de contenu et corps. */
    public static Response response(int status, String detail, String code) {
        return response(status, detail, code, null);
    }

    public static Response response(int status, String detail, String code, Long resourceId) {
        Response.Status known = Response.Status.fromStatusCode(status);
        String title = known != null ? known.getReasonPhrase() : "Error";
        ProblemDetail problem = new ProblemDetail("about:blank", title, status, detail, code, resourceId);
        return Response.status(status).type(MEDIA_TYPE).entity(problem).build();
    }
}