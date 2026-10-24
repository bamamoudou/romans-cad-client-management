package com.romanscad.clientmanagement.core.service;

import com.romanscad.clientmanagement.core.domain.Customer;
import com.romanscad.clientmanagement.core.exception.CustomerNotFoundException;
import com.romanscad.clientmanagement.core.exception.ValidationException;
import com.romanscad.clientmanagement.core.pagination.Page;
import com.romanscad.clientmanagement.core.pagination.PageRequest;

/** Cas d'usage liés aux clients. */
public interface CustomerService {

    /**
     * Crée un client.
     *
     * @throws ValidationException si un champ est invalide
     */
    Customer createCustomer(String firstName, String lastName, String email);

    /**
     * @throws CustomerNotFoundException si le client n'existe pas
     */
    Customer getCustomer(long customerId);

    /**
     * Supprime un client et toutes ses associations à des groupes.
     *
     * @throws CustomerNotFoundException si le client n'existe pas
     */
    void deleteCustomer(long customerId);

    /** Liste paginée de tous les clients. */
    Page<Customer> listCustomers(PageRequest pageRequest);
}