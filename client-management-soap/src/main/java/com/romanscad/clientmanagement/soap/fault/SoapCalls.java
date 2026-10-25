package com.romanscad.clientmanagement.soap.fault;

import com.romanscad.clientmanagement.core.exception.ClientManagementException;
import jakarta.xml.ws.WebServiceException;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enveloppe commune de toutes les opérations SOAP : une seule gestion des erreurs, au lieu d'un
 * try/catch recopié dans chaque méthode des endpoints.
 *
 * - erreur métier prévue (ClientManagementException) : traduite en faute SOAP typée
 * - toute autre erreur (bug, état interne incohérent...) : journalisée en détail côté serveur,
 *   mais le client ne reçoit qu'un message générique. On n'expose jamais de détail interne
 *   (noms de classes, pile d'appels, données) à l'extérieur.
 */
public final class SoapCalls {

    private static final Logger LOGGER = Logger.getLogger(SoapCalls.class.getName());

    private SoapCalls() {
        // Classe utilitaire : non instanciable
    }

    /** Exécute une opération qui renvoie un résultat. */
    public static <T> T call(Supplier<T> action) throws ClientManagementFault {
        try {
            return action.get();
        } catch (ClientManagementException e) {
            throw SoapFaults.from(e);
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Unexpected error while handling a SOAP request", e);
            throw new WebServiceException("Internal server error");
        }
    }

    /** Exécute une opération sans résultat. */
    public static void run(Runnable action) throws ClientManagementFault {
        call(() -> {
            action.run();
            return null;
        });
    }
}