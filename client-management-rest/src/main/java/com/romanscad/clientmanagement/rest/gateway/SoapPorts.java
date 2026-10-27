package com.romanscad.clientmanagement.rest.gateway;

import com.romanscad.clientmanagement.rest.config.GatewayConfiguration;
import com.romanscad.clientmanagement.rest.soap.generated.CustomerWebService;
import com.romanscad.clientmanagement.rest.soap.generated.CustomerWebService_Service;
import com.romanscad.clientmanagement.rest.soap.generated.GroupWebService;
import com.romanscad.clientmanagement.rest.soap.generated.GroupWebService_Service;
import com.romanscad.clientmanagement.rest.soap.generated.MembershipWebService;
import com.romanscad.clientmanagement.rest.soap.generated.MembershipWebService_Service;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.xml.ws.BindingProvider;
import java.util.Map;

/**
 * Fournit les trois ports SOAP, créés une seule fois et à la demande.
 *
 * Créer un port est coûteux (lecture du WSDL, construction du client), donc on ne le fait ni à
 * chaque requête ni au démarrage : le déploiement réussit même si le serveur SOAP n'est pas encore
 * prêt. Les ports de CXF (l'implémentation JAX-WS de WildFly) supportent les appels concurrents tant
 * que leur configuration n'est plus modifiée après la création, ce qui est le cas ici.
 */
@ApplicationScoped
public class SoapPorts {

    /** Délai pour établir la connexion, en millisecondes. */
    static final int CONNECT_TIMEOUT_MILLIS = 5_000;
    /** Délai d'attente d'une réponse, en millisecondes. */
    static final int RECEIVE_TIMEOUT_MILLIS = 60_000;

    // Injection par champ : un bean ApplicationScoped est proxifié et doit avoir un constructeur sans argument.
    @Inject
    GatewayConfiguration configuration;

    private CustomerWebService customers;
    private GroupWebService groups;
    private MembershipWebService memberships;

    public synchronized CustomerWebService customers() {
        if (customers == null) {
            customers = configure(new CustomerWebService_Service().getCustomerWebServicePort(), "/CustomerWebService");
        }
        return customers;
    }

    public synchronized GroupWebService groups() {
        if (groups == null) {
            groups = configure(new GroupWebService_Service().getGroupWebServicePort(), "/GroupWebService");
        }
        return groups;
    }

    public synchronized MembershipWebService memberships() {
        if (memberships == null) {
            memberships = configure(new MembershipWebService_Service().getMembershipWebServicePort(), "/MembershipWebService");
        }
        return memberships;
    }

    /** Fixe l'adresse réelle du serveur et les délais : le WSDL embarqué ne fournit qu'une adresse d'exemple. */
    private <P> P configure(P port, String path) {
        Map<String, Object> context = ((BindingProvider) port).getRequestContext();
        context.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, configuration.soapBaseUrl() + path);
        context.put("jakarta.xml.ws.client.connectionTimeout", CONNECT_TIMEOUT_MILLIS);
        context.put("jakarta.xml.ws.client.receiveTimeout", RECEIVE_TIMEOUT_MILLIS);
        return port;
    }
}