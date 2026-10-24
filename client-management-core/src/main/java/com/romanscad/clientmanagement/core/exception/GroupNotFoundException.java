package com.romanscad.clientmanagement.core.exception;

/** Aucun groupe ne correspond à l'identifiant demandé (SOAP : faute, REST : 404). */
public final class GroupNotFoundException extends ClientManagementException {

    private static final long serialVersionUID = 1L;

    private final long groupId;

    public GroupNotFoundException(long groupId) {
        super("Group " + groupId + " not found");
        this.groupId = groupId;
    }

    public long getGroupId() {
        return groupId;
    }
}