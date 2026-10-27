package com.romanscad.clientmanagement.cli.rest;

import com.romanscad.clientmanagement.cli.ServiceUnavailableException;
import com.romanscad.clientmanagement.core.pagination.PageRequest;
import jakarta.json.JsonObject;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Transport HTTP vers la passerelle REST, basé sur le HttpClient du JDK (aucune bibliothèque externe).
 *
 * Cette classe ne connaît ni les ressources ni le JSON métier : elle envoie une requête et rend un
 * HttpResult. Toute panne de transport (serveur arrêté, délai dépassé, hôte inconnu) devient une
 * ServiceUnavailableException au message lisible.
 */
final class RestClient {

    static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(60);

    private final String baseUrl;
    private final HttpClient http;

    /** @throws IllegalArgumentException si l'adresse n'est pas une adresse http(s) valide */
    RestClient(String baseUrl) {
        this.baseUrl = validate(baseUrl);
        this.http = HttpClient.newBuilder()
                // HTTP/1.1 forcé : la négociation automatique vers HTTP/2 en clair pose des problèmes
                // avec certains serveurs pour les requêtes avec corps.
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(CONNECT_TIMEOUT)
                .build();
    }

    HttpResult get(String path) {
        return send(request(path).GET().build());
    }

    HttpResult delete(String path) {
        return send(request(path).DELETE().build());
    }

    HttpResult post(String path, JsonObject body) {
        return send(request(path)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                .build());
    }

    /** Paramètres de pagination de l'API : ?after=...&limit=... */
    static String pageQuery(PageRequest pageRequest) {
        return "?after=" + pageRequest.afterId() + "&limit=" + pageRequest.limit();
    }

    private HttpRequest.Builder request(String path) {
        return HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(REQUEST_TIMEOUT)
                .header("Accept", "application/json");
    }

    private HttpResult send(HttpRequest request) {
        try {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return new HttpResult(response.statusCode(), response.body());
        } catch (IOException e) {
            throw new ServiceUnavailableException(
                    "Cannot reach the REST gateway at " + baseUrl + ": " + describe(e), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // on rétablit le signal d'interruption
            throw new ServiceUnavailableException("Interrupted while waiting for the REST gateway", e);
        }
    }

    /** Décrit une panne réseau en termes compréhensibles plutôt que par le nom d'une exception technique. */
    static String describe(Throwable error) {
        Throwable root = error;
        for (Throwable current = error; current != null; current = current.getCause()) {
            if (current instanceof HttpTimeoutException) {
                return "timed out";
            }
            if (current instanceof ConnectException) {
                return "connection refused";
            }
            if (current instanceof UnknownHostException) {
                return "unknown host " + current.getMessage();
            }
            root = current;
            if (current.getCause() == current) {
                break;
            }
        }
        return root.getMessage() != null ? root.getMessage() : root.getClass().getSimpleName();
    }

    private static String validate(String url) {
        String trimmed = url.strip();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        URI uri;
        try {
            uri = URI.create(trimmed);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid --url '" + url + "': expected http://host[:port]/path", e);
        }
        String scheme = uri.getScheme();
        boolean httpScheme = "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
        if (!httpScheme || uri.getHost() == null) {
            throw new IllegalArgumentException("Invalid --url '" + url + "': expected http://host[:port]/path");
        }
        return trimmed;
    }
}