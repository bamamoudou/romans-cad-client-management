package com.romanscad.clientmanagement.rest.soap.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour removeCustomersFromGroupResponse complex type.</p>
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.</p>
 * 
 * <pre>{@code
 * <complexType name="removeCustomersFromGroupResponse">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="removed" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       </sequence>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "removeCustomersFromGroupResponse", propOrder = {
    "removed"
})
public class RemoveCustomersFromGroupResponse {

    @XmlElement(namespace = "")
    protected int removed;

    /**
     * Obtient la valeur de la propriété removed.
     * 
     */
    public int getRemoved() {
        return removed;
    }

    /**
     * Définit la valeur de la propriété removed.
     * 
     */
    public void setRemoved(int value) {
        this.removed = value;
    }

}
