package com.romanscad.clientmanagement.soap.dto;

import com.romanscad.clientmanagement.soap.SoapContract;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/** Un client avec les groupes auxquels il appartient (vue « clients et groupes associés »). */
@XmlType(name = "CustomerWithGroups", namespace = SoapContract.NAMESPACE,
        propOrder = {"customer", "groups"})
public class CustomerWithGroupsDto {

    private CustomerDto customer;
    private List<GroupDto> groups = new ArrayList<>();

    /** Requis par JAXB. */
    public CustomerWithGroupsDto() {
    }

    public CustomerWithGroupsDto(CustomerDto customer, List<GroupDto> groups) {
        this.customer = customer;
        this.groups = groups;
    }

    public CustomerDto getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerDto customer) {
        this.customer = customer;
    }

    /** Chaque groupe devient un élément répété {@code <group>}. */
    @XmlElement(name = "group")
    public List<GroupDto> getGroups() {
        return groups;
    }

    public void setGroups(List<GroupDto> groups) {
        this.groups = groups;
    }
}