package com.romanscad.clientmanagement.soap.fault;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.romanscad.clientmanagement.core.exception.CustomerNotFoundException;
import com.romanscad.clientmanagement.core.exception.DuplicateGroupNameException;
import com.romanscad.clientmanagement.core.exception.GroupNotFoundException;
import com.romanscad.clientmanagement.core.exception.ValidationException;
import com.romanscad.clientmanagement.soap.dto.ErrorCode;
import com.romanscad.clientmanagement.soap.dto.ErrorDetail;
import org.junit.jupiter.api.Test;

class SoapFaultsTest {

    @Test
    void customerNotFoundKeepsTheMissingId() {
        ErrorDetail detail = SoapFaults.from(new CustomerNotFoundException(42)).getFaultInfo();

        assertEquals(ErrorCode.CUSTOMER_NOT_FOUND, detail.getCode());
        assertEquals(42L, detail.getResourceId());
    }

    @Test
    void groupNotFoundKeepsTheMissingId() {
        ErrorDetail detail = SoapFaults.from(new GroupNotFoundException(7)).getFaultInfo();

        assertEquals(ErrorCode.GROUP_NOT_FOUND, detail.getCode());
        assertEquals(7L, detail.getResourceId());
    }

    @Test
    void duplicateGroupNameHasNoResourceId() {
        ErrorDetail detail = SoapFaults.from(new DuplicateGroupNameException("VIP")).getFaultInfo();

        assertEquals(ErrorCode.DUPLICATE_GROUP_NAME, detail.getCode());
        assertNull(detail.getResourceId());
    }

    @Test
    void validationErrorBecomesInvalidRequest() {
        ErrorDetail detail = SoapFaults.from(new ValidationException("limit must be between 1 and 1000")).getFaultInfo();

        assertEquals(ErrorCode.INVALID_REQUEST, detail.getCode());
        assertNull(detail.getResourceId());
    }

    @Test
    void messageIsCopiedAndOriginalExceptionKeptAsCause() {
        CustomerNotFoundException original = new CustomerNotFoundException(42);

        ClientManagementFault fault = SoapFaults.from(original);

        assertEquals("Customer 42 not found", fault.getMessage());
        assertEquals("Customer 42 not found", fault.getFaultInfo().getMessage());
        assertSame(original, fault.getCause());
    }
}