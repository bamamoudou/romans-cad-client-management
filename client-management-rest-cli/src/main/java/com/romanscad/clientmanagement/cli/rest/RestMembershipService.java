package com.romanscad.clientmanagement.cli.rest;

import com.romanscad.clientmanagement.core.domain.Customer;
import com.romanscad.clientmanagement.core.domain.CustomerWithGroups;
import com.romanscad.clientmanagement.core.domain.Group;
import com.romanscad.clientmanagement.core.pagination.Page;
import com.romanscad.clientmanagement.core.pagination.PageRequest;
import com.romanscad.clientmanagement.core.service.MembershipService;
import java.util.Collection;

/**
 * Proxy distant du service des appartenances.
 *
 * La limite de 1000 identifiants par appel d'association est celle du serveur : elle n'est pas masquée
 * ici, car découper en plusieurs appels romprait la garantie « tout ou rien ».
 */
final class RestMembershipService implements MembershipService {

    private final RestClient client;

    RestMembershipService(RestClient client) {
        this.client = client;
    }

    @Override
    public int addCustomersToGroup(long groupId, Collection<Long> customerIds) {
        String body = client.post("/groups/" + groupId + "/customers", RestJson.idsBody(customerIds)).bodyOrThrow();
        return RestJson.read(body, RestJson::toCount);
    }

    @Override
    public int removeCustomersFromGroup(long groupId, Collection<Long> customerIds) {
        String body = client.post("/groups/" + groupId + "/customers/removal", RestJson.idsBody(customerIds)).bodyOrThrow();
        return RestJson.read(body, RestJson::toCount);
    }

    @Override
    public Page<Customer> listCustomersOfGroup(long groupId, PageRequest pageRequest) {
        String body = client.get("/groups/" + groupId + "/customers" + RestClient.pageQuery(pageRequest)).bodyOrThrow();
        return RestJson.read(body, json -> RestJson.toPage(json, RestJson::toCustomer));
    }

    @Override
    public Page<Group> listGroupsOfCustomer(long customerId, PageRequest pageRequest) {
        String body = client.get("/customers/" + customerId + "/groups" + RestClient.pageQuery(pageRequest)).bodyOrThrow();
        return RestJson.read(body, json -> RestJson.toPage(json, RestJson::toGroup));
    }

    @Override
    public Page<CustomerWithGroups> listCustomersWithGroups(PageRequest pageRequest) {
        String body = client.get("/customers/with-groups" + RestClient.pageQuery(pageRequest)).bodyOrThrow();
        return RestJson.read(body, json -> RestJson.toPage(json, RestJson::toCustomerWithGroups));
    }
}