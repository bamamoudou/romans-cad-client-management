package com.romanscad.clientmanagement.cli.soap;

import com.romanscad.clientmanagement.core.domain.Customer;
import com.romanscad.clientmanagement.core.domain.CustomerWithGroups;
import com.romanscad.clientmanagement.core.domain.Group;
import com.romanscad.clientmanagement.core.pagination.Page;
import java.util.List;
import java.util.function.Function;

/**
 * Conversions des types générés à partir du WSDL vers ceux du domaine.
 *
 * Les stubs générés et le domaine ont des classes de même nom (Customer, Group...) : les types
 * générés sont donc écrits avec leur nom complet, uniquement dans cette classe.
 */
final class SoapMapper {

    private SoapMapper() {
        // Classe utilitaire : non instanciable
    }

    static Customer toCustomer(com.romanscad.clientmanagement.cli.soap.generated.Customer source) {
        return new Customer(source.getId(), source.getFirstName(), source.getLastName(), source.getEmail());
    }

    static Group toGroup(com.romanscad.clientmanagement.cli.soap.generated.Group source) {
        return new Group(source.getId(), source.getName(), source.getDescription());
    }

    static CustomerWithGroups toCustomerWithGroups(
            com.romanscad.clientmanagement.cli.soap.generated.CustomerWithGroups source) {
        return new CustomerWithGroups(
                toCustomer(source.getCustomer()),
                source.getGroup().stream().map(SoapMapper::toGroup).toList());
    }

    /** Convertit les éléments d'une page SOAP et conserve son curseur (null sur la dernière page). */
    static <S, D> Page<D> toPage(List<S> items, Long nextCursor, Function<S, D> itemMapper) {
        return new Page<>(items.stream().map(itemMapper).toList(), nextCursor);
    }
}