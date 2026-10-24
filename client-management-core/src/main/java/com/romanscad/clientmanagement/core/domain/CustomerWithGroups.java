package com.romanscad.clientmanagement.core.domain;

import java.util.List;
import java.util.Objects;

/**
 * Un client accompagné des groupes auxquels il appartient : c'est le résultat de la vue
 * « clients et groupes associés » demandée dans l'énoncé.
 *
 * Hypothèse : un client appartient à un nombre limité de groupes (le nombre de groupes est
 * faible face à 1 million de clients). On peut donc embarquer la liste complète de ses groupes ;
 * ce sont les clients qui sont paginés.
 *
 * @param customer le client
 * @param groups   ses groupes (liste immuable, éventuellement vide)
 */
public record CustomerWithGroups(Customer customer, List<Group> groups) {

    public CustomerWithGroups {
        Objects.requireNonNull(customer, "customer must not be null");
        // Copie défensive : l'objet reste immuable même si l'appelant modifie sa liste ensuite.
        groups = List.copyOf(groups);
    }
}