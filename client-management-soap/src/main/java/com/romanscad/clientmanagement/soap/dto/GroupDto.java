package com.romanscad.clientmanagement.soap.dto;

import com.romanscad.clientmanagement.soap.SoapContract;
import jakarta.xml.bind.annotation.XmlType;

/** Représentation d'un groupe dans le contrat SOAP (même principe que CustomerDto). */
@XmlType(name = "Group", namespace = SoapContract.NAMESPACE,
        propOrder = {"id", "name", "description"})
public class GroupDto {

    private long id;
    private String name;
    private String description;

    /** Requis par JAXB pour instancier l'objet à la désérialisation. */
    public GroupDto() {
    }

    public GroupDto(long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}