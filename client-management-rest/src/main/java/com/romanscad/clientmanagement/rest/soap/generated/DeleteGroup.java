package com.romanscad.clientmanagement.rest.soap.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour deleteGroup complex type.</p>
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.</p>
 * 
 * <pre>{@code
 * <complexType name="deleteGroup">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="groupId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *       </sequence>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "deleteGroup", propOrder = {
    "groupId"
})
public class DeleteGroup {

    protected long groupId;

    /**
     * Obtient la valeur de la propriété groupId.
     * 
     */
    public long getGroupId() {
        return groupId;
    }

    /**
     * Définit la valeur de la propriété groupId.
     * 
     */
    public void setGroupId(long value) {
        this.groupId = value;
    }

}
