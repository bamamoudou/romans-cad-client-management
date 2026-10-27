package com.romanscad.clientmanagement.rest.error;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/** Transforme les erreurs attendues (client introuvable, doublon, SOAP injoignable...) en réponse HTTP. */
@Provider
public class ApiExceptionMapper implements ExceptionMapper<ApiException> {

    @Override
    public Response toResponse(ApiException exception) {
        return ProblemDetail.response(exception.status(), exception.getMessage(), exception.code());
    }
}