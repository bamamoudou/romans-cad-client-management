package com.romanscad.clientmanagement.soap.mapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.romanscad.clientmanagement.core.domain.Customer;
import com.romanscad.clientmanagement.core.domain.CustomerWithGroups;
import com.romanscad.clientmanagement.core.domain.Group;
import com.romanscad.clientmanagement.core.exception.ValidationException;
import com.romanscad.clientmanagement.core.pagination.Page;
import com.romanscad.clientmanagement.core.pagination.PageRequest;
import com.romanscad.clientmanagement.soap.dto.CustomerDto;
import com.romanscad.clientmanagement.soap.dto.CustomerPageDto;
import com.romanscad.clientmanagement.soap.dto.CustomerWithGroupsDto;
import com.romanscad.clientmanagement.soap.dto.GroupDto;
import com.romanscad.clientmanagement.soap.dto.GroupPageDto;
import java.util.List;
import org.junit.jupiter.api.Test;

class DtoMapperTest {

    private static final Customer ADA = new Customer(1, "Ada", "Lovelace", "ada@example.com");
    private static final Customer ALAN = new Customer(2, "Alan", "Turing", "alan@example.com");
    private static final Group VIP = new Group(10, "VIP", "Best customers");
    private static final Group STAFF = new Group(11, "Staff", null);

    @Test
    void customerFieldsAreCopied() {
        CustomerDto dto = DtoMapper.toCustomerDto(ADA);

        assertEquals(1L, dto.getId());
        assertEquals("Ada", dto.getFirstName());
        assertEquals("Lovelace", dto.getLastName());
        assertEquals("ada@example.com", dto.getEmail());
    }

    @Test
    void groupFieldsAreCopied() {
        GroupDto dto = DtoMapper.toGroupDto(STAFF);

        assertEquals(11L, dto.getId());
        assertEquals("Staff", dto.getName());
        assertEquals("", dto.getDescription()); // le domaine normalise null en chaîne vide
    }

    @Test
    void customerWithGroupsKeepsCustomerAndGroupOrder() {
        CustomerWithGroupsDto dto = DtoMapper.toCustomerWithGroupsDto(new CustomerWithGroups(ADA, List.of(VIP, STAFF)));

        assertEquals("Ada", dto.getCustomer().getFirstName());
        assertEquals(List.of(10L, 11L), dto.getGroups().stream().map(GroupDto::getId).toList());
    }

    @Test
    void customerPageKeepsOrderAndCursor() {
        CustomerPageDto dto = DtoMapper.toCustomerPage(new Page<>(List.of(ADA, ALAN), 2L));

        assertEquals(List.of(1L, 2L), dto.getItems().stream().map(CustomerDto::getId).toList());
        assertEquals(2L, dto.getNextCursor());
    }

    @Test
    void lastPageHasNoCursor() {
        GroupPageDto dto = DtoMapper.toGroupPage(new Page<>(List.of(VIP), null));

        assertEquals(1, dto.getItems().size());
        assertNull(dto.getNextCursor());
    }

    @Test
    void emptyPageGivesEmptyModifiableList() {
        CustomerPageDto dto = DtoMapper.toCustomerPage(Page.empty());

        assertEquals(0, dto.getItems().size());
        dto.getItems().add(new CustomerDto()); // ne doit pas lever UnsupportedOperationException
    }

    @Test
    void missingPageParametersMeanFirstPageWithDefaultSize() {
        PageRequest request = DtoMapper.toPageRequest(null, null);

        assertEquals(0L, request.afterId());
        assertEquals(PageRequest.DEFAULT_LIMIT, request.limit());
    }

    @Test
    void givenPageParametersAreKept() {
        PageRequest request = DtoMapper.toPageRequest(500L, 25);

        assertEquals(500L, request.afterId());
        assertEquals(25, request.limit());
    }

    @Test
    void invalidPageParametersAreRejected() {
        assertThrows(ValidationException.class, () -> DtoMapper.toPageRequest(0L, 0));
        assertThrows(ValidationException.class, () -> DtoMapper.toPageRequest(0L, PageRequest.MAX_LIMIT + 1));
        assertThrows(ValidationException.class, () -> DtoMapper.toPageRequest(-1L, 10));
    }
}