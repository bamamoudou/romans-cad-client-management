package com.romanscad.clientmanagement.cli.soap;

import com.romanscad.clientmanagement.cli.soap.generated.CustomerPage;
import com.romanscad.clientmanagement.cli.soap.generated.CustomerWithGroupsPage;
import com.romanscad.clientmanagement.cli.soap.generated.GroupPage;
import com.romanscad.clientmanagement.cli.soap.generated.MembershipWebService;
import com.romanscad.clientmanagement.core.domain.Customer;
import com.romanscad.clientmanagement.core.domain.CustomerWithGroups;
import com.romanscad.clientmanagement.core.domain.Group;
import com.romanscad.clientmanagement.core.pagination.Page;
import com.romanscad.clientmanagement.core.pagination.PageRequest;
import com.romanscad.clientmanagement.core.service.MembershipService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

/**
 * Proxy distant du service des appartenances.
 *
 * Le serveur refuse plus de 1000 identifiants par appel d'association : cette limite n'est pas
 * masquée ici. Découper en plusieurs appels romprait la garantie « tout ou rien », donc on laisse
 * le serveur répondre par une erreur explicite.
 */
final class SoapMembershipService implements MembershipService {

    private final Supplier<MembershipWebService> port;
    private final SoapInvoker invoker;

    SoapMembershipService(Supplier<MembershipWebService> port, SoapInvoker invoker) {
        this.port = port;
        this.invoker = invoker;
    }

    @Override
    public int addCustomersToGroup(long groupId, Collection<Long> customerIds) {
        return invoker.invoke(() -> port.get().addCustomersToGroup(groupId, listOf(customerIds)));
    }

    @Override
    public int removeCustomersFromGroup(long groupId, Collection<Long> customerIds) {
        return invoker.invoke(() -> port.get().removeCustomersFromGroup(groupId, listOf(customerIds)));
    }

    @Override
    public Page<Customer> listCustomersOfGroup(long groupId, PageRequest pageRequest) {
        return invoker.invoke(() -> {
            CustomerPage page = port.get().listCustomersOfGroup(groupId, pageRequest.afterId(), pageRequest.limit());
            return SoapMapper.toPage(page.getItem(), page.getNextCursor(), SoapMapper::toCustomer);
        });
    }

    @Override
    public Page<Group> listGroupsOfCustomer(long customerId, PageRequest pageRequest) {
        return invoker.invoke(() -> {
            GroupPage page = port.get().listGroupsOfCustomer(customerId, pageRequest.afterId(), pageRequest.limit());
            return SoapMapper.toPage(page.getItem(), page.getNextCursor(), SoapMapper::toGroup);
        });
    }

    @Override
    public Page<CustomerWithGroups> listCustomersWithGroups(PageRequest pageRequest) {
        return invoker.invoke(() -> {
            CustomerWithGroupsPage page = port.get().listCustomersWithGroups(pageRequest.afterId(), pageRequest.limit());
            return SoapMapper.toPage(page.getItem(), page.getNextCursor(), SoapMapper::toCustomerWithGroups);
        });
    }

    /** Une liste ordinaire, comme l'attend le stub (un Set ou autre Collection ne passerait pas). */
    private static ArrayList<Long> listOf(Collection<Long> customerIds) {
        return new ArrayList<>(customerIds);
    }
}