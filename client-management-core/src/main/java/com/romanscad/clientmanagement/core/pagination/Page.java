package com.romanscad.clientmanagement.core.pagination;

import java.util.List;
import java.util.function.Function;

/**
 * Une page de résultats.
 *
 * @param items      éléments de la page (liste immuable, triée par identifiant croissant)
 * @param nextCursor valeur à passer comme afterId pour obtenir la page suivante,
 *                   ou null s'il n'y a plus de résultat
 * @param <T>        type des éléments
 */
public record Page<T>(List<T> items, Long nextCursor) {

    public Page {
        // Copie défensive : la page reste immuable même si l'appelant modifie sa liste.
        items = List.copyOf(items);
    }

    /** Page vide, sans suite. */
    public static <T> Page<T> empty() {
        return new Page<>(List.of(), null);
    }

    /** Vrai s'il reste des éléments après cette page. */
    public boolean hasNext() {
        return nextCursor != null;
    }

    /** Transforme les éléments en conservant le curseur (par exemple des ids en entités). */
    public <R> Page<R> map(Function<? super T, ? extends R> mapper) {
        return new Page<>(items.stream().<R>map(mapper).toList(), nextCursor);
    }
}