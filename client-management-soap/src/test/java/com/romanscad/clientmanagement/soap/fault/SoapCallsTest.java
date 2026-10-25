package com.romanscad.clientmanagement.soap.fault;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.romanscad.clientmanagement.core.exception.CustomerNotFoundException;
import com.romanscad.clientmanagement.soap.dto.ErrorCode;
import jakarta.xml.ws.WebServiceException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class SoapCallsTest {

    @Test
    void returnsTheResultOfTheAction() throws ClientManagementFault {
        assertEquals("ok", SoapCalls.call(() -> "ok"));
    }

    @Test
    void businessErrorBecomesATypedFault() {
        ClientManagementFault fault = assertThrows(ClientManagementFault.class,
                () -> SoapCalls.call(() -> {
                    throw new CustomerNotFoundException(42);
                }));

        assertEquals(ErrorCode.CUSTOMER_NOT_FOUND, fault.getFaultInfo().getCode());
        assertEquals(42L, fault.getFaultInfo().getResourceId());
    }

    @Test
    void unexpectedErrorIsHiddenBehindAGenericMessage() {
        WebServiceException error = assertThrows(WebServiceException.class,
                () -> SoapCalls.call(() -> {
                    throw new IllegalStateException("secret internal detail");
                }));

        assertEquals("Internal server error", error.getMessage());
        assertFalse(error.getMessage().contains("secret"));
    }

    @Test
    void runExecutesTheActionAndTranslatesErrors() throws ClientManagementFault {
        AtomicBoolean executed = new AtomicBoolean(false);
        SoapCalls.run(() -> executed.set(true));
        assertTrue(executed.get());

        assertThrows(ClientManagementFault.class, () -> SoapCalls.run(() -> {
            throw new CustomerNotFoundException(1);
        }));
    }
}