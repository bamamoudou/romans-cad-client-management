package com.romanscad.clientmanagement.rest.soap.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour ErrorDetail complex type.</p>
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.</p>
 * 
 * <pre>{@code
 * <complexType name="ErrorDetail">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="code" type="{http://clientmanagement.romanscad.com/v1}ErrorCode" minOccurs="0" form="qualified"/>
 *         <element name="message" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0" form="qualified"/>
 *         <element name="resourceId" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0" form="qualified"/>
 *       </sequence>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ErrorDetail", propOrder = {
    "code",
    "message",
    "resourceId"
})
public class ErrorDetail {

    @XmlSchemaType(name = "string")
    protected ErrorCode code;
    protected String message;
    protected Long resourceId;

    /**
     * Obtient la valeur de la propriété code.
     * 
     * @return
     *     possible object is
     *     {@link ErrorCode }
     *     
     */
    public ErrorCode getCode() {
        return code;
    }

    /**
     * Définit la valeur de la propriété code.
     * 
     * @param value
     *     allowed object is
     *     {@link ErrorCode }
     *     
     */
    public void setCode(ErrorCode value) {
        this.code = value;
    }

    /**
     * Obtient la valeur de la propriété message.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessage() {
        return message;
    }

    /**
     * Définit la valeur de la propriété message.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessage(String value) {
        this.message = value;
    }

    /**
     * Obtient la valeur de la propriété resourceId.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getResourceId() {
        return resourceId;
    }

    /**
     * Définit la valeur de la propriété resourceId.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setResourceId(Long value) {
        this.resourceId = value;
    }

}
