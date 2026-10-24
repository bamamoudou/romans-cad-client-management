package com.romanscad.clientmanagement.core.repository.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.romanscad.clientmanagement.core.domain.Customer;
import com.romanscad.clientmanagement.core.pagination.Page;
import com.romanscad.clientmanagement.core.pagination.PageRequest;
import java.util.List;
import org.junit.jupiter.api.Test;

class InMemoryCustomerRepositoryTest {

    private final InMemoryCustomerRepository repository = new InMemoryCustomerRepository();

    private Customer createCustomer(int n) {
        return repository.create("First" + n, "Last" + n, "user" + n + "@example.com");
    }

    private static List<Long> ids(Page<Customer> page) {
        return page.items().stream().map(Customer::id).toList();
    }

    @Test
    void createAssignsIncreasingIds() {
        assertEquals(1, createCustomer(1).id());
        assertEquals(2, createCustomer(2).id());
    }

    @Test
    void findByIdReturnsEmptyWhenUnknown() {
        assertTrue(repository.findById(42).isEmpty());
    }

    @Test
    void deleteReportsWhetherCustomerExisted() {
        Customer customer = createCustomer(1);

        assertTrue(repository.deleteById(customer.id()));
        assertFalse(repository.deleteById(customer.id()));
        assertTrue(repository.findById(customer.id()).isEmpty());
    }

    @Test
    void countFollowsCreationsAndDeletions() {
        createCustomer(1);
        Customer second = createCustomer(2);
        repository.deleteById(second.id());
        repository.deleteById(second.id()); // déjà supprimé : ne doit pas décrémenter deux fois

        assertEquals(1, repository.count());
    }

    @Test
    void paginationWalksAllPagesInOrder() {
        for (int i = 1; i <= 5; i++) {
            createCustomer(i);
        }

        Page<Customer> first = repository.findAll(new PageRequest(0, 2));
        assertEquals(List.of(1L, 2L), ids(first));
        assertEquals(2L, first.nextCursor());

        Page<Customer> second = repository.findAll(new PageRequest(first.nextCursor(), 2));
        assertEquals(List.of(3L, 4L), ids(second));
        assertEquals(4L, second.nextCursor());

        Page<Customer> third = repository.findAll(new PageRequest(second.nextCursor(), 2));
        assertEquals(List.of(5L), ids(third));
        assertFalse(third.hasNext());
    }

    @Test
    void lastPageExactlyFullHasNoNext() {
        for (int i = 1; i <= 4; i++) {
            createCustomer(i);
        }

        Page<Customer> second = repository.findAll(new PageRequest(2, 2));

        assertEquals(List.of(3L, 4L), ids(second));
        assertFalse(second.hasNext());
    }

    @Test
    void paginationSkipsDeletedCustomers() {
        for (int i = 1; i <= 4; i++) {
            createCustomer(i);
        }
        repository.deleteById(2);

        assertEquals(List.of(1L, 3L), ids(repository.findAll(new PageRequest(0, 2))));
    }

    @Test
    void emptyRepositoryGivesEmptyPage() {
        Page<Customer> page = repository.findAll(PageRequest.firstPage());

        assertTrue(page.items().isEmpty());
        assertFalse(page.hasNext());
    }
}