package com.romanscad.clientmanagement.core.exception;

/** Aucun client ne correspond à l'identifiant demandé (SOAP : faute, REST : 404). */
public final class CustomerNotFoundException extends ClientManagementException {

    private static final long serialVersionUID = 1L;

    private final long customerId;

    public CustomerNotFoundException(long customerId) {
        super("Customer " + customerId + " not found");
        this.customerId = customerId;
    }

    public long getCustomerId() {
        return customerId;
    }
}