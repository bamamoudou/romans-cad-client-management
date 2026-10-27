package com.romanscad.clientmanagement.rest.dto;

/** Nombre d'appartenances réellement modifiées (les doublons et les absents ne comptent pas). */
public record MembershipResult(int count) {
}