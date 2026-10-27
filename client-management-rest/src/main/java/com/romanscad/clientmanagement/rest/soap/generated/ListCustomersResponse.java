package com.romanscad.clientmanagement.rest.soap.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour listCustomersResponse complex type.</p>
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.</p>
 * 
 * <pre>{@code
 * <complexType name="listCustomersResponse">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="page" type="{http://clientmanagement.romanscad.com/v1}CustomerPage" minOccurs="0"/>
 *       </sequence>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "listCustomersResponse", propOrder = {
    "page"
})
public class ListCustomersResponse {

    protected CustomerPage page;

    /**
     * Obtient la valeur de la propriété page.
     * 
     * @return
     *     possible object is
     *     {@link CustomerPage }
     *     
     */
    public CustomerPage getPage() {
        return page;
    }

    /**
     * Définit la valeur de la propriété page.
     * 
     * @param value
     *     allowed object is
     *     {@link CustomerPage }
     *     
     */
    public void setPage(CustomerPage value) {
        this.page = value;
    }

}
