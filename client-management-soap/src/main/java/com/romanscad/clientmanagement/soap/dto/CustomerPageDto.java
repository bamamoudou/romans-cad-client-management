package com.romanscad.clientmanagement.soap.dto;

import com.romanscad.clientmanagement.soap.SoapContract;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * Une page de clients dans le contrat SOAP.
 *
 * JAXB ne sait pas représenter un type générique comme Page<T> : chaque type de page a donc
 * sa propre classe concrète. Cette répétition est le prix d'un contrat WSDL fiable.
 *
 * nextCursor est absent (null) sur la dernière page ; sinon, il faut le renvoyer comme
 * afterId pour obtenir la page suivante.
 */
@XmlType(name = "CustomerPage", namespace = SoapContract.NAMESPACE,
        propOrder = {"items", "nextCursor"})
public class CustomerPageDto {

    private List<CustomerDto> items = new ArrayList<>();
    private Long nextCursor;

    /** Requis par JAXB. */
    public CustomerPageDto() {
    }

    public CustomerPageDto(List<CustomerDto> items, Long nextCursor) {
        this.items = items;
        this.nextCursor = nextCursor;
    }

    /** Chaque client devient un élément répété {@code <item>} (et non {@code <items>}). */
    @XmlElement(name = "item")
    public List<CustomerDto> getItems() {
        return items;
    }

    public void setItems(List<CustomerDto> items) {
        this.items = items;
    }

    public Long getNextCursor() {
        return nextCursor;
    }

    public void setNextCursor(Long nextCursor) {
        this.nextCursor = nextCursor;
    }
}