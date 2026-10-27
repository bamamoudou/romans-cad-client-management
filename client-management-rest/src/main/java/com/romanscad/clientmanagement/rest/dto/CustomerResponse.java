package com.romanscad.clientmanagement.rest.dto;

/** Un client tel que l'expose l'API REST. */
public record CustomerResponse(long id, String firstName, String lastName, String email) {
}