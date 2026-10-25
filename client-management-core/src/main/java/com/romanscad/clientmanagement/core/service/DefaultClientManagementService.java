package com.romanscad.clientmanagement.core.service;

import com.romanscad.clientmanagement.core.domain.Customer;
import com.romanscad.clientmanagement.core.domain.CustomerWithGroups;
import com.romanscad.clientmanagement.core.domain.Group;
import com.romanscad.clientmanagement.core.exception.CustomerNotFoundException;
import com.romanscad.clientmanagement.core.exception.DuplicateGroupNameException;
import com.romanscad.clientmanagement.core.exception.GroupNotFoundException;
import com.romanscad.clientmanagement.core.exception.ValidationException;
import com.romanscad.clientmanagement.core.pagination.Page;
import com.romanscad.clientmanagement.core.pagination.PageRequest;
import com.romanscad.clientmanagement.core.repository.CustomerRepository;
import com.romanscad.clientmanagement.core.repository.GroupRepository;
import com.romanscad.clientmanagement.core.repository.MembershipRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 * Implémentation des cas d'usage : applique les règles métier en coordonnant les trois dépôts.
 *
 * Dépendances : uniquement des interfaces, fournies par le constructeur (inversion de dépendances).
 * Cette classe ne sait pas que les données sont en mémoire.
 *
 * Concurrence : un ReadWriteLock protège les règles qui traversent plusieurs structures
 * (existence avant association, cascades, unicité du nom, cohérence des deux index).
 * - Les opérations qui modifient plusieurs structures ou font "vérifier puis agir" prennent le
 *   verrou en ÉCRITURE (exclusif).
 * - Les lectures qui croisent plusieurs structures prennent le verrou en LECTURE (partagé).
 * - Les opérations qui ne touchent qu'une seule structure thread-safe n'ont pas besoin de verrou.
 */
