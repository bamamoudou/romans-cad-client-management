package com.romanscad.clientmanagement.cli.rest;

/**
 * Réponse HTTP brute : statut et corps.
 *
 * @param status code HTTP
 * @param body   corps de la réponse (peut être vide)
 */
record HttpResult(int status, String body) {

    boolean isSuccess() {
        return status >= 200 && status < 300;
    }

    /** Le corps si la réponse est un succès ; sinon lève l'exception du domaine correspondante. */
    String bodyOrThrow() {
        return bodyOrThrow(null);
    }

    /**
     * @param groupName nom du groupe en cours de création, ou null : la réponse « nom déjà pris » ne le contient
     *                  pas et l'exception de doublon en a besoin
     */
    String bodyOrThrow(String groupName) {
        if (!isSuccess()) {
            throw ProblemTranslator.translate(this, groupName);
        }
        return body;
    }
}