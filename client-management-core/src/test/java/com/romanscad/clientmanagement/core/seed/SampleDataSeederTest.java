package com.romanscad.clientmanagement.core.seed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.romanscad.clientmanagement.core.domain.Customer;
import com.romanscad.clientmanagement.core.exception.ValidationException;
import com.romanscad.clientmanagement.core.pagination.Page;
import com.romanscad.clientmanagement.core.pagination.PageRequest;
import com.romanscad.clientmanagement.core.repository.memory.InMemoryCustomerRepository;
import com.romanscad.clientmanagement.core.repository.memory.InMemoryGroupRepository;
import com.romanscad.clientmanagement.core.repository.memory.InMemoryMembershipRepository;
import com.romanscad.clientmanagement.core.service.DefaultClientManagementService;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class SampleDataSeederTest {

    private static DefaultClientManagementService newService() {
        return new DefaultClientManagementService(
                new InMemoryCustomerRepository(), new InMemoryGroupRepository(), new InMemoryMembershipRepository());
    }

    private static SeedReport seed(DefaultClientManagementService service, SeedConfiguration configuration) {
        return new SampleDataSeeder(service, service, service).seed(configuration);
    }

    private static List<Customer> membersOf(DefaultClientManagementService service, long groupId) {
        List<Customer> members = new ArrayList<>();
        Page<Customer> page = service.listCustomersOfGroup(groupId, new PageRequest(0, PageRequest.MAX_LIMIT));
        members.addAll(page.items());
        while (page.hasNext()) {
            page = service.listCustomersOfGroup(groupId, new PageRequest(page.nextCursor(), PageRequest.MAX_LIMIT));
            members.addAll(page.items());
        }
        return members;
    }

    @Test
    void reportMatchesWhatWasCreated() {
        DefaultClientManagementService service = newService();

        SeedReport report = seed(service, new SeedConfiguration(2_000, 5, 7L));

        assertEquals(2_000, report.customers());
        assertEquals(5, report.groups());
        long members = 0;
        for (long groupId = 1; groupId <= 5; groupId++) {
            members += membersOf(service, groupId).size();
        }
        assertEquals(members, report.memberships());
    }

    @Test
    void biggestGroupHoldsAboutFortyPercentOfCustomers() {
        DefaultClientManagementService service = newService();
        seed(service, new SeedConfiguration(5_000, 3, 7L));

        int size = membersOf(service, 1).size();

        assertTrue(size > 1_700 && size < 2_300, "unexpected size: " + size); // ~2 000 attendus
    }

    @Test
    void sameConfigurationProducesIdenticalData() {
        DefaultClientManagementService first = newService();
        DefaultClientManagementService second = newService();
        SeedConfiguration configuration = new SeedConfiguration(1_000, 4, 99L);

        seed(first, configuration);
        seed(second, configuration);

        assertEquals(first.getCustomer(500), second.getCustomer(500));
        assertEquals(membersOf(first, 2), membersOf(second, 2));
    }

    @Test
    void differentSeedProducesDifferentData() {
        DefaultClientManagementService first = newService();
        DefaultClientManagementService second = newService();

        seed(first, new SeedConfiguration(1_000, 4, 1L));
        seed(second, new SeedConfiguration(1_000, 4, 2L));

        assertFalse(membersOf(first, 1).equals(membersOf(second, 1)));
    }

    @Test
    void emailsAreUnique() {
        DefaultClientManagementService service = newService();
        seed(service, new SeedConfiguration(900, 0, 5L)); // moins d'une page : tout tient dans une seule lecture

        List<Customer> customers = service.listCustomers(new PageRequest(0, PageRequest.MAX_LIMIT)).items();

        assertEquals(900, customers.size());
        assertEquals(900, customers.stream().map(Customer::email).distinct().count());
    }

    @Test
    void rejectsInvalidConfiguration() {
        assertThrows(ValidationException.class, () -> new SeedConfiguration(-1, 5, 1L));
        assertThrows(ValidationException.class, () -> new SeedConfiguration(10, 21, 1L));
        assertThrows(ValidationException.class, () -> new SeedConfiguration(10, -1, 1L));
    }
}