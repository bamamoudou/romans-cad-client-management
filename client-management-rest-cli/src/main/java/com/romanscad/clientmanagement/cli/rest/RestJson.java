package com.romanscad.clientmanagement.cli.rest;

import com.romanscad.clientmanagement.cli.ServiceUnavailableException;
import com.romanscad.clientmanagement.core.domain.Customer;
import com.romanscad.clientmanagement.core.domain.CustomerWithGroups;
import com.romanscad.clientmanagement.core.domain.Group;
import com.romanscad.clientmanagement.core.pagination.Page;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonException;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import java.io.StringReader;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Tout le JSON du client : corps des requêtes et conversion des réponses vers les types du domaine.
 *
 * Une réponse dont la forme n'est pas celle du contrat (champ absent, mauvais type) n'est pas un bug
 * à cacher : elle devient une ServiceUnavailableException « format inattendu ».
 */
final class RestJson {

    private RestJson() {
        // Classe utilitaire : non instanciable
    }

    // ---------------------------------------------------------------- corps des requêtes

    static JsonObject customerBody(String firstName, String lastName, String email) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        put(builder, "firstName", firstName);
        put(builder, "lastName", lastName);
        put(builder, "email", email);
        return builder.build();
    }

    static JsonObject groupBody(String name, String description) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        put(builder, "name", name);
        put(builder, "description", description);
        return builder.build();
    }

    static JsonObject idsBody(Collection<Long> customerIds) {
        JsonArrayBuilder ids = Json.createArrayBuilder();
        for (Long id : customerIds) {
            ids.add(id.longValue());
        }
        return Json.createObjectBuilder().add("customerIds", ids).build();
    }

    /** Une valeur absente n'est pas envoyée : le serveur la traitera comme manquante. */
    private static void put(JsonObjectBuilder builder, String key, String value) {
        if (value != null) {
            builder.add(key, value);
        }
    }

    // ---------------------------------------------------------------- lecture des réponses

    /** Analyse un corps JSON de type objet ; null s'il est vide, illisible ou d'un autre type. */
    static JsonObject tryParse(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }
        try (JsonReader reader = Json.createReader(new StringReader(body))) {
            return reader.readObject();
        } catch (JsonException e) {
            return null;
        }
    }

    /** Analyse la réponse puis la convertit ; toute anomalie de forme devient « format inattendu ». */
    static <T> T read(String body, Function<JsonObject, T> mapper) {
        JsonObject root = tryParse(body);
        if (root == null) {
            throw malformed(null);
        }
        try {
            return mapper.apply(root);
        } catch (JsonException | ClassCastException e) {
            throw malformed(e);
        }
    }

    static Customer toCustomer(JsonObject json) {
        return new Customer(number(json, "id"), text(json, "firstName"), text(json, "lastName"), text(json, "email"));
    }

    static Group toGroup(JsonObject json) {
        return new Group(number(json, "id"), text(json, "name"), json.getString("description", ""));
    }

    static CustomerWithGroups toCustomerWithGroups(JsonObject json) {
        Customer customer = toCustomer(object(json, "customer"));
        List<Group> groups = array(json, "groups").stream().map(value -> toGroup(value.asJsonObject())).toList();
        return new CustomerWithGroups(customer, groups);
    }

    /** Une page : les éléments et le curseur (absent sur la dernière page). */
    static <T> Page<T> toPage(JsonObject json, Function<JsonObject, T> itemMapper) {
        List<T> items = array(json, "items").stream().map(value -> itemMapper.apply(value.asJsonObject())).toList();
        Long nextCursor = json.containsKey("nextCursor") && !json.isNull("nextCursor")
                ? Long.valueOf(number(json, "nextCursor"))
                : null;
        return new Page<>(items, nextCursor);
    }

    /** Résultat d'une association ou d'une dissociation : le nombre d'appartenances modifiées. */
    static int toCount(JsonObject json) {
        return Math.toIntExact(number(json, "count"));
    }

    // ---------------------------------------------------------------- accès sûrs aux champs

    private static long number(JsonObject json, String key) {
        JsonNumber value = json.getJsonNumber(key);
        if (value == null) {
            throw new JsonException("Missing number '" + key + "'");
        }
        return value.longValue();
    }

    private static String text(JsonObject json, String key) {
        String value = json.getString(key, null);
        if (value == null) {
            throw new JsonException("Missing text '" + key + "'");
        }
        return value;
    }

    private static JsonObject object(JsonObject json, String key) {
        JsonObject value = json.getJsonObject(key);
        if (value == null) {
            throw new JsonException("Missing object '" + key + "'");
        }
        return value;
    }

    private static JsonArray array(JsonObject json, String key) {
        JsonArray value = json.getJsonArray(key);
        if (value == null) {
            throw new JsonException("Missing array '" + key + "'");
        }
        return value;
    }

    private static ServiceUnavailableException malformed(Throwable cause) {
        return new ServiceUnavailableException("The server sent a response in an unexpected format", cause);
    }
}