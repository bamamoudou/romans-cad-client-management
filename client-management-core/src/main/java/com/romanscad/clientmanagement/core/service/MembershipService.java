package com.romanscad.clientmanagement.core.service;

import com.romanscad.clientmanagement.core.domain.Customer;
import com.romanscad.clientmanagement.core.domain.CustomerWithGroups;
import com.romanscad.clientmanagement.core.domain.Group;
import com.romanscad.clientmanagement.core.exception.CustomerNotFoundException;
import com.romanscad.clientmanagement.core.exception.GroupNotFoundException;
import com.romanscad.clientmanagement.core.exception.ValidationException;
import com.romanscad.clientmanagement.core.pagination.Page;
import com.romanscad.clientmanagement.core.pagination.PageRequest;
import java.util.Collection;

/**
 * Cas d'usage liés aux appartenances client/groupe.
 *
 * Les interfaces CustomerService, GroupService et celle-ci sont séparées : un consommateur
 * ne dépend que de ce dont il a besoin.
 */
public interface MembershipService {

    /**
     * Associe des clients à un groupe.
     *
     * L'opération est idempotente (ré-associer un client déjà membre n'est pas une erreur)
     * et atomique : si un client n'existe pas, rien n'est modifié.
     *
     * @return nombre d'associations réellement créées
     * @throws GroupNotFoundException    si le groupe n'existe pas
     * @throws CustomerNotFoundException si un des clients n'existe pas
     * @throws ValidationException       si la liste est vide
     */
    int addCustomersToGroup(long groupId, Collection<Long> customerIds);

    /**
     * Dissocie des clients d'un groupe. Mêmes garanties que addCustomersToGroup.
     *
     * @return nombre d'associations réellement supprimées
     * @throws GroupNotFoundException    si le groupe n'existe pas
     * @throws CustomerNotFoundException si un des clients n'existe pas
     * @throws ValidationException       si la liste est vide
     */
    int removeCustomersFromGroup(long groupId, Collection<Long> customerIds);

    /**
     * Clients d'un groupe, paginés.
     *
     * @throws GroupNotFoundException si le groupe n'existe pas
     */
    Page<Customer> listCustomersOfGroup(long groupId, PageRequest pageRequest);

    /**
     * Groupes d'un client, paginés.
     *
     * @throws CustomerNotFoundException si le client n'existe pas
     */
    Page<Group> listGroupsOfCustomer(long customerId, PageRequest pageRequest);

    /** Clients avec leurs groupes (la pagination porte sur les clients). */
    Page<CustomerWithGroups> listCustomersWithGroups(PageRequest pageRequest);
}