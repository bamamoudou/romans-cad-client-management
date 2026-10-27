package com.romanscad.clientmanagement.rest.gateway;

import com.romanscad.clientmanagement.rest.dto.CustomerResponse;
import com.romanscad.clientmanagement.rest.dto.CustomerWithGroupsResponse;
import com.romanscad.clientmanagement.rest.dto.GroupResponse;
import com.romanscad.clientmanagement.rest.dto.PageResponse;
import com.romanscad.clientmanagement.rest.soap.generated.CustomerPage;
import com.romanscad.clientmanagement.rest.soap.generated.CustomerWithGroupsPage;
import com.romanscad.clientmanagement.rest.soap.generated.GroupPage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Passerelle vers le service SOAP des appartenances.
 *
 * La limite de 1000 identifiants par appel d'association est celle du serveur : elle n'est pas
 * masquée ici, car découper en plusieurs appels romprait la garantie « tout ou rien ». Le serveur
 * répond par une erreur explicite, que la passerelle renvoie en 400.
 */
@ApplicationScoped
public class MembershipGateway {

    @Inject
    SoapPorts ports;

    public int addCustomersToGroup(long groupId, List<Long> customerIds) {
        return SoapCalls.call(() -> ports.memberships().addCustomersToGroup(groupId, new ArrayList<>(customerIds)));
    }

    public int removeCustomersFromGroup(long groupId, List<Long> customerIds) {
        return SoapCalls.call(() -> ports.memberships().removeCustomersFromGroup(groupId, new ArrayList<>(customerIds)));
    }

    public PageResponse<CustomerResponse> listCustomersOfGroup(long groupId, Long afterId, Integer limit) {
        return SoapCalls.call(() -> {
            CustomerPage page = ports.memberships().listCustomersOfGroup(groupId, afterId, limit);
            return GatewayMapper.toPage(page.getItem(), page.getNextCursor(), GatewayMapper::toCustomer);
        });
    }

    public PageResponse<GroupResponse> listGroupsOfCustomer(long customerId, Long afterId, Integer limit) {
        return SoapCalls.call(() -> {
            GroupPage page = ports.memberships().listGroupsOfCustomer(customerId, afterId, limit);
            return GatewayMapper.toPage(page.getItem(), page.getNextCursor(), GatewayMapper::toGroup);
        });
    }

    public PageResponse<CustomerWithGroupsResponse> listCustomersWithGroups(Long afterId, Integer limit) {
        return SoapCalls.call(() -> {
            CustomerWithGroupsPage page = ports.memberships().listCustomersWithGroups(afterId, limit);
            return GatewayMapper.toPage(page.getItem(), page.getNextCursor(), GatewayMapper::toCustomerWithGroups);
        });
    }
}