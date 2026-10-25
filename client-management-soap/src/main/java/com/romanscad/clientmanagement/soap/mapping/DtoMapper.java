package com.romanscad.clientmanagement.soap.mapping;

import com.romanscad.clientmanagement.core.domain.Customer;
import com.romanscad.clientmanagement.core.domain.CustomerWithGroups;
import com.romanscad.clientmanagement.core.domain.Group;
import com.romanscad.clientmanagement.core.pagination.Page;
import com.romanscad.clientmanagement.core.pagination.PageRequest;
import com.romanscad.clientmanagement.soap.dto.CustomerDto;
import com.romanscad.clientmanagement.soap.dto.CustomerPageDto;
import com.romanscad.clientmanagement.soap.dto.CustomerWithGroupsDto;
import com.romanscad.clientmanagement.soap.dto.CustomerWithGroupsPageDto;
import com.romanscad.clientmanagement.soap.dto.GroupDto;
import com.romanscad.clientmanagement.soap.dto.GroupPageDto;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Conversions entre le domaine et le contrat SOAP.
 *
 * C'est le seul endroit qui connaît les deux modèles : le domaine ignore les DTO, et les DTO
 * ignorent le domaine. Changer l'un n'oblige à retoucher que cette classe.
 *
 * Fonctions pures, sans état : méthodes statiques, faciles à tester sans serveur.
 */
public final class DtoMapper {

    private DtoMapper() {
        // Classe utilitaire : non instanciable
    }

    // ------------------------------------------------------------ domaine -> DTO

    public static CustomerDto toCustomerDto(Customer customer) {
        return new CustomerDto(customer.id(), customer.firstName(), customer.lastName(), customer.email());
    }

    public static GroupDto toGroupDto(Group group) {
        return new GroupDto(group.id(), group.name(), group.description());
    }

    public static CustomerWithGroupsDto toCustomerWithGroupsDto(CustomerWithGroups value) {
        return new CustomerWithGroupsDto(
                toCustomerDto(value.customer()),
                toList(value.groups(), DtoMapper::toGroupDto));
    }

    public static CustomerPageDto toCustomerPage(Page<Customer> page) {
        return toPage(page, DtoMapper::toCustomerDto, CustomerPageDto::new);
    }

    public static GroupPageDto toGroupPage(Page<Group> page) {
        return toPage(page, DtoMapper::toGroupDto, GroupPageDto::new);
    }

    public static CustomerWithGroupsPageDto toCustomerWithGroupsPage(Page<CustomerWithGroups> page) {
        return toPage(page, DtoMapper::toCustomerWithGroupsDto, CustomerWithGroupsPageDto::new);
    }

    // ------------------------------------------------------------ requête -> domaine

    /**
     * Construit la demande de page à partir des paramètres facultatifs du contrat :
     * afterId absent = première page, limit absent = taille par défaut.
     * Les valeurs fournies sont validées par PageRequest (ValidationException si hors bornes).
     */
    public static PageRequest toPageRequest(Long afterId, Integer limit) {
        long cursor = (afterId != null) ? afterId : 0L;
        int size = (limit != null) ? limit : PageRequest.DEFAULT_LIMIT;
        return new PageRequest(cursor, size);
    }

    // ------------------------------------------------------------ outils

    /** Convertit une page : mêmes éléments (convertis), même curseur, dans le type de page voulu. */
    private static <S, D, P> P toPage(Page<S> page,
                                      Function<S, D> itemMapper,
                                      BiFunction<List<D>, Long, P> pageFactory) {
        return pageFactory.apply(toList(page.items(), itemMapper), page.nextCursor());
    }

    /** Liste modifiable (et non immuable) : JAXB peut avoir besoin de la remplir ou de la modifier. */
    private static <S, D> List<D> toList(List<S> source, Function<S, D> itemMapper) {
        return source.stream().map(itemMapper).collect(Collectors.toCollection(ArrayList::new));
    }
}