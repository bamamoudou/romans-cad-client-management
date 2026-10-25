package com.romanscad.clientmanagement.soap.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.romanscad.clientmanagement.core.exception.ValidationException;
import com.romanscad.clientmanagement.core.seed.SeedConfiguration;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class DataInitializerTest {

    @Test
    void usesDefaultsWhenNothingIsConfigured() {
        SeedConfiguration configuration = DataInitializer.readConfiguration(new Properties());

        assertEquals(SeedConfiguration.defaults(), configuration);
    }

    @Test
    void readsOverridesFromProperties() {
        Properties properties = new Properties();
        properties.setProperty(DataInitializer.CUSTOMERS_PROPERTY, " 5000 ");
        properties.setProperty(DataInitializer.GROUPS_PROPERTY, "3");
        properties.setProperty(DataInitializer.SEED_PROPERTY, "7");

        assertEquals(new SeedConfiguration(5000, 3, 7L), DataInitializer.readConfiguration(properties));
    }

    @Test
    void blankPropertyMeansDefault() {
        Properties properties = new Properties();
        properties.setProperty(DataInitializer.CUSTOMERS_PROPERTY, "  ");

        assertEquals(SeedConfiguration.DEFAULT_CUSTOMER_COUNT, DataInitializer.readConfiguration(properties).customerCount());
    }

    @Test
    void notANumberGivesAClearError() {
        Properties properties = new Properties();
        properties.setProperty(DataInitializer.CUSTOMERS_PROPERTY, "many");

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> DataInitializer.readConfiguration(properties));
        assertEquals("Invalid value for system property clientmanagement.seed.customers: 'many'", error.getMessage());
    }

    @Test
    void outOfRangeValueIsRejectedByTheConfiguration() {
        Properties properties = new Properties();
        properties.setProperty(DataInitializer.GROUPS_PROPERTY, "99");

        assertThrows(ValidationException.class, () -> DataInitializer.readConfiguration(properties));
    }
}