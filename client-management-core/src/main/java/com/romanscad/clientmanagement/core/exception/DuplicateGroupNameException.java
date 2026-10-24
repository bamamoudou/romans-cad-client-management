package com.romanscad.clientmanagement.core.exception;

/** Un groupe portant ce nom existe déjà (SOAP : faute, REST : 409 Conflict). */
public final class DuplicateGroupNameException extends ClientManagementException {

    private static final long serialVersionUID = 1L;

    private final String groupName;

    public DuplicateGroupNameException(String groupName) {
        super("A group named '" + groupName + "' already exists");
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }
}