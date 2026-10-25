package com.romanscad.clientmanagement.soap.fault;

import com.romanscad.clientmanagement.soap.SoapContract;
import com.romanscad.clientmanagement.soap.dto.ErrorDetail;
import jakarta.xml.ws.WebFault;

/**
 * Faute SOAP unique du contrat : toutes les erreurs métier voyagent dans ce même type,
 * et c'est le code du détail qui les distingue.
 *
 * Exception vérifiée (Exception, pas RuntimeException) : c'est ce que demande JAX-WS pour
 * déclarer la faute dans le WSDL. Les deux constructeurs et getFaultInfo() suivent la forme
 * imposée par la spécification.
 */
@WebFault(name = "ClientManagementFault", targetNamespace = SoapContract.NAMESPACE)
public class ClientManagementFault extends Exception {

    private static final long serialVersionUID = 1L;

    private final ErrorDetail faultInfo;

    public ClientManagementFault(String message, ErrorDetail faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    public ClientManagementFault(String message, ErrorDetail faultInfo, Throwable cause) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }

    /** Le détail structuré, tel qu'il apparaît dans la faute SOAP. */
    public ErrorDetail getFaultInfo() {
        return faultInfo;
    }
}