package com.romanscad.clientmanagement.cli.command;

import com.romanscad.clientmanagement.cli.args.Arguments;
import com.romanscad.clientmanagement.cli.output.Console;
import java.util.Set;

/**
 * Description complète d'une commande : son nom, son aide, les options qu'elle accepte et le
 * code à exécuter. L'aide affichée à l'utilisateur est construite à partir de ces descriptions,
 * donc elle ne peut pas se désynchroniser des commandes.
 *
 * @param name         mots de la commande ("customer create", "overview")
 * @param usage        syntaxe complète, pour l'aide
 * @param description  ce que fait la commande, en une phrase
 * @param valueOptions options acceptées avec une valeur (sans les tirets)
 * @param flagOptions  options acceptées sans valeur (sans les tirets)
 * @param handler      le code de la commande
 */
public record CommandSpec(String name, String usage, String description,
                          Set<String> valueOptions, Set<String> flagOptions, Handler handler) {

    @FunctionalInterface
    public interface Handler {
        void run(Arguments arguments, Console console);
    }

    /** Commande sans option. */
    public static CommandSpec of(String name, String usage, String description, Handler handler) {
        return new CommandSpec(name, usage, description, Set.of(), Set.of(), handler);
    }
}