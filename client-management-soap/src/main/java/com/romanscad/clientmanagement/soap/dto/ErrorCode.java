package com.romanscad.clientmanagement.soap.dto;

import com.romanscad.clientmanagement.soap.SoapContract;
import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Codes d'erreur du contrat SOAP : le vocabulaire commun entre le serveur et ses clients.
 *
 * Un client lit ce code pour décider quoi faire (réessayer, corriger la saisie, ignorer...)
 * sans jamais analyser le texte du message, qui n'est destiné qu'aux humains.
 */
@XmlType(name = "ErrorCode", namespace = SoapContract.NAMESPACE)
@XmlEnum
public enum ErrorCode {

    /** Le client demandé n'existe pas. */
    CUSTOMER_NOT_FOUND,

    /** Le groupe demandé n'existe pas. */
    GROUP_NOT_FOUND,

    /** Un groupe de ce nom existe déjà. */
    DUPLICATE_GROUP_NAME,

    /** La requête est invalide : champ vide, trop long, taille de page hors limites... */
    INVALID_REQUEST
}