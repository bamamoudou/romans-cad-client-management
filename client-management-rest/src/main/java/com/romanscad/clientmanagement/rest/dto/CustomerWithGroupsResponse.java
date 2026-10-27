package com.romanscad.clientmanagement.rest.dto;

import java.util.List;

/** Un client et la liste de ses groupes. */
public record CustomerWithGroupsResponse(CustomerResponse customer, List<GroupResponse> groups) {
}