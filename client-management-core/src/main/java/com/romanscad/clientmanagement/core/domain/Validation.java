package com.romanscad.clientmanagement.core.domain;

import com.romanscad.clientmanagement.core.exception.ValidationException;

/**
 * Règles de validation partagées par les objets du domaine.
 * Visibilité package : c'est un détail interne, non exposé aux autres couches.
 */
final class Validation {

    private Validation() {
        // Classe utilitaire : non instanciable
    }

    /** Valide un texte obligatoire : non vide, longueur bornée. Retourne la valeur sans espaces superflus. */
    static String requireText(String field, String value, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(field + " must not be blank");
        }
        return checkLength(field, value.strip(), maxLength);
    }

    /** Valide un texte facultatif : null devient une chaîne vide. */
    static String optionalText(String field, String value, int maxLength) {
        return value == null ? "" : checkLength(field, value.strip(), maxLength);
    }

    /** Valide qu'un identifiant est strictement positif. */
    static long requirePositive(String field, long value) {
        if (value <= 0) {
            throw new ValidationException(field + " must be > 0");
        }
        return value;
    }

    private static String checkLength(String field, String value, int maxLength) {
        if (value.length() > maxLength) {
            throw new ValidationException(field + " must not exceed " + maxLength + " characters");
        }
        return value;
    }
}