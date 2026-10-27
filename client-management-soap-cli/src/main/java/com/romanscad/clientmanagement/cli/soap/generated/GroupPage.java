package com.romanscad.clientmanagement.cli.soap.generated;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour GroupPage complex type.</p>
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.</p>
 * 
 * <pre>{@code
 * <complexType name="GroupPage">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="item" type="{http://clientmanagement.romanscad.com/v1}Group" maxOccurs="unbounded" minOccurs="0" form="qualified"/>
 *         <element name="nextCursor" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0" form="qualified"/>
 *       </sequence>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GroupPage", propOrder = {
    "item",
    "nextCursor"
})
public class GroupPage {

    protected List<Group> item;
    protected Long nextCursor;

    /**
     * Gets the value of the item property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore, any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a {@code set} method for the item property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getItem().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Group }
     * </p>
     * 
     * 
     * @return
     *     The value of the item property.
     */
    public List<Group> getItem() {
        if (item == null) {
            item = new ArrayList<>();
        }
        return this.item;
    }

    /**
     * Obtient la valeur de la propriété nextCursor.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getNextCursor() {
        return nextCursor;
    }

    /**
     * Définit la valeur de la propriété nextCursor.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setNextCursor(Long value) {
        this.nextCursor = value;
    }

}
