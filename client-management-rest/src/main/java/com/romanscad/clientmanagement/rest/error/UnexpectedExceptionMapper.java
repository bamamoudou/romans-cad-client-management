package com.romanscad.clientmanagement.rest.error;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Filet de sécurité : toute exception non prévue produit quand même une réponse au format
 * Problem Details, sans jamais exposer de trace d'exécution au client.
 *
 * Les erreurs HTTP du framework (URL inconnue, corps JSON illisible, méthode non autorisée)
 * arrivent ici sous forme de WebApplicationException : on conserve leur statut d'origine.
 */
@Provider
public class UnexpectedExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(UnexpectedExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        if (exception instanceof WebApplicationException web) {
            int status = web.getResponse().getStatus();
            if (status >= 400) {
                return ProblemDetail.response(status, web.getMessage(), null);
            }
            return web.getResponse(); // redirection : rien à transformer
        }
        LOGGER.log(Level.SEVERE, "Unexpected error while handling a request", exception);
        return ProblemDetail.response(500, "Unexpected server error", "INTERNAL_ERROR");
    }
}