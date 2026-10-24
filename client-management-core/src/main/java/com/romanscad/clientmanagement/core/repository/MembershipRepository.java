package com.romanscad.clientmanagement.core.repository;

import com.romanscad.clientmanagement.core.pagination.Page;
import com.romanscad.clientmanagement.core.pagination.PageRequest;

/**
 * Stockage des associations client/groupe (port).
 *
 * Il ne manipule que des identifiants : il ignore tout des clients et des groupes eux-mêmes.
 * Le contrôle d'existence est fait par le service.
 *
 * Une association est bidirectionnelle : on peut la consulter dans les deux sens
 * (clients d'un groupe, groupes d'un client) en O(log n), car les implémentations
 * maintiennent un index dans chaque sens.
 */
public interface MembershipRepository {

    /** @return true si l'association vient d'être créée, false si elle existait déjà */
    boolean link(long customerId, long groupId);

    /** @return true si l'association existait et a été supprimée */
    boolean unlink(long customerId, long groupId);

    /** Identifiants des clients d'un groupe, triés, par curseur. */
    Page<Long> findCustomerIdsByGroup(long groupId, PageRequest pageRequest);

    /** Identifiants des groupes d'un client, triés, par curseur. */
    Page<Long> findGroupIdsByCustomer(long customerId, PageRequest pageRequest);

    /** Supprime toutes les associations d'un client (cascade lors de sa suppression). */
    void unlinkAllOfCustomer(long customerId);

    /** Supprime toutes les associations d'un groupe (cascade lors de sa suppression). */
    void unlinkAllOfGroup(long groupId);
}