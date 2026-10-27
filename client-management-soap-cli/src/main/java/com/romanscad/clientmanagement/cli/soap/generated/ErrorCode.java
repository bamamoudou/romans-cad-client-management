package com.romanscad.clientmanagement.cli.soap.generated;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * 
 * <p>Classe Java pour ErrorCode.</p>
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.</p>
 * <pre>{@code
 * <simpleType name="ErrorCode">
 *   <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     <enumeration value="CUSTOMER_NOT_FOUND"/>
 *     <enumeration value="GROUP_NOT_FOUND"/>
 *     <enumeration value="DUPLICATE_GROUP_NAME"/>
 *     <enumeration value="INVALID_REQUEST"/>
 *   </restriction>
 * </simpleType>
 * }</pre>
 * 
 */
@XmlType(name = "ErrorCode")
@XmlEnum
public enum ErrorCode {

    CUSTOMER_NOT_FOUND,
    GROUP_NOT_FOUND,
    DUPLICATE_GROUP_NAME,
    INVALID_REQUEST;

    public String value() {
        return name();
    }

    public static ErrorCode fromValue(String v) {
        return valueOf(v);
    }

}
