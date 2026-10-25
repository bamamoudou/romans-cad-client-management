package com.romanscad.clientmanagement.core.seed;

import com.romanscad.clientmanagement.core.exception.ValidationException;

/**
 * Paramètres de génération du jeu de données.
 *
 * @param customerCount nombre de clients à créer
 * @param groupCount    nombre de groupes à créer (0 à SampleDataSeeder.MAX_GROUPS)
 * @param randomSeed    graine du générateur aléatoire : même graine = mêmes données, à chaque exécution
 */
public record SeedConfiguration(int customerCount, int groupCount, long randomSeed) {

    public static final int DEFAULT_CUSTOMER_COUNT = 1_000_000;
    public static final int DEFAULT_GROUP_COUNT = 20;
    public static final long DEFAULT_RANDOM_SEED = 42L;

    public SeedConfiguration {
        if (customerCount < 0) {
            throw new ValidationException("customerCount must be >= 0");
        }
        if (groupCount < 0 || groupCount > SampleDataSeeder.MAX_GROUPS) {
            throw new ValidationException("groupCount must be between 0 and " + SampleDataSeeder.MAX_GROUPS);
        }
    }

    /** Jeu de données de l'exercice : 1 million de clients, 20 groupes, graine 42. */
    public static SeedConfiguration defaults() {
        return new SeedConfiguration(DEFAULT_CUSTOMER_COUNT, DEFAULT_GROUP_COUNT, DEFAULT_RANDOM_SEED);
    }
}