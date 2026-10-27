package com.romanscad.clientmanagement.cli.soap.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour addCustomersToGroupResponse complex type.</p>
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.</p>
 * 
 * <pre>{@code
 * <complexType name="addCustomersToGroupResponse">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="added" type="{http://www.w3.org/2001/XMLSchema}int" form="qualified"/>
 *       </sequence>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "addCustomersToGroupResponse", propOrder = {
    "added"
})
public class AddCustomersToGroupResponse {

    protected int added;

    /**
     * Obtient la valeur de la propriété added.
     * 
     */
    public int getAdded() {
        return added;
    }

    /**
     * Définit la valeur de la propriété added.
     * 
     */
    public void setAdded(int value) {
        this.added = value;
    }

}
