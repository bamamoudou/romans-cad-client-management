package com.romanscad.clientmanagement.soap;

/**
 * Constantes du contrat SOAP partagées par les DTO et les endpoints.
 *
 * Le namespace identifie la version du contrat dans le WSDL : un changement incompatible
 * donnerait un nouveau namespace (v2), sans casser les clients existants.
 */
public final class SoapContract {

    public static final String NAMESPACE = "http://clientmanagement.romanscad.com/v1";

    private SoapContract() {
        // Classe de constantes : non instanciable
    }
}