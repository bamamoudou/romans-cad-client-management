package com.romanscad.clientmanagement.rest.dto;

import java.util.List;

/** Corps des opérations d'association et de dissociation : les clients concernés. */
public record MembershipRequest(List<Long> customerIds) {
}