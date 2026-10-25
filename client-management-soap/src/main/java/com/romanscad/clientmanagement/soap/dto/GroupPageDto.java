package com.romanscad.clientmanagement.soap.dto;

import com.romanscad.clientmanagement.soap.SoapContract;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/** Une page de groupes dans le contrat SOAP (même principe que CustomerPageDto). */
@XmlType(name = "GroupPage", namespace = SoapContract.NAMESPACE,
        propOrder = {"items", "nextCursor"})
public class GroupPageDto {

    private List<GroupDto> items = new ArrayList<>();
    private Long nextCursor;

    /** Requis par JAXB. */
    public GroupPageDto() {
    }

    public GroupPageDto(List<GroupDto> items, Long nextCursor) {
        this.items = items;
        this.nextCursor = nextCursor;
    }

    @XmlElement(name = "item")
    public List<GroupDto> getItems() {
        return items;
    }

    public void setItems(List<GroupDto> items) {
        this.items = items;
    }

    public Long getNextCursor() {
        return nextCursor;
    }

    public void setNextCursor(Long nextCursor) {
        this.nextCursor = nextCursor;
    }
}