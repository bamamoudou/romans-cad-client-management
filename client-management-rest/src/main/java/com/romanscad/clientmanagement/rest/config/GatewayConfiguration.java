package com.romanscad.clientmanagement.rest.config;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Configuration de la passerelle : où se trouve le serveur SOAP.
 *
 * Ordre de priorité : propriété système, puis variable d'environnement, puis valeur par défaut.
 * Une propriété système se règle au lancement de WildFly (-Dclientmanagement.soap.url=...),
 * la variable d'environnement convient mieux aux conteneurs.
 */
@ApplicationScoped
public class GatewayConfiguration {

    public static final String SOAP_URL_PROPERTY = "clientmanagement.soap.url";
    public static final String SOAP_URL_ENV = "CLIENTMANAGEMENT_SOAP_URL";
    public static final String DEFAULT_SOAP_URL = "http://localhost:8080/client-management-soap";

    private String soapBaseUrl;

    @PostConstruct
    void init() {
        soapBaseUrl = resolve(System.getProperty(SOAP_URL_PROPERTY), System.getenv(SOAP_URL_ENV));
    }

    /** Adresse de l'application SOAP, sans "/" final. */
    public String soapBaseUrl() {
        return soapBaseUrl;
    }

    /** Logique pure, séparée de la lecture de l'environnement pour rester testable. */
    static String resolve(String property, String environment) {
        String chosen = firstNonBlank(property, environment, DEFAULT_SOAP_URL);
        String trimmed = chosen.strip();
        return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
    }

    private static String firstNonBlank(String... candidates) {
        for (String candidate : candidates) {
            if (candidate != null && !candidate.isBlank()) {
                return candidate;
            }
        }
        throw new IllegalStateException("unreachable: the last candidate is never blank");
    }
}