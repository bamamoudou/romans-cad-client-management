package com.romanscad.clientmanagement.cli.rest;

import com.romanscad.clientmanagement.core.domain.Customer;
import com.romanscad.clientmanagement.core.pagination.Page;
import com.romanscad.clientmanagement.core.pagination.PageRequest;
import com.romanscad.clientmanagement.core.service.CustomerService;

/**
 * Proxy distant du service des clients : implémente l'interface du domaine en appelant la passerelle REST.
 * Le moteur de ligne de commande ne voit aucune différence avec le service local ou le client SOAP.
 */
final class RestCustomerService implements CustomerService {

    private final RestClient client;

    RestCustomerService(RestClient client) {
        this.client = client;
    }

    @Override
    public Customer createCustomer(String firstName, String lastName, String email) {
        String body = client.post("/customers", RestJson.customerBody(firstName, lastName, email)).bodyOrThrow();
        return RestJson.read(body, RestJson::toCustomer);
    }

    @Override
    public Customer getCustomer(long customerId) {
        String body = client.get("/customers/" + customerId).bodyOrThrow();
        return RestJson.read(body, RestJson::toCustomer);
    }

    @Override
    public void deleteCustomer(long customerId) {
        client.delete("/customers/" + customerId).bodyOrThrow();
    }

    @Override
    public Page<Customer> listCustomers(PageRequest pageRequest) {
        String body = client.get("/customers" + RestClient.pageQuery(pageRequest)).bodyOrThrow();
        return RestJson.read(body, json -> RestJson.toPage(json, RestJson::toCustomer));
    }
}