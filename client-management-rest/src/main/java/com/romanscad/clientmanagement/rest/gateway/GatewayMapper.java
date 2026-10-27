package com.romanscad.clientmanagement.rest.gateway;

import com.romanscad.clientmanagement.rest.dto.CustomerResponse;
import com.romanscad.clientmanagement.rest.dto.CustomerWithGroupsResponse;
import com.romanscad.clientmanagement.rest.dto.GroupResponse;
import com.romanscad.clientmanagement.rest.dto.PageResponse;
import com.romanscad.clientmanagement.rest.soap.generated.Customer;
import com.romanscad.clientmanagement.rest.soap.generated.CustomerWithGroups;
import com.romanscad.clientmanagement.rest.soap.generated.Group;
import java.util.List;
import java.util.function.Function;

/** Conversions des types générés à partir du WSDL vers les réponses JSON de l'API. */
final class GatewayMapper {

    private GatewayMapper() {
        // Classe utilitaire : non instanciable
    }

    static CustomerResponse toCustomer(Customer source) {
        return new CustomerResponse(source.getId(), source.getFirstName(), source.getLastName(), source.getEmail());
    }

    static GroupResponse toGroup(Group source) {
        return new GroupResponse(source.getId(), source.getName(), source.getDescription());
    }

    static CustomerWithGroupsResponse toCustomerWithGroups(CustomerWithGroups source) {
        return new CustomerWithGroupsResponse(
                toCustomer(source.getCustomer()),
                source.getGroup().stream().map(GatewayMapper::toGroup).toList());
    }

    /** Convertit les éléments d'une page SOAP et conserve son curseur (null sur la dernière page). */
    static <S, D> PageResponse<D> toPage(List<S> items, Long nextCursor, Function<S, D> itemMapper) {
        return new PageResponse<>(items.stream().map(itemMapper).toList(), nextCursor);
    }
}