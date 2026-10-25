/**
 * DTO du contrat SOAP.
 *
 * Tous les éléments XML du package appartiennent au namespace du contrat (QUALIFIED) :
 * le schéma du WSDL est ainsi homogène, sans éléments "sans namespace" mélangés aux autres.
 */
@XmlSchema(namespace = SoapContract.NAMESPACE, elementFormDefault = XmlNsForm.QUALIFIED)
package com.romanscad.clientmanagement.soap.dto;

import com.romanscad.clientmanagement.soap.SoapContract;
import jakarta.xml.bind.annotation.XmlNsForm;
import jakarta.xml.bind.annotation.XmlSchema;