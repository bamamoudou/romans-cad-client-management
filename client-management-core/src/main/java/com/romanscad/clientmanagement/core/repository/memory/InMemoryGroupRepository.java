package com.romanscad.clientmanagement.core.repository.memory;

import com.romanscad.clientmanagement.core.domain.Group;
import com.romanscad.clientmanagement.core.pagination.Page;
import com.romanscad.clientmanagement.core.pagination.PageRequest;
import com.romanscad.clientmanagement.core.repository.GroupRepository;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Base de groupes en mémoire.
 *
 * Deux structures, chacune adaptée à son usage :
 * - groups : ConcurrentSkipListMap triée par identifiant (accès par id et pagination par curseur)
 * - idsByName : ConcurrentHashMap nom normalisé -> identifiant (recherche par nom en O(1))
 *
 * L'index par nom sert à contrôler l'unicité des noms. La règle elle-même (refuser un doublon)
 * appartient au service : ce dépôt se contente de fournir la recherche.
 */
public final class InMemoryGroupRepository implements GroupRepository {

    private final ConcurrentNavigableMap<Long, Group> groups = new ConcurrentSkipListMap<>();

    /** Index secondaire : clé = nom en minuscules, valeur = identifiant du groupe. */
    private final ConcurrentMap<String, Long> idsByName = new ConcurrentHashMap<>();

    private final AtomicLong sequence = new AtomicLong();
    private final LongAdder size = new LongAdder();

    @Override
    public Group create(String name, String description) {
        Group group = new Group(sequence.incrementAndGet(), name, description);
        groups.put(group.id(), group);
        idsByName.put(nameKey(group.name()), group.id());
        size.increment();
        return group;
    }

    @Override
    public Optional<Group> findById(long id) {
        return Optional.ofNullable(groups.get(id));
    }

    @Override
    public Optional<Group> findByName(String name) {
        if (name == null) {
            return Optional.empty();
        }
        Long id = idsByName.get(nameKey(name));
        return id == null ? Optional.empty() : findById(id);
    }

    @Override
    public boolean deleteById(long id) {
        Group removed = groups.remove(id);
        if (removed == null) {
            return false;
        }
        // remove(clé, valeur) : n'enlève l'entrée que si elle pointe bien vers CE groupe.
        idsByName.remove(nameKey(removed.name()), removed.id());
        size.decrement();
        return true;
    }

    @Override
    public Page<Group> findAll(PageRequest pageRequest) {
        var afterCursor = groups.tailMap(pageRequest.afterId(), false).values().iterator();
        return Pages.collect(afterCursor, pageRequest.limit(), Group::id);
    }

    @Override
    public long count() {
        return size.sum();
    }

    /** Clé d'index : sans espaces superflus, en minuscules (Locale.ROOT : résultat identique sur toute machine). */
    private static String nameKey(String name) {
        return name.strip().toLowerCase(Locale.ROOT);
    }
}