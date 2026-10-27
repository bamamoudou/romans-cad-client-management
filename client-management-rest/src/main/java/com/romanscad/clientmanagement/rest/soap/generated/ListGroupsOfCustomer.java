package com.romanscad.clientmanagement.rest.soap.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour listGroupsOfCustomer complex type.</p>
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.</p>
 * 
 * <pre>{@code
 * <complexType name="listGroupsOfCustomer">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="customerId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         <element name="afterId" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         <element name="limit" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *       </sequence>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "listGroupsOfCustomer", propOrder = {
    "customerId",
    "afterId",
    "limit"
})
public class ListGroupsOfCustomer {

    @XmlElement(namespace = "")
    protected long customerId;
    @XmlElement(namespace = "")
    protected Long afterId;
    @XmlElement(namespace = "")
    protected Integer limit;

    /**
     * Obtient la valeur de la propriété customerId.
     * 
     */
    public long getCustomerId() {
        return customerId;
    }

    /**
     * Définit la valeur de la propriété customerId.
     * 
     */
    public void setCustomerId(long value) {
        this.customerId = value;
    }

    /**
     * Obtient la valeur de la propriété afterId.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getAfterId() {
        return afterId;
    }

    /**
     * Définit la valeur de la propriété afterId.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setAfterId(Long value) {
        this.afterId = value;
    }

    /**
     * Obtient la valeur de la propriété limit.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getLimit() {
        return limit;
    }

    /**
     * Définit la valeur de la propriété limit.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setLimit(Integer value) {
        this.limit = value;
    }

}