public final class DefaultClientManagementService
        implements CustomerService, GroupService, MembershipService {

    private final CustomerRepository customers;
    private final GroupRepository groups;
    private final MembershipRepository memberships;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    public DefaultClientManagementService(CustomerRepository customers,
                                          GroupRepository groups,
                                          MembershipRepository memberships) {
        this.customers = Objects.requireNonNull(customers, "customers must not be null");
        this.groups = Objects.requireNonNull(groups, "groups must not be null");
        this.memberships = Objects.requireNonNull(memberships, "memberships must not be null");
    }

    // ------------------------------------------------------------------ clients

    @Override
    public Customer createCustomer(String firstName, String lastName, String email) {
        // Une seule structure, thread-safe, aucune règle croisée : pas de verrou nécessaire.
        return customers.create(firstName, lastName, email);
    }

    @Override
    public Customer getCustomer(long customerId) {
        return requireCustomer(customerId);
    }

    @Override
    public void deleteCustomer(long customerId) {
        runWithWriteLock(() -> {
            if (!customers.deleteById(customerId)) {
                throw new CustomerNotFoundException(customerId);
            }
            // Cascade : sans elle, des groupes référenceraient un client disparu.
            memberships.unlinkAllOfCustomer(customerId);
        });
    }

    @Override
    public Page<Customer> listCustomers(PageRequest pageRequest) {
        return customers.findAll(pageRequest);
    }

    // ------------------------------------------------------------------ groupes

    @Override
    public Group createGroup(String name, String description) {
        // "Vérifier que le nom est libre, puis créer" doit être indivisible, sinon deux appels
        // simultanés pourraient créer deux groupes du même nom.
        return withWriteLock(() -> {
            groups.findByName(name).ifPresent(existing -> {
                throw new DuplicateGroupNameException(existing.name());
            });
            return groups.create(name, description);
        });
    }

    @Override
    public Group getGroup(long groupId) {
        return requireGroup(groupId);
    }

    @Override
    public void deleteGroup(long groupId) {
        runWithWriteLock(() -> {
            if (!groups.deleteById(groupId)) {
                throw new GroupNotFoundException(groupId);
            }
            memberships.unlinkAllOfGroup(groupId); // les clients, eux, restent
        });
    }

    @Override
    public Page<Group> listGroups(PageRequest pageRequest) {
        return groups.findAll(pageRequest);
    }

    // ------------------------------------------------------------- appartenances

    @Override
    public int addCustomersToGroup(long groupId, Collection<Long> customerIds) {
        return changeMemberships(groupId, customerIds, memberships::link);
    }

    @Override
    public int removeCustomersFromGroup(long groupId, Collection<Long> customerIds) {
        return changeMemberships(groupId, customerIds, memberships::unlink);
    }

    @Override
    public Page<Customer> listCustomersOfGroup(long groupId, PageRequest pageRequest) {
        return withReadLock(() -> {
            requireGroup(groupId);
            return memberships.findCustomerIdsByGroup(groupId, pageRequest)
                    .map(this::customerOfMembership);
        });
    }

    @Override
    public Page<Group> listGroupsOfCustomer(long customerId, PageRequest pageRequest) {
        return withReadLock(() -> {
            requireCustomer(customerId);
            return memberships.findGroupIdsByCustomer(customerId, pageRequest)
                    .map(this::groupOfMembership);
        });
    }

    @Override
    public Page<CustomerWithGroups> listCustomersWithGroups(PageRequest pageRequest) {
        // Un seul verrou de lecture pour toute la page : la vue est cohérente du début à la fin.
        return withReadLock(() -> customers.findAll(pageRequest)
                .map(customer -> new CustomerWithGroups(customer, allGroupsOf(customer.id()))));
    }

    /**
     * Cœur commun de l'association et de la dissociation (seule l'opération appliquée change).
     * Tout est vérifié AVANT la première modification : l'opération est atomique (tout ou rien).
     */
    private int changeMemberships(long groupId, Collection<Long> customerIds, LinkOperation operation) {
        Set<Long> ids = validatedIds(customerIds);
        return withWriteLock(() -> {
            requireGroup(groupId);
            for (long customerId : ids) {
                requireCustomer(customerId);
            }
            int changed = 0;
            for (long customerId : ids) {
                if (operation.apply(customerId, groupId)) {
                    changed++;
                }
            }
            return changed;
        });
    }

    /** Opération sur une association : memberships::link ou memberships::unlink (pattern Strategy). */
    @FunctionalInterface
    private interface LinkOperation {
        boolean apply(long customerId, long groupId);
    }

    // ------------------------------------------------------------------ outils

    private Customer requireCustomer(long customerId) {
        return customers.findById(customerId).orElseThrow(() -> new CustomerNotFoundException(customerId));
    }

    private Group requireGroup(long groupId) {
        return groups.findById(groupId).orElseThrow(() -> new GroupNotFoundException(groupId));
    }

    /**
     * Client référencé par une association. S'il est introuvable, c'est un BUG interne (le verrou
     * doit l'empêcher), pas une erreur de l'appelant : d'où IllegalStateException.
     */
    private Customer customerOfMembership(long customerId) {
        return customers.findById(customerId).orElseThrow(
                () -> new IllegalStateException("Membership refers to unknown customer " + customerId));
    }

    private Group groupOfMembership(long groupId) {
        return groups.findById(groupId).orElseThrow(
                () -> new IllegalStateException("Membership refers to unknown group " + groupId));
    }

    /** Tous les groupes d'un client (hypothèse : peu de groupes par client), en parcourant les pages. */
    private List<Group> allGroupsOf(long customerId) {
        List<Group> result = new ArrayList<>();
        Page<Long> page = memberships.findGroupIdsByCustomer(customerId, PageRequest.firstPage());
        while (true) {
            for (long groupId : page.items()) {
                result.add(groupOfMembership(groupId));
            }
            if (!page.hasNext()) {
                return result;
            }
            page = memberships.findGroupIdsByCustomer(customerId, PageRequest.after(page.nextCursor()));
        }
    }

    /** Refuse une liste nulle, vide ou contenant null ; supprime les doublons en gardant l'ordre. */
    private static Set<Long> validatedIds(Collection<Long> customerIds) {
        if (customerIds == null || customerIds.isEmpty()) {
            throw new ValidationException("customerIds must not be empty");
        }
        Set<Long> ids = new LinkedHashSet<>();
        for (Long id : customerIds) {
            if (id == null) {
                throw new ValidationException("customerIds must not contain null");
            }
            ids.add(id);
        }
        return ids;
    }

    private <T> T withReadLock(Supplier<T> action) {
        readLock.lock();
        try {
            return action.get();
        } finally {
            readLock.unlock(); // toujours relâché, même si l'action lève une exception
        }
    }

    private <T> T withWriteLock(Supplier<T> action) {
        writeLock.lock();
        try {
            return action.get();
        } finally {
            writeLock.unlock();
        }
    }

    private void runWithWriteLock(Runnable action) {
        writeLock.lock();
        try {
            action.run();
        } finally {
            writeLock.unlock();
        }
    }
}