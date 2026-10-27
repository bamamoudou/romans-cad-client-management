package com.romanscad.clientmanagement.cli.soap;

import com.romanscad.clientmanagement.cli.Services;
import com.romanscad.clientmanagement.cli.soap.generated.CustomerWebService_Service;
import com.romanscad.clientmanagement.cli.soap.generated.GroupWebService_Service;
import com.romanscad.clientmanagement.cli.soap.generated.MembershipWebService_Service;
import jakarta.xml.ws.BindingProvider;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Fabrique des trois services branchés sur le serveur SOAP : le seul endroit qui sait où
 * se trouvent les services et comment les configurer.
 *
 * Les WSDL sont embarqués dans le programme (dossier wsdl des ressources) : il n'est pas
 * nécessaire de joindre le serveur pour les lire. L'adresse réelle du serveur est fixée par
 * programme, donc le même programme fonctionne avec n'importe quel serveur.
 */
public final class SoapServices {

    public static final String DEFAULT_BASE_URL = "http://localhost:8080/client-management-soap";

    /** Délai pour établir la connexion, en millisecondes. */
    static final int CONNECT_TIMEOUT_MILLIS = 5_000;
    /** Délai d'attente d'une réponse, en millisecondes (une page de 1000 clients est rapide). */
    static final int RECEIVE_TIMEOUT_MILLIS = 60_000;

    private SoapServices() {
        // Classe utilitaire : non instanciable
    }

    /** @param baseUrl adresse de l'application, par exemple http://localhost:8080/client-management-soap */
    public static Services create(String baseUrl) {
        String base = withoutTrailingSlash(baseUrl);
        SoapInvoker invoker = new SoapInvoker(base);
        return new Services(
                new SoapCustomerService(lazy(() -> configure(
                        new CustomerWebService_Service().getCustomerWebServicePort(), base + "/CustomerWebService")), invoker),
                new SoapGroupService(lazy(() -> configure(
                        new GroupWebService_Service().getGroupWebServicePort(), base + "/GroupWebService")), invoker),
                new SoapMembershipService(lazy(() -> configure(
                        new MembershipWebService_Service().getMembershipWebServicePort(), base + "/MembershipWebService")), invoker));
    }

    static String withoutTrailingSlash(String url) {
        String trimmed = url.strip();
        return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
    }

    /** Fixe l'adresse du serveur et les délais sur un port. */
    private static <P> P configure(P port, String address) {
        Map<String, Object> context = ((BindingProvider) port).getRequestContext();
        context.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, address);
        // Clés "jakarta." : les anciennes clés "javax." sont ignorées par CXF 4 (vérifié).
        context.put("jakarta.xml.ws.client.connectionTimeout", CONNECT_TIMEOUT_MILLIS);
        context.put("jakarta.xml.ws.client.receiveTimeout", RECEIVE_TIMEOUT_MILLIS);
        return port;
    }

    /** Un Supplier qui ne calcule sa valeur qu'une fois, au premier appel. */
    private static <T> Supplier<T> lazy(Supplier<T> factory) {
        return new Supplier<>() {
            private T value;

            @Override
            public synchronized T get() {
                if (value == null) {
                    value = factory.get();
                }
                return value;
            }
        };
    }
}