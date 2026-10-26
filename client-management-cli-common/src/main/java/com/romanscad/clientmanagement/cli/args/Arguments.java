package com.romanscad.clientmanagement.cli.args;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Arguments d'une commande, séparés en trois familles :
 * - les arguments positionnels : {@code 42}, {@code VIP}
 * - les options à valeur : {@code --limit 10} ou {@code --limit=10}
 * - les options sans valeur (drapeaux) : {@code --all}
 *
 * L'analyse est stricte : une option inconnue, un doublon ou une valeur manquante sont refusés
 * avec un message précis, plutôt qu'ignorés en silence.
 *
 * Écrit à la main, sans bibliothèque : le besoin est simple, et cela évite une dépendance.
 */
public final class Arguments {

    private final List<String> positionals;
    private final Map<String, String> options;
    private final Set<String> flags;

    private Arguments(List<String> positionals, Map<String, String> options, Set<String> flags) {
        this.positionals = List.copyOf(positionals);
        this.options = Map.copyOf(options);
        this.flags = Set.copyOf(flags);
    }

    /**
     * Analyse une liste de mots.
     *
     * @param tokens       les mots de la ligne de commande (après le nom de la commande)
     * @param valueOptions noms des options qui attendent une valeur (sans les tirets)
     * @param flagOptions  noms des options sans valeur (sans les tirets)
     * @throws UsageException si une option est inconnue, répétée, ou sans valeur
     */
    public static Arguments parse(List<String> tokens, Set<String> valueOptions, Set<String> flagOptions) {
        List<String> positionals = new ArrayList<>();
        Map<String, String> options = new HashMap<>();
        Set<String> flags = new HashSet<>();

        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);

            if (token.equals("--")) { // fin des options : tout le reste est positionnel
                positionals.addAll(tokens.subList(i + 1, tokens.size()));
                break;
            }
            if (!token.startsWith("--")) {
                positionals.add(token);
                continue;
            }

            String body = token.substring(2);
            int equals = body.indexOf('=');
            String name = (equals >= 0) ? body.substring(0, equals) : body;

            if (flagOptions.contains(name)) {
                if (equals >= 0) {
                    throw new UsageException("Option --" + name + " does not take a value");
                }
                flags.add(name);
            } else if (valueOptions.contains(name)) {
                String value;
                if (equals >= 0) {
                    value = body.substring(equals + 1);
                } else if (i + 1 < tokens.size()) {
                    value = tokens.get(++i);
                } else {
                    throw new UsageException("Option --" + name + " requires a value");
                }
                if (options.putIfAbsent(name, value) != null) {
                    throw new UsageException("Option --" + name + " given more than once");
                }
            } else {
                throw new UsageException("Unknown option --" + name);
            }
        }
        return new Arguments(positionals, options, flags);
    }

    // ------------------------------------------------------------ arguments positionnels

    /** Argument positionnel obligatoire. @param name nom affiché dans le message d'erreur */
    public String positional(int index, String name) {
        if (index >= positionals.size()) {
            throw new UsageException("Missing argument <" + name + ">");
        }
        return positionals.get(index);
    }

    /** Argument positionnel obligatoire, lu comme un nombre entier. */
    public long positionalLong(int index, String name) {
        return parseLong(positional(index, name), "<" + name + ">");
    }

    /** Tous les arguments positionnels à partir de index, lus comme des nombres. */
    public List<Long> positionalLongsFrom(int index, String name) {
        List<Long> values = new ArrayList<>();
        for (int i = index; i < positionals.size(); i++) {
            values.add(parseLong(positionals.get(i), "<" + name + ">"));
        }
        return values;
    }

    /** Refuse les arguments positionnels en trop (au-delà de count). */
    public void requireNoMorePositionalsThan(int count) {
        if (positionals.size() > count) {
            throw new UsageException("Unexpected argument '" + positionals.get(count) + "'");
        }
    }

    // ------------------------------------------------------------ options

    public Optional<String> option(String name) {
        return Optional.ofNullable(options.get(name));
    }

    public String requiredOption(String name) {
        return option(name).orElseThrow(() -> new UsageException("Missing option --" + name));
    }

    public Optional<Long> optionLong(String name) {
        return option(name).map(value -> parseLong(value, "--" + name));
    }

    public Optional<Integer> optionInt(String name) {
        return optionLong(name).map(value -> {
            if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
                throw new UsageException("--" + name + " is out of range: " + value);
            }
            return value.intValue();
        });
    }

    public boolean flag(String name) {
        return flags.contains(name);
    }

    // ------------------------------------------------------------ outils

    private static long parseLong(String raw, String label) {
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            throw new UsageException(label + " must be a whole number, got '" + raw + "'");
        }
    }
}