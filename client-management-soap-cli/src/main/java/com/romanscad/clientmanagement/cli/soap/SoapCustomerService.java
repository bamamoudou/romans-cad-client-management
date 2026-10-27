package com.romanscad.clientmanagement.cli.soap;

import com.romanscad.clientmanagement.cli.soap.generated.CustomerPage;
import com.romanscad.clientmanagement.cli.soap.generated.CustomerWebService;
import com.romanscad.clientmanagement.core.domain.Customer;
import com.romanscad.clientmanagement.core.pagination.Page;
import com.romanscad.clientmanagement.core.pagination.PageRequest;
import com.romanscad.clientmanagement.core.service.CustomerService;
import java.util.function.Supplier;

/**
 * Proxy distant du service des clients : implémente l'interface du domaine en appelant le
 * serveur SOAP. Le moteur de la ligne de commande ne voit aucune différence avec le service local.
 *
 * Le port SOAP est fourni par un Supplier : il n'est créé (et le WSDL lu) qu'au premier appel
 * réel, pour que des commandes comme "help" restent instantanées.
 */
final class SoapCustomerService implements CustomerService {

    private final Supplier<CustomerWebService> port;
    private final SoapInvoker invoker;

    SoapCustomerService(Supplier<CustomerWebService> port, SoapInvoker invoker) {
        this.port = port;
        this.invoker = invoker;
    }

    @Override
    public Customer createCustomer(String firstName, String lastName, String email) {
        return invoker.invoke(() -> SoapMapper.toCustomer(port.get().createCustomer(firstName, lastName, email)));
    }

    @Override
    public Customer getCustomer(long customerId) {
        return invoker.invoke(() -> SoapMapper.toCustomer(port.get().getCustomer(customerId)));
    }

    @Override
    public void deleteCustomer(long customerId) {
        invoker.invokeVoid(() -> port.get().deleteCustomer(customerId));
    }

    @Override
    public Page<Customer> listCustomers(PageRequest pageRequest) {
        return invoker.invoke(() -> {
            CustomerPage page = port.get().listCustomers(pageRequest.afterId(), pageRequest.limit());
            return SoapMapper.toPage(page.getItem(), page.getNextCursor(), SoapMapper::toCustomer);
        });
    }
}