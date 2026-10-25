package com.romanscad.clientmanagement.soap.dto;

import com.romanscad.clientmanagement.soap.SoapContract;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/** Une page de clients avec leurs groupes (la pagination porte sur les clients). */
@XmlType(name = "CustomerWithGroupsPage", namespace = SoapContract.NAMESPACE,
        propOrder = {"items", "nextCursor"})
public class CustomerWithGroupsPageDto {

    private List<CustomerWithGroupsDto> items = new ArrayList<>();
    private Long nextCursor;

    /** Requis par JAXB. */
    public CustomerWithGroupsPageDto() {
    }

    public CustomerWithGroupsPageDto(List<CustomerWithGroupsDto> items, Long nextCursor) {
        this.items = items;
        this.nextCursor = nextCursor;
    }

    @XmlElement(name = "item")
    public List<CustomerWithGroupsDto> getItems() {
        return items;
    }

    public void setItems(List<CustomerWithGroupsDto> items) {
        this.items = items;
    }

    public Long getNextCursor() {
        return nextCursor;
    }

    public void setNextCursor(Long nextCursor) {
        this.nextCursor = nextCursor;
    }
}