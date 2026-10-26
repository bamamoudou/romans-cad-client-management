package com.romanscad.clientmanagement.cli.command;

import com.romanscad.clientmanagement.cli.args.Arguments;
import com.romanscad.clientmanagement.cli.output.Console;
import com.romanscad.clientmanagement.cli.output.TableFormatter;
import com.romanscad.clientmanagement.core.pagination.Page;
import com.romanscad.clientmanagement.core.pagination.PageRequest;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Logique commune à toutes les commandes de liste : options --after, --limit et --all.
 *
 * - sans --all : affiche UNE page, et indique la valeur de --after pour obtenir la suivante
 * - avec --all : parcourt toutes les pages et les affiche au fil de l'eau, sans jamais garder
 *   plus d'une page en mémoire (même avec 1 million de clients)
 */
final class PagedListing {

    static final Set<String> VALUE_OPTIONS = Set.of("after", "limit");
    static final Set<String> FLAG_OPTIONS = Set.of("all");
    static final String USAGE = "[--after <id>] [--limit <n>] [--all]";

    private PagedListing() {
        // Classe utilitaire : non instanciable
    }

    /**
     * @param noun  nom pluriel de ce qu'on liste ("customers"), pour les messages
     * @param fetch obtient une page (appel du service)
     * @param toRow transforme un élément en ligne de tableau
     */
    static <T> void run(Arguments arguments, Console console, TableFormatter table, String noun,
                        Function<PageRequest, Page<T>> fetch, Function<T, String[]> toRow) {
        boolean all = arguments.flag("all");
        long after = arguments.optionLong("after").orElse(0L);
        // Pour tout parcourir, on prend les plus grosses pages : moins d'appels.
        int limit = arguments.optionInt("limit").orElse(all ? PageRequest.MAX_LIMIT : PageRequest.DEFAULT_LIMIT);
        PageRequest request = new PageRequest(after, limit);

        long total = 0;
        boolean firstPrinted = false;
        Page<T> page;
        do {
            page = fetch.apply(request);
            if (!page.items().isEmpty()) {
                List<String[]> rows = page.items().stream().map(toRow).toList();
                console.printLines(table.format(rows, !firstPrinted));
                firstPrinted = true;
                total += rows.size();
            }
            if (all && page.hasNext()) {
                request = new PageRequest(page.nextCursor(), limit);
            }
        } while (all && page.hasNext());

        if (total == 0) {
            console.println("No " + noun + ".");
        } else if (all) {
            console.println(total + " " + noun + " listed.");
        } else if (page.hasNext()) {
            console.println(total + " " + noun + " shown. More available: add --after " + page.nextCursor());
        } else {
            console.println(total + " " + noun + " shown.");
        }
    }
}