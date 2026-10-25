package com.romanscad.clientmanagement.soap.endpoint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.romanscad.clientmanagement.core.exception.ValidationException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class MembershipWebServiceTest {

    private static List<Long> ids(int count) {
        List<Long> ids = new ArrayList<>();
        for (long id = 1; id <= count; id++) {
            ids.add(id);
        }
        return ids;
    }

    @Test
    void batchAtTheLimitIsAccepted() {
        List<Long> batch = ids(MembershipWebService.MAX_BATCH_SIZE);

        assertSame(batch, MembershipWebService.requireWithinBatchLimit(batch));
    }

    @Test
    void batchAboveTheLimitIsRejected() {
        ValidationException error = assertThrows(ValidationException.class,
                () -> MembershipWebService.requireWithinBatchLimit(ids(MembershipWebService.MAX_BATCH_SIZE + 1)));

        assertEquals("customerId must not contain more than 1000 identifiers", error.getMessage());
    }

    @Test
    void nullOrEmptyListIsLeftToTheBusinessService() {
        assertNull(MembershipWebService.requireWithinBatchLimit(null));
        assertEquals(List.of(), MembershipWebService.requireWithinBatchLimit(List.of()));
    }
}