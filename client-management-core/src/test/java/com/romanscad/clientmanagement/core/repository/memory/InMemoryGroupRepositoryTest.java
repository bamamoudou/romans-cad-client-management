package com.romanscad.clientmanagement.core.repository.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.romanscad.clientmanagement.core.domain.Group;
import com.romanscad.clientmanagement.core.pagination.Page;
import com.romanscad.clientmanagement.core.pagination.PageRequest;
import java.util.List;
import org.junit.jupiter.api.Test;

class InMemoryGroupRepositoryTest {

    private final InMemoryGroupRepository repository = new InMemoryGroupRepository();

    @Test
    void createAssignsIncreasingIdsAndNormalizesDescription() {
        Group first = repository.create("VIP", null);
        Group second = repository.create("Staff", "Employees");

        assertEquals(1, first.id());
        assertEquals("", first.description());
        assertEquals(2, second.id());
    }

    @Test
    void findByIdReturnsEmptyWhenUnknown() {
        assertTrue(repository.findById(42).isEmpty());
    }

    @Test
    void findByNameIgnoresCaseAndSurroundingSpaces() {
        Group vip = repository.create("VIP", "Best customers");

        assertEquals(vip, repository.findByName("vip").orElseThrow());
        assertEquals(vip, repository.findByName("  Vip ").orElseThrow());
    }

    @Test
    void findByNameReturnsEmptyForUnknownOrNullName() {
        repository.create("VIP", null);

        assertTrue(repository.findByName("Staff").isEmpty());
        assertTrue(repository.findByName(null).isEmpty());
    }

    @Test
    void deleteReportsWhetherGroupExisted() {
        Group group = repository.create("VIP", null);

        assertTrue(repository.deleteById(group.id()));
        assertFalse(repository.deleteById(group.id()));
    }

    @Test
    void deleteFreesTheName() {
        Group group = repository.create("VIP", null);
        repository.deleteById(group.id());

        assertTrue(repository.findByName("VIP").isEmpty());
        assertEquals(2, repository.create("VIP", null).id());
    }

    @Test
    void countFollowsCreationsAndDeletions() {
        repository.create("A", null);
        Group second = repository.create("B", null);
        repository.deleteById(second.id());
        repository.deleteById(second.id());

        assertEquals(1, repository.count());
    }

    @Test
    void paginationWalksAllPagesInOrder() {
        repository.create("A", null);
        repository.create("B", null);
        repository.create("C", null);

        Page<Group> first = repository.findAll(new PageRequest(0, 2));
        assertEquals(List.of(1L, 2L), first.items().stream().map(Group::id).toList());
        assertEquals(2L, first.nextCursor());

        Page<Group> second = repository.findAll(new PageRequest(first.nextCursor(), 2));
        assertEquals(List.of(3L), second.items().stream().map(Group::id).toList());
        assertFalse(second.hasNext());
    }
}