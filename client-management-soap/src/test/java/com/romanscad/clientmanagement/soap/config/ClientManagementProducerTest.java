package com.romanscad.clientmanagement.soap.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.romanscad.clientmanagement.core.domain.Customer;
import com.romanscad.clientmanagement.core.domain.Group;
import com.romanscad.clientmanagement.core.pagination.PageRequest;
import java.util.List;
import org.junit.jupiter.api.Test;

class ClientManagementProducerTest {

    @Test
    void threeInterfacesShareTheSameStateAndTheSameService() {
        ClientManagementProducer producer = new ClientManagementProducer();
        producer.init(); // en production, le conteneur l'appelle (@PostConstruct)

        Customer customer = producer.customerService().createCustomer("Ada", "Lovelace", "ada@example.com");
        Group group = producer.groupService().createGroup("VIP", null);
        producer.membershipService().addCustomersToGroup(group.id(), List.of(customer.id()));

        assertEquals(List.of(customer),
                producer.membershipService().listCustomersOfGroup(group.id(), PageRequest.firstPage()).items());
        assertSame(producer.customerService(), producer.groupService()); // un seul objet derrière les trois
    }
}