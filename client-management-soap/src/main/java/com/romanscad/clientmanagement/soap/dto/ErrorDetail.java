package com.romanscad.clientmanagement.soap.dto;

import com.romanscad.clientmanagement.soap.SoapContract;
import jakarta.xml.bind.annotation.XmlType;
import java.io.Serializable;

/**
 * Détail structuré d'une erreur, transporté dans la faute SOAP.
 *
 * Il est Serializable car la faute (une exception) le contient et les exceptions Java
 * sont sérialisables.
 */
@XmlType(name = "ErrorDetail", namespace = SoapContract.NAMESPACE,
        propOrder = {"code", "message", "resourceId"})
public class ErrorDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    private ErrorCode code;
    private String message;
    private Long resourceId;

    /** Requis par JAXB. */
    public ErrorDetail() {
    }

    /**
     * @param code       nature de l'erreur
     * @param message    explication lisible par un humain (en anglais)
     * @param resourceId identifiant de la ressource concernée (client ou groupe introuvable),
     *                   ou null quand il n'y en a pas
     */
    public ErrorDetail(ErrorCode code, String message, Long resourceId) {
        this.code = code;
        this.message = message;
        this.resourceId = resourceId;
    }

    public ErrorCode getCode() {
        return code;
    }

    public void setCode(ErrorCode code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }
}