package com.romanscad.clientmanagement.core.domain;

/**
 * Un groupe de clients.
 *
 * Le groupe ne contient PAS la liste de ses clients : un groupe peut en rassembler des
 * centaines de milliers. L'appartenance est gérée à part (MembershipRepository) et
 * toujours consultée page par page.
 *
 * @param id          identifiant unique, généré par la base (strictement positif)
 * @param name        nom (obligatoire, unique : l'unicité est contrôlée par le service)
 * @param description description libre (facultative, chaîne vide par défaut)
 */
public record Group(long id, String name, String description) {

    public static final int MAX_NAME_LENGTH = 100;
    public static final int MAX_DESCRIPTION_LENGTH = 500;

    public Group {
        Validation.requirePositive("id", id);
        name = Validation.requireText("name", name, MAX_NAME_LENGTH);
        description = Validation.optionalText("description", description, MAX_DESCRIPTION_LENGTH);
    }
}