package com.romanscad.clientmanagement.rest.dto;

/** Corps de POST /customers. La validation métier est faite par le serveur SOAP. */
public record CreateCustomerRequest(String firstName, String lastName, String email) {
}