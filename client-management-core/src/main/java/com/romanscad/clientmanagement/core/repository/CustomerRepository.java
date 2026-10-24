package com.romanscad.clientmanagement.core.repository;

import com.romanscad.clientmanagement.core.domain.Customer;
import com.romanscad.clientmanagement.core.pagination.Page;
import com.romanscad.clientmanagement.core.pagination.PageRequest;
import java.util.Optional;

/**
 * Stockage des clients (port).
 *
 * Le métier dépend de cette interface, jamais d'une implémentation : on pourrait remplacer
 * la base en mémoire par une base SQL sans toucher au service. Le dépôt ne fait que stocker :
 * les règles métier (existence, cascade...) vivent dans la couche service.
 *
 * Les implémentations doivent être thread-safe.
 */
public interface CustomerRepository {

    /** Crée un client. L'identifiant est attribué par le dépôt, comme une séquence de base de données. */
    Customer create(String firstName, String lastName, String email);

    Optional<Customer> findById(long id);

    /** @return true si le client existait et a été supprimé */
    boolean deleteById(long id);

    /** Clients triés par identifiant croissant. */
    Page<Customer> findAll(PageRequest pageRequest);

    long count();
}