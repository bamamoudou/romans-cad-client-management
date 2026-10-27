package com.romanscad.clientmanagement.cli.args;

import java.util.List;
import java.util.Optional;

/**
 * Option globale placée AVANT la commande, par exemple {@code --url http://serveur:8080/...}.
 *
 * Elle est extraite avant que le moteur ne voie la ligne de commande : les commandes n'ont donc
 * pas à la connaître, et les deux clients (SOAP et REST) partagent le même mécanisme.
 *
 * @param value     la valeur de l'option, ou vide si elle n'est pas donnée
 * @param remaining la ligne de commande restante, sans l'option
 */
public record LeadingOption(Optional<String> value, List<String> remaining) {

    public LeadingOption {
        remaining = List.copyOf(remaining);
    }

    /**
     * Cherche l'option en tête de ligne, sous la forme {@code --name valeur} ou {@code --name=valeur}.
     *
     * @param name nom de l'option, sans les tirets
     * @throws UsageException si l'option est donnée sans valeur
     */
    public static LeadingOption extract(List<String> arguments, String name) {
        String flag = "--" + name;
        if (!arguments.isEmpty()) {
            String first = arguments.get(0);
            if (first.equals(flag)) {
                if (arguments.size() < 2) {
                    throw new UsageException("Option " + flag + " requires a value");
                }
                return new LeadingOption(Optional.of(arguments.get(1)), arguments.subList(2, arguments.size()));
            }
            if (first.startsWith(flag + "=")) {
                return new LeadingOption(Optional.of(first.substring(flag.length() + 1)),
                        arguments.subList(1, arguments.size()));
            }
        }
        return new LeadingOption(Optional.empty(), arguments);
    }
}