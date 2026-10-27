package com.romanscad.clientmanagement.rest.soap.generated;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour removeCustomersFromGroup complex type.</p>
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.</p>
 * 
 * <pre>{@code
 * <complexType name="removeCustomersFromGroup">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="groupId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         <element name="customerId" type="{http://www.w3.org/2001/XMLSchema}long" maxOccurs="unbounded" minOccurs="0"/>
 *       </sequence>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "removeCustomersFromGroup", propOrder = {
    "groupId",
    "customerId"
})
public class RemoveCustomersFromGroup {

    @XmlElement(namespace = "")
    protected long groupId;
    @XmlElement(namespace = "", type = Long.class)
    protected List<Long> customerId;

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

    /**
     * Gets the value of the customerId property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore, any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a {@code set} method for the customerId property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getCustomerId().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     * </p>
     * 
     * 
     * @return
     *     The value of the customerId property.
     */
    public List<Long> getCustomerId() {
        if (customerId == null) {
            customerId = new ArrayList<>();
        }
        return this.customerId;
    }

}
