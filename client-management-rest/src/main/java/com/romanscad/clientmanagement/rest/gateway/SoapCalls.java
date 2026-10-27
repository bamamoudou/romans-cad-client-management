package com.romanscad.clientmanagement.rest.gateway;

import com.romanscad.clientmanagement.rest.error.ApiException;
import com.romanscad.clientmanagement.rest.soap.generated.ClientManagementFault;
import com.romanscad.clientmanagement.rest.soap.generated.ErrorDetail;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.soap.SOAPFaultException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.http.HttpTimeoutException;
import java.nio.channels.ClosedChannelException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Exécute un appel SOAP et traduit ce qui peut mal tourner en ApiException :
 *
 * - faute métier du serveur : 400, 404 ou 409 selon son code d'erreur
 * - serveur SOAP injoignable : 503 Service Unavailable
 * - serveur SOAP trop lent : 504 Gateway Timeout
 * - faute SOAP imprévue ou autre problème de protocole : 502 Bad Gateway
 *
 * Pour une passerelle, ce sont les statuts HTTP prévus : le client REST doit pouvoir distinguer
 * « votre requête est mauvaise » (4xx) de « la passerelle n'arrive pas à joindre son serveur » (5xx).
 * Les détails techniques (adresse du serveur SOAP, exceptions) restent dans le journal, pas dans la réponse.
 */
final class SoapCalls {

    private static final Logger LOGGER = Logger.getLogger(SoapCalls.class.getName());

    /** Un appel SOAP qui renvoie un résultat. */
    @FunctionalInterface
    interface Call<T> {
        T execute() throws ClientManagementFault;
    }

    /** Un appel SOAP sans résultat. */
    @FunctionalInterface
    interface VoidCall {
        void execute() throws ClientManagementFault;
    }

    private SoapCalls() {
        // Classe utilitaire : non instanciable
    }

    static <T> T call(Call<T> call) {
        try {
            return call.execute();
        } catch (ClientManagementFault fault) {
            throw fromFault(fault);
        } catch (SOAPFaultException e) { // doit précéder WebServiceException, dont elle hérite
            LOGGER.log(Level.WARNING, "The SOAP service returned an unexpected fault", e);
            throw new ApiException(502, "SOAP_SERVICE_ERROR", "The SOAP service failed to process the request", e);
        } catch (WebServiceException e) {
            throw fromTransportFailure(e);
        }
    }

    static void run(VoidCall call) {
        call(() -> {
            call.execute();
            return null;
        });
    }

    /**
     * Le switch n'a pas de "default" : si le serveur ajoute un code d'erreur et qu'on régénère les
     * stubs, ce fichier ne compile plus tant que le nouveau code n'a pas reçu son statut HTTP.
     */
    private static ApiException fromFault(ClientManagementFault fault) {
        ErrorDetail detail = fault.getFaultInfo();
        if (detail == null || detail.getCode() == null) {
            LOGGER.log(Level.WARNING, "The SOAP service sent a fault without an error code", fault);
            return new ApiException(502, "SOAP_SERVICE_ERROR", "The SOAP service sent an unintelligible error", fault);
        }
        int status = switch (detail.getCode()) {
            case CUSTOMER_NOT_FOUND, GROUP_NOT_FOUND -> 404;
            case DUPLICATE_GROUP_NAME -> 409;
            case INVALID_REQUEST -> 400;
        };
        return new ApiException(status, detail.getCode().name(), detail.getMessage(), fault);
    }

    private static ApiException fromTransportFailure(WebServiceException error) {
        Throwable root = error;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        LOGGER.log(Level.WARNING, "Cannot talk to the SOAP service", error);
        if (root instanceof ConnectException || root instanceof ClosedChannelException || root instanceof UnknownHostException) {
            return new ApiException(503, "SOAP_SERVICE_UNAVAILABLE", "The SOAP service is unreachable", error);
        }
        if (root instanceof SocketTimeoutException || root instanceof HttpTimeoutException) {
            return new ApiException(504, "SOAP_SERVICE_TIMEOUT", "The SOAP service did not answer in time", error);
        }
        return new ApiException(502, "SOAP_SERVICE_ERROR", "The SOAP service call failed", error);
    }
}