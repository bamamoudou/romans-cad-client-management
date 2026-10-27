package com.romanscad.clientmanagement.rest.gateway;

import com.romanscad.clientmanagement.rest.dto.CreateCustomerRequest;
import com.romanscad.clientmanagement.rest.dto.CustomerResponse;
import com.romanscad.clientmanagement.rest.dto.PageResponse;
import com.romanscad.clientmanagement.rest.soap.generated.CustomerPage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/** Passerelle vers le service SOAP des clients : appelle le serveur et renvoie des objets de l'API REST. */
@ApplicationScoped
public class CustomerGateway {

    @Inject
    SoapPorts ports;

    public CustomerResponse create(CreateCustomerRequest request) {
        return SoapCalls.call(() -> GatewayMapper.toCustomer(
                ports.customers().createCustomer(request.firstName(), request.lastName(), request.email())));
    }

    public CustomerResponse get(long customerId) {
        return SoapCalls.call(() -> GatewayMapper.toCustomer(ports.customers().getCustomer(customerId)));
    }

    public void delete(long customerId) {
        SoapCalls.run(() -> ports.customers().deleteCustomer(customerId));
    }

    public PageResponse<CustomerResponse> list(Long afterId, Integer limit) {
        return SoapCalls.call(() -> {
            CustomerPage page = ports.customers().listCustomers(afterId, limit);
            return GatewayMapper.toPage(page.getItem(), page.getNextCursor(), GatewayMapper::toCustomer);
        });
    }
}