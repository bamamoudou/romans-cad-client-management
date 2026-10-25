package com.romanscad.clientmanagement.core.seed;

import java.time.Duration;

/** Bilan d'une génération, pour les journaux de démarrage. */
public record SeedReport(long customers, long groups, long memberships, Duration duration) {

    @Override
    public String toString() {
        return "%,d customers, %,d groups, %,d memberships in %d ms"
                .formatted(customers, groups, memberships, duration.toMillis());
    }
}