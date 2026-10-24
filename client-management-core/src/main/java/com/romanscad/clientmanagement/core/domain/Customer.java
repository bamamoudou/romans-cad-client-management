package com.romanscad.clientmanagement.core.domain;

import com.romanscad.clientmanagement.core.exception.ValidationException;
import java.util.Locale;

/**
 * Un client.
 *
 * Implémenté en record : objet immuable, thread-safe par construction, avec
 * equals/hashCode/toString générés. Les mêmes instances sont lues simultanément par
 * plusieurs requêtes, sans aucune synchronisation.
 *
 * Les invariants sont vérifiés dans le constructeur compact : il est impossible de créer
 * un client invalide, quelle que soit la couche appelante.
 *
 * @param id        identifiant unique, généré par la base (strictement positif)
 * @param firstName prénom (obligatoire)
 * @param lastName  nom (obligatoire)
 * @param email     adresse e-mail, normalisée en minuscules (obligatoire)
 */
public record Customer(long id, String firstName, String lastName, String email) {

    public static final int MAX_NAME_LENGTH = 100;
    public static final int MAX_EMAIL_LENGTH = 254;

    public Customer {
        Validation.requirePositive("id", id);
        firstName = Validation.requireText("firstName", firstName, MAX_NAME_LENGTH);
        lastName = Validation.requireText("lastName", lastName, MAX_NAME_LENGTH);
        email = requireEmail(email);
    }

    /**
     * Contrôle volontairement minimal : un seul '@' avec du texte de chaque côté.
     * La vraie validation d'une adresse est l'envoi d'un e-mail, pas une regex.
     */
    private static String requireEmail(String value) {
        String email = Validation.requireText("email", value, MAX_EMAIL_LENGTH)
                .toLowerCase(Locale.ROOT);
        int at = email.indexOf('@');
        if (at < 1 || at != email.lastIndexOf('@') || at == email.length() - 1) {
            throw new ValidationException("email is not a valid address");
        }
        return email;
    }
}