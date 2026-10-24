package com.romanscad.clientmanagement.core.repository.memory;

import com.romanscad.clientmanagement.core.pagination.Page;
import com.romanscad.clientmanagement.core.pagination.PageRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.ToLongFunction;

/**
 * Construction d'une page à partir d'un itérateur déjà positionné après le curseur.
 * Partagé par tous les dépôts en mémoire (clients, groupes, associations).
 */
final class Pages {

    private Pages() {
        // Classe utilitaire : non instanciable
    }

    /**
     * Lit au plus limit éléments, puis regarde s'il en reste un derrière.
     *
     * @param iterator itérateur trié par identifiant, positionné juste après le curseur
     * @param limit    taille maximale de la page
     * @param idOf     extrait l'identifiant d'un élément (sert à calculer le curseur suivant)
     */
    static <T> Page<T> collect(Iterator<T> iterator, int limit, ToLongFunction<T> idOf) {
        List<T> items = new ArrayList<>(Math.min(limit, PageRequest.DEFAULT_LIMIT));
        while (items.size() < limit && iterator.hasNext()) {
            items.add(iterator.next());
        }
        // Il reste des éléments seulement si l'itérateur en a encore un après la page.
        Long nextCursor = iterator.hasNext() ? idOf.applyAsLong(items.getLast()) : null;
        return new Page<>(items, nextCursor);
    }
}