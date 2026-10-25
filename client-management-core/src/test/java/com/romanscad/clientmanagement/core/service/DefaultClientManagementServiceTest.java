package com.romanscad.clientmanagement.core.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.romanscad.clientmanagement.core.domain.Customer;
import com.romanscad.clientmanagement.core.domain.CustomerWithGroups;
import com.romanscad.clientmanagement.core.domain.Group;
import com.romanscad.clientmanagement.core.exception.CustomerNotFoundException;
import com.romanscad.clientmanagement.core.exception.DuplicateGroupNameException;
import com.romanscad.clientmanagement.core.exception.GroupNotFoundException;
import com.romanscad.clientmanagement.core.exception.ValidationException;
import com.romanscad.clientmanagement.core.pagination.Page;
import com.romanscad.clientmanagement.core.pagination.PageRequest;
import com.romanscad.clientmanagement.core.repository.memory.InMemoryCustomerRepository;
import com.romanscad.clientmanagement.core.repository.memory.InMemoryGroupRepository;
import com.romanscad.clientmanagement.core.repository.memory.InMemoryMembershipRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

/** Teste les règles métier avec les vraies implémentations en mémoire (pas de mock : elles sont rapides). */
class DefaultClientManagementServiceTest {

    private final DefaultClientManagementService service = new DefaultClientManagementService(
            new InMemoryCustomerRepository(),
            new InMemoryGroupRepository(),
            new InMemoryMembershipRepository());

    private Customer newCustomer(int n) {
        return service.createCustomer("First" + n, "Last" + n, "user" + n + "@example.com");
    }

    private List<Long> customerIdsOf(long groupId) {
        List<Long> ids = new ArrayList<>();
        Page<Customer> page = service.listCustomersOfGroup(groupId, new PageRequest(0, 100));
        page.items().forEach(c -> ids.add(c.id()));
        while (page.hasNext()) {
            page = service.listCustomersOfGroup(groupId, new PageRequest(page.nextCursor(), 100));
            page.items().forEach(c -> ids.add(c.id()));
        }
        return ids;
    }

    // ---- groupes

    @Test
    void createGroupRejectsDuplicateNameIgnoringCase() {
        service.createGroup("VIP", null);

        assertThrows(DuplicateGroupNameException.class, () -> service.createGroup("  vip ", "other"));
    }

    @Test
    void createGroupRejectsBlankName() {
        assertThrows(ValidationException.class, () -> service.createGroup("  ", null));
        assertThrows(ValidationException.class, () -> service.createGroup(null, null));
    }

    @Test
    void deletedGroupNameCanBeReused() {
        Group group = service.createGroup("VIP", null);
        service.deleteGroup(group.id());

        assertEquals("VIP", service.createGroup("VIP", null).name());
    }

    // ---- clients

    @Test
    void getUnknownCustomerFails() {
        assertThrows(CustomerNotFoundException.class, () -> service.getCustomer(42));
    }

    @Test
    void deleteCustomerRemovesItsMembershipsAndFailsWhenRepeated() {
        Customer customer = newCustomer(1);
        Group group = service.createGroup("VIP", null);
        service.addCustomersToGroup(group.id(), List.of(customer.id()));

        service.deleteCustomer(customer.id());

        assertTrue(customerIdsOf(group.id()).isEmpty());
        assertThrows(CustomerNotFoundException.class, () -> service.deleteCustomer(customer.id()));
    }

    @Test
    void deleteGroupRemovesMembershipsButKeepsCustomers() {
        Customer customer = newCustomer(1);
        Group group = service.createGroup("VIP", null);
        service.addCustomersToGroup(group.id(), List.of(customer.id()));

        service.deleteGroup(group.id());

        assertEquals(customer, service.getCustomer(customer.id()));
        assertTrue(service.listGroupsOfCustomer(customer.id(), PageRequest.firstPage()).items().isEmpty());
        assertThrows(GroupNotFoundException.class, () -> service.deleteGroup(group.id()));
    }

    // ---- associations

    @Test
    void addCustomersCountsOnlyNewAssociations() {
        Customer first = newCustomer(1);
        Customer second = newCustomer(2);
        Group group = service.createGroup("VIP", null);

        assertEquals(2, service.addCustomersToGroup(group.id(), List.of(first.id(), second.id())));
        assertEquals(0, service.addCustomersToGroup(group.id(), List.of(first.id(), second.id())));
    }

    @Test
    void duplicateIdsInOneRequestAreCountedOnce() {
        Customer customer = newCustomer(1);
        Group group = service.createGroup("VIP", null);

        assertEquals(1, service.addCustomersToGroup(group.id(), List.of(customer.id(), customer.id())));
    }

    @Test
    void addCustomersIsAtomicWhenOneCustomerIsUnknown() {
        Customer known = newCustomer(1);
        Group group = service.createGroup("VIP", null);

        assertThrows(CustomerNotFoundException.class,
                () -> service.addCustomersToGroup(group.id(), List.of(known.id(), 999L)));

        assertTrue(customerIdsOf(group.id()).isEmpty()); // rien n'a été associé
    }

    @Test
    void addCustomersRejectsUnknownGroupAndBadInput() {
        Customer customer = newCustomer(1);

        assertThrows(GroupNotFoundException.class, () -> service.addCustomersToGroup(99, List.of(customer.id())));

        Group group = service.createGroup("VIP", null);
        assertThrows(ValidationException.class, () -> service.addCustomersToGroup(group.id(), List.of()));
        assertThrows(ValidationException.class, () -> service.addCustomersToGroup(group.id(), null));
        List<Long> withNull = new ArrayList<>();
        withNull.add(null);
        assertThrows(ValidationException.class, () -> service.addCustomersToGroup(group.id(), withNull));
    }

    @Test
    void removeCustomersCountsRemovedAssociationsAndIsAtomic() {
        Customer first = newCustomer(1);
        Customer second = newCustomer(2);
        Group group = service.createGroup("VIP", null);
        service.addCustomersToGroup(group.id(), List.of(first.id(), second.id()));

        assertThrows(CustomerNotFoundException.class,
                () -> service.removeCustomersFromGroup(group.id(), List.of(first.id(), 999L)));
        assertEquals(List.of(first.id(), second.id()), customerIdsOf(group.id())); // inchangé

        assertEquals(1, service.removeCustomersFromGroup(group.id(), List.of(first.id())));
        assertEquals(0, service.removeCustomersFromGroup(group.id(), List.of(first.id())));
        assertEquals(List.of(second.id()), customerIdsOf(group.id()));
    }

    // ---- consultations

    @Test
    void listCustomersOfGroupReturnsCustomersPaginated() {
        Group group = service.createGroup("VIP", null);
        List<Long> ids = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            ids.add(newCustomer(i).id());
        }
        service.addCustomersToGroup(group.id(), ids);

        Page<Customer> first = service.listCustomersOfGroup(group.id(), new PageRequest(0, 2));
        assertEquals(List.of(1L, 2L), first.items().stream().map(Customer::id).toList());
        assertEquals("First1", first.items().get(0).firstName());
        assertTrue(first.hasNext());
        assertEquals(ids, customerIdsOf(group.id()));
    }

    @Test
    void listOfUnknownGroupOrCustomerFails() {
        assertThrows(GroupNotFoundException.class, () -> service.listCustomersOfGroup(1, PageRequest.firstPage()));
        assertThrows(CustomerNotFoundException.class, () -> service.listGroupsOfCustomer(1, PageRequest.firstPage()));
    }

    @Test
    void listGroupsOfCustomerReturnsItsGroups() {
        Customer customer = newCustomer(1);
        Group vip = service.createGroup("VIP", null);
        Group staff = service.createGroup("Staff", null);
        service.addCustomersToGroup(vip.id(), List.of(customer.id()));
        service.addCustomersToGroup(staff.id(), List.of(customer.id()));

        List<Group> groups = service.listGroupsOfCustomer(customer.id(), PageRequest.firstPage()).items();

        assertEquals(List.of(vip, staff), groups);
    }

    @Test
    void listCustomersWithGroupsCombinesBothSides() {
        Customer first = newCustomer(1);
        Customer second = newCustomer(2);
        Group vip = service.createGroup("VIP", null);
        Group staff = service.createGroup("Staff", null);
        service.addCustomersToGroup(vip.id(), List.of(first.id()));
        service.addCustomersToGroup(staff.id(), List.of(first.id()));

        List<CustomerWithGroups> result = service.listCustomersWithGroups(PageRequest.firstPage()).items();

        assertEquals(2, result.size());
        assertEquals(first, result.get(0).customer());
        assertEquals(List.of(vip, staff), result.get(0).groups());
        assertEquals(second, result.get(1).customer());
        assertTrue(result.get(1).groups().isEmpty());
    }

    // ---- concurrence

    @Test
    void concurrentAddsAndDeletesNeverLeaveOrphanMemberships() throws InterruptedException {
        int customerCount = 300;
        Group group = service.createGroup("VIP", null);
        for (int i = 1; i <= customerCount; i++) {
            newCustomer(i);
        }

        ExecutorService executor = Executors.newFixedThreadPool(5);
        for (int t = 0; t < 4; t++) { // 4 threads associent des clients au hasard
            executor.submit(() -> {
                for (int i = 0; i < 2_000; i++) {
                    long id = ThreadLocalRandom.current().nextLong(1, customerCount + 1);
                    try {
                        service.addCustomersToGroup(group.id(), List.of(id));
                    } catch (CustomerNotFoundException expected) {
                        // le client a déjà été supprimé : cas normal dans ce test
                    }
                }
            });
        }
        executor.submit(() -> { // 1 thread supprime les clients pendant ce temps
            for (long id = 1; id <= customerCount; id++) {
                service.deleteCustomer(id);
            }
        });
        executor.shutdown();
        assertTrue(executor.awaitTermination(60, TimeUnit.SECONDS));

        // Tous les clients sont supprimés : le groupe doit être vide. Un client "fantôme"
        // ferait lever IllegalStateException pendant la lecture.
        assertDoesNotThrow(() -> assertTrue(customerIdsOf(group.id()).isEmpty()));
        assertFalse(service.listCustomers(PageRequest.firstPage()).hasNext());
    }
}