package com.romanscad.clientmanagement.core.repository.memory;

import com.romanscad.clientmanagement.core.domain.Customer;
import com.romanscad.clientmanagement.core.pagination.Page;
import com.romanscad.clientmanagement.core.pagination.PageRequest;
import com.romanscad.clientmanagement.core.repository.CustomerRepository;
import java.util.Optional;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Base de clients en mémoire, conçue pour plus d'un million d'entrées.
 *
 * Structure : ConcurrentSkipListMap triée par identifiant.
 * - accès, ajout, suppression : O(log n)
 * - positionnement sur un curseur : O(log n), puis lecture séquentielle
 * - thread-safe sans verrou global : plusieurs lectures et écritures en parallèle
 *
 * Remarque : ConcurrentSkipListMap.size() parcourt toute la map (O(n)). On tient donc
 * un compteur à part pour que count() reste instantané.
 */
public final class InMemoryCustomerRepository implements CustomerRepository {

    private final ConcurrentNavigableMap<Long, Customer> customers = new ConcurrentSkipListMap<>();

    /** Séquence d'identifiants : atomique, donc sans doublon même en parallèle. */
    private final AtomicLong sequence = new AtomicLong();

    /** Nombre de clients : LongAdder est plus efficace qu'AtomicLong quand beaucoup de threads écrivent. */
    private final LongAdder size = new LongAdder();

    @Override
    public Customer create(String firstName, String lastName, String email) {
        // Comme une séquence SQL, un identifiant peut être "perdu" si les données sont refusées.
        Customer customer = new Customer(sequence.incrementAndGet(), firstName, lastName, email);
        customers.put(customer.id(), customer);
        size.increment();
        return customer;
    }

    @Override
    public Optional<Customer> findById(long id) {
        return Optional.ofNullable(customers.get(id));
    }

    @Override
    public boolean deleteById(long id) {
        // remove() est atomique : un seul thread obtient l'ancienne valeur, donc le compteur reste juste.
        if (customers.remove(id) != null) {
            size.decrement();
            return true;
        }
        return false;
    }

    @Override
    public Page<Customer> findAll(PageRequest pageRequest) {
        // tailMap(..., false) = vue des clés strictement supérieures au curseur, sans copie.
        var afterCursor = customers.tailMap(pageRequest.afterId(), false).values().iterator();
        return Pages.collect(afterCursor, pageRequest.limit(), Customer::id);
    }

    @Override
    public long count() {
        return size.sum();
    }
}