package com.romanscad.clientmanagement.core.repository.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.romanscad.clientmanagement.core.pagination.Page;
import com.romanscad.clientmanagement.core.pagination.PageRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class InMemoryMembershipRepositoryTest {

    private final InMemoryMembershipRepository repository = new InMemoryMembershipRepository();

    /** Parcourt toutes les pages des clients d'un groupe et rassemble les identifiants. */
    private List<Long> allCustomerIdsOf(long groupId, int pageSize) {
        List<Long> all = new ArrayList<>();
        Page<Long> page = repository.findCustomerIdsByGroup(groupId, new PageRequest(0, pageSize));
        all.addAll(page.items());
        while (page.hasNext()) {
            page = repository.findCustomerIdsByGroup(groupId, new PageRequest(page.nextCursor(), pageSize));
            all.addAll(page.items());
        }
        return all;
    }

    private List<Long> groupsOf(long customerId) {
        return repository.findGroupIdsByCustomer(customerId, PageRequest.firstPage()).items();
    }

    @Test
    void linkIsIdempotentAndReportsCreation() {
        assertTrue(repository.link(1, 10));
        assertFalse(repository.link(1, 10));
    }

    @Test
    void linkIsVisibleInBothDirections() {
        repository.link(1, 10);

        assertEquals(List.of(1L), allCustomerIdsOf(10, 100));
        assertEquals(List.of(10L), groupsOf(1));
    }

    @Test
    void unlinkReportsWhetherAssociationExisted() {
        repository.link(1, 10);

        assertTrue(repository.unlink(1, 10));
        assertFalse(repository.unlink(1, 10));
    }

    @Test
    void unlinkRemovesBothDirections() {
        repository.link(1, 10);
        repository.unlink(1, 10);

        assertTrue(allCustomerIdsOf(10, 100).isEmpty());
        assertTrue(groupsOf(1).isEmpty());
    }

    @Test
    void canLinkAgainAfterUnlink() {
        repository.link(1, 10);
        repository.unlink(1, 10);

        assertTrue(repository.link(1, 10));
    }

    @Test
    void unknownKeysGiveEmptyPages() {
        assertTrue(repository.findCustomerIdsByGroup(99, PageRequest.firstPage()).items().isEmpty());
        assertFalse(repository.findGroupIdsByCustomer(99, PageRequest.firstPage()).hasNext());
    }

    @Test
    void customersOfGroupArePaginatedInOrder() {
        for (long customerId = 5; customerId >= 1; customerId--) { // ordre d'insertion inversé
            repository.link(customerId, 10);
        }

        Page<Long> first = repository.findCustomerIdsByGroup(10, new PageRequest(0, 2));
        assertEquals(List.of(1L, 2L), first.items());
        assertEquals(2L, first.nextCursor());

        Page<Long> second = repository.findCustomerIdsByGroup(10, new PageRequest(first.nextCursor(), 2));
        assertEquals(List.of(3L, 4L), second.items());

        Page<Long> third = repository.findCustomerIdsByGroup(10, new PageRequest(second.nextCursor(), 2));
        assertEquals(List.of(5L), third.items());
        assertFalse(third.hasNext());
    }

    @Test
    void unlinkAllOfCustomerRemovesEveryAssociationOfThatCustomerOnly() {
        repository.link(1, 10);
        repository.link(1, 20);
        repository.link(2, 10);

        repository.unlinkAllOfCustomer(1);

        assertTrue(groupsOf(1).isEmpty());
        assertEquals(List.of(2L), allCustomerIdsOf(10, 100));
        assertTrue(allCustomerIdsOf(20, 100).isEmpty());
    }

    @Test
    void unlinkAllOfGroupRemovesEveryAssociationOfThatGroupOnly() {
        repository.link(1, 10);
        repository.link(2, 10);
        repository.link(1, 20);

        repository.unlinkAllOfGroup(10);

        assertTrue(allCustomerIdsOf(10, 100).isEmpty());
        assertEquals(List.of(20L), groupsOf(1));
        assertTrue(groupsOf(2).isEmpty());
    }

    @Test
    void concurrentLinksAreAllRecorded() throws InterruptedException {
        int threads = 8;
        int perThread = 1_000;
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        for (int t = 0; t < threads; t++) {
            long firstId = (long) t * perThread + 1;
            executor.submit(() -> {
                for (long id = firstId; id < firstId + perThread; id++) {
                    repository.link(id, 10);
                }
            });
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS));

        assertEquals(threads * perThread, allCustomerIdsOf(10, 500).size());
    }
}