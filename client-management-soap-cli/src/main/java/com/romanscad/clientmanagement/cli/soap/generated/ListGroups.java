package com.romanscad.clientmanagement.cli.soap.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour listGroups complex type.</p>
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.</p>
 * 
 * <pre>{@code
 * <complexType name="listGroups">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
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
@XmlType(name = "listGroups", propOrder = {
    "afterId",
    "limit"
})
public class ListGroups {

    protected Long afterId;
    protected Integer limit;

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
