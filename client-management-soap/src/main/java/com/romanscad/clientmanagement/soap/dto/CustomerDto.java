package com.romanscad.clientmanagement.soap.dto;

import com.romanscad.clientmanagement.soap.SoapContract;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Représentation d'un client dans le contrat SOAP.
 *
 * Classe ordinaire (constructeur sans argument, getters et setters) car JAXB ne sait pas
 * construire un record. Elle est volontairement séparée du Customer du domaine : le contrat
 * public peut ainsi évoluer sans toucher au modèle interne, et inversement.
 */
@XmlType(name = "Customer", namespace = SoapContract.NAMESPACE,
        propOrder = {"id", "firstName", "lastName", "email"})
public class CustomerDto {

    private long id;
    private String firstName;
    private String lastName;
    private String email;

    /** Requis par JAXB pour instancier l'objet à la désérialisation. */
    public CustomerDto() {
    }

    public CustomerDto(long id, String firstName, String lastName, String email) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}