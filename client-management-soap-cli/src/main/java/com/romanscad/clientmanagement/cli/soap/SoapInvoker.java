package com.romanscad.clientmanagement.cli.soap;

import com.romanscad.clientmanagement.cli.ServiceUnavailableException;
import com.romanscad.clientmanagement.cli.soap.generated.ClientManagementFault;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.soap.SOAPFaultException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.http.HttpTimeoutException;
import java.nio.channels.ClosedChannelException;

/**
 * Exécute un appel SOAP et traduit tout ce qui peut mal tourner :
 * - faute métier du serveur : exception du domaine (voir FaultTranslator)
 * - faute SOAP imprévue (erreur interne du serveur) : ServiceUnavailableException
 * - panne de transport (serveur arrêté, adresse fausse, délai dépassé) : ServiceUnavailableException
 *
 * Toute la gestion des erreurs réseau est ici : les adaptateurs restent de simples conversions.
 */
final class SoapInvoker {

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

    private final String baseUrl;

    SoapInvoker(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    <T> T invoke(Call<T> call) {
        return invoke(null, call);
    }

    /** @param groupName nom du groupe en cours de création (pour l'erreur de doublon), sinon null */
    <T> T invoke(String groupName, Call<T> call) {
        try {
            return call.execute();
        } catch (ClientManagementFault fault) {
            throw FaultTranslator.translate(fault, groupName);
        } catch (SOAPFaultException e) { // doit précéder WebServiceException, dont elle hérite
            throw new ServiceUnavailableException("The server failed to process the request: " + e.getMessage(), e);
        } catch (WebServiceException e) {
            throw new ServiceUnavailableException(
                    "Cannot reach the SOAP service at " + baseUrl + ": " + describe(e), e);
        }
    }

    void invokeVoid(VoidCall call) {
        invoke(() -> {
            call.execute();
            return null;
        });
    }

    /**
     * Décrit la cause la plus profonde en termes compréhensibles : "connection refused" plutôt que
     * "Could not send Message" ou le nom d'une exception technique.
     */
    static String describe(Throwable error) {
        Throwable root = error;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        if (root instanceof ConnectException) {
            return "connection refused";
        }
        if (root instanceof ClosedChannelException) {
            return "connection failed";
        }
        if (root instanceof SocketTimeoutException || root instanceof HttpTimeoutException) {
            return "timed out";
        }
        if (root instanceof UnknownHostException) {
            return "unknown host " + root.getMessage();
        }
        return root.getMessage() != null ? root.getMessage() : root.getClass().getSimpleName();
    }
}