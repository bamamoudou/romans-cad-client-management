package com.romanscad.clientmanagement.cli.soap.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour listCustomersWithGroupsResponse complex type.</p>
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.</p>
 * 
 * <pre>{@code
 * <complexType name="listCustomersWithGroupsResponse">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="page" type="{http://clientmanagement.romanscad.com/v1}CustomerWithGroupsPage" minOccurs="0" form="qualified"/>
 *       </sequence>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "listCustomersWithGroupsResponse", propOrder = {
    "page"
})
public class ListCustomersWithGroupsResponse {

    protected CustomerWithGroupsPage page;

    /**
     * Obtient la valeur de la propriété page.
     * 
     * @return
     *     possible object is
     *     {@link CustomerWithGroupsPage }
     *     
     */
    public CustomerWithGroupsPage getPage() {
        return page;
    }

    /**
     * Définit la valeur de la propriété page.
     * 
     * @param value
     *     allowed object is
     *     {@link CustomerWithGroupsPage }
     *     
     */
    public void setPage(CustomerWithGroupsPage value) {
        this.page = value;
    }

}
