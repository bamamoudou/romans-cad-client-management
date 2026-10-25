package com.romanscad.clientmanagement.soap.config;

import com.romanscad.clientmanagement.core.seed.SampleDataSeeder;
import com.romanscad.clientmanagement.core.seed.SeedConfiguration;
import com.romanscad.clientmanagement.core.seed.SeedReport;
import com.romanscad.clientmanagement.core.service.CustomerService;
import com.romanscad.clientmanagement.core.service.GroupService;
import com.romanscad.clientmanagement.core.service.MembershipService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import java.util.Properties;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Initialise la base au démarrage de l'application.
 *
 * L'événement "application initialisée" est traité de façon synchrone pendant le déploiement :
 * WildFly ne répond aux requêtes qu'une fois la génération terminée, donc aucun client ne peut
 * lire une base à moitié remplie. Si la génération échoue, le déploiement échoue (fail fast).
 *
 * Réglable sans recompiler, avec des propriétés système (option -D de la JVM) :
 * - clientmanagement.seed.customers  : nombre de clients (défaut 1 000 000, 0 = base vide)
 * - clientmanagement.seed.groups     : nombre de groupes (défaut 20)
 * - clientmanagement.seed.randomSeed : graine du générateur aléatoire (défaut 42)
 */
@ApplicationScoped
public class DataInitializer {

    static final String CUSTOMERS_PROPERTY = "clientmanagement.seed.customers";
    static final String GROUPS_PROPERTY = "clientmanagement.seed.groups";
    static final String SEED_PROPERTY = "clientmanagement.seed.randomSeed";

    private static final Logger LOGGER = Logger.getLogger(DataInitializer.class.getName());

    // Injection par champ : un bean à portée applicative doit avoir un constructeur sans
    // argument pour que CDI puisse créer son proxy, ce qui exclut l'injection par constructeur.
    @Inject
    private CustomerService customerService;

    @Inject
    private GroupService groupService;

    @Inject
    private MembershipService membershipService;

    void onStartup(@Observes @Initialized(ApplicationScoped.class) Object event) {
        SeedConfiguration configuration = readConfiguration(System.getProperties());
        LOGGER.info(() -> "Generating sample data: " + configuration);

        SeedReport report = new SampleDataSeeder(customerService, groupService, membershipService)
                .seed(configuration);

        LOGGER.info(() -> "Sample data ready: " + report);
    }

    /** Lit la configuration dans des propriétés ; une propriété absente prend sa valeur par défaut. */
    static SeedConfiguration readConfiguration(Properties properties) {
        return new SeedConfiguration(
                read(properties, CUSTOMERS_PROPERTY, SeedConfiguration.DEFAULT_CUSTOMER_COUNT, Integer::valueOf),
                read(properties, GROUPS_PROPERTY, SeedConfiguration.DEFAULT_GROUP_COUNT, Integer::valueOf),
                read(properties, SEED_PROPERTY, SeedConfiguration.DEFAULT_RANDOM_SEED, Long::valueOf));
    }

    private static <T> T read(Properties properties, String key, T defaultValue, Function<String, T> parser) {
        String raw = properties.getProperty(key);
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        try {
            return parser.apply(raw.strip());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid value for system property " + key + ": '" + raw + "'", e);
        }
    }
}