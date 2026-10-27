package com.romanscad.clientmanagement.cli.rest;

import com.romanscad.clientmanagement.cli.Services;

/** Fabrique des trois services branchés sur la passerelle REST : le seul endroit qui connaît l'adresse. */
public final class RestServices {

    public static final String DEFAULT_BASE_URL = "http://localhost:8080/client-management-rest/api";

    private RestServices() {
        // Classe utilitaire : non instanciable
    }

    /**
     * @param baseUrl adresse de l'API, par exemple http://localhost:8080/client-management-rest/api
     * @throws IllegalArgumentException si l'adresse n'est pas une adresse http(s) valide
     */
    public static Services create(String baseUrl) {
        RestClient client = new RestClient(baseUrl);
        return new Services(
                new RestCustomerService(client),
                new RestGroupService(client),
                new RestMembershipService(client));
    }
}