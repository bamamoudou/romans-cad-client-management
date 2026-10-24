package com.romanscad.clientmanagement.core.repository.memory;

import com.romanscad.clientmanagement.core.pagination.Page;
import com.romanscad.clientmanagement.core.pagination.PageRequest;
import com.romanscad.clientmanagement.core.repository.MembershipRepository;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Associations client/groupe en mémoire, avec un index dans chaque sens :
 * - customerIdsByGroup : groupe -> clients (peut atteindre des centaines de milliers d'éléments)
 * - groupIdsByCustomer : client -> groupes (en général quelques éléments)
 *
 * Les deux sens sont consultables en O(log n), sans jamais parcourir toute la base. Chaque
 * ensemble est un ConcurrentSkipListSet : trié, donc paginable par curseur, comme les clients.
 *
 * Cohérence : chaque index est thread-safe pris isolément, mais une écriture touche les deux.
 * Garantir qu'ils restent synchronisés est le rôle du service, qui sérialise les écritures
 * avec un verrou. Ce dépôt ne le fait pas lui-même pour éviter un second verrou redondant.
 */
public final class InMemoryMembershipRepository implements MembershipRepository {

    private final ConcurrentMap<Long, NavigableSet<Long>> customerIdsByGroup = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, NavigableSet<Long>> groupIdsByCustomer = new ConcurrentHashMap<>();

    @Override
    public boolean link(long customerId, long groupId) {
        // Le résultat de référence est celui de l'index « groupe -> clients ».
        boolean created = addTo(customerIdsByGroup, groupId, customerId);
        addTo(groupIdsByCustomer, customerId, groupId);
        return created;
    }

    @Override
    public boolean unlink(long customerId, long groupId) {
        boolean removed = removeFrom(customerIdsByGroup, groupId, customerId);
        removeFrom(groupIdsByCustomer, customerId, groupId);
        return removed;
    }

    @Override
    public Page<Long> findCustomerIdsByGroup(long groupId, PageRequest pageRequest) {
        return pageOf(customerIdsByGroup, groupId, pageRequest);
    }

    @Override
    public Page<Long> findGroupIdsByCustomer(long customerId, PageRequest pageRequest) {
        return pageOf(groupIdsByCustomer, customerId, pageRequest);
    }

    @Override
    public void unlinkAllOfCustomer(long customerId) {
        NavigableSet<Long> groupIds = groupIdsByCustomer.remove(customerId);
        if (groupIds != null) {
            for (long groupId : groupIds) {
                removeFrom(customerIdsByGroup, groupId, customerId);
            }
        }
    }

    @Override
    public void unlinkAllOfGroup(long groupId) {
        NavigableSet<Long> customerIds = customerIdsByGroup.remove(groupId);
        if (customerIds != null) {
            for (long customerId : customerIds) {
                removeFrom(groupIdsByCustomer, customerId, groupId);
            }
        }
    }

    /**
     * Ajoute value à l'ensemble de key, en le créant au besoin.
     * compute() est atomique par clé : pas de perte d'ajout si deux threads créent le même ensemble.
     */
    private static boolean addTo(ConcurrentMap<Long, NavigableSet<Long>> index, long key, long value) {
        boolean[] added = new boolean[1]; // contourne la limite des lambdas (variables locales finales)
        index.compute(key, (k, set) -> {
            NavigableSet<Long> target = (set != null) ? set : new ConcurrentSkipListSet<>();
            added[0] = target.add(value);
            return target;
        });
        return added[0];
    }

    /**
     * Retire value de l'ensemble de key, et supprime l'ensemble s'il devient vide
     * (sinon la map accumulerait des milliers d'ensembles vides : fuite mémoire).
     */
    private static boolean removeFrom(ConcurrentMap<Long, NavigableSet<Long>> index, long key, long value) {
        boolean[] removed = new boolean[1];
        index.computeIfPresent(key, (k, set) -> {
            removed[0] = set.remove(value);
            return set.isEmpty() ? null : set; // null = supprimer l'entrée
        });
        return removed[0];
    }

    private static Page<Long> pageOf(ConcurrentMap<Long, NavigableSet<Long>> index, long key, PageRequest pageRequest) {
        NavigableSet<Long> ids = index.get(key);
        if (ids == null) {
            return Page.empty();
        }
        var afterCursor = ids.tailSet(pageRequest.afterId(), false).iterator();
        return Pages.collect(afterCursor, pageRequest.limit(), Long::longValue);
    }
}