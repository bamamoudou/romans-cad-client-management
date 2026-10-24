package com.romanscad.clientmanagement.core.pagination;

import com.romanscad.clientmanagement.core.exception.ValidationException;

/**
 * Demande de page par curseur (keyset pagination) : « les limit éléments dont l'identifiant
 * est strictement supérieur à afterId ».
 *
 * Pourquoi pas une pagination par offset (« page 3 ») ? Avec 1 million d'entrées, un offset
 * oblige à parcourir tous les éléments précédents (coût O(offset)) et donne des résultats
 * instables si des données sont ajoutées ou supprimées entre deux appels. Le curseur se
 * positionne directement dans la structure triée (coût O(log n)) et reste stable.
 *
 * @param afterId curseur : dernier identifiant déjà reçu (0 pour la première page)
 * @param limit   nombre maximal d'éléments à renvoyer, entre 1 et MAX_LIMIT
 */
public record PageRequest(long afterId, int limit) {

    /** Taille de page par défaut. */
    public static final int DEFAULT_LIMIT = 100;

    /** Taille maximale : protège le serveur contre les réponses démesurées. */
    public static final int MAX_LIMIT = 1_000;

    public PageRequest {
        if (afterId < 0) {
            throw new ValidationException("afterId must be >= 0");
        }
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new ValidationException("limit must be between 1 and " + MAX_LIMIT);
        }
    }

    /** Première page avec la taille par défaut. */
    public static PageRequest firstPage() {
        return new PageRequest(0, DEFAULT_LIMIT);
    }

    /** Page suivant le curseur donné, avec la taille par défaut. */
    public static PageRequest after(long afterId) {
        return new PageRequest(afterId, DEFAULT_LIMIT);
    }
}