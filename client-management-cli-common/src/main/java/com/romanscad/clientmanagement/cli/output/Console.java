package com.romanscad.clientmanagement.cli.output;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;

/** Sortie standard du programme : le seul endroit qui écrit à l'écran (hors messages d'erreur). */
public final class Console {

    private final PrintStream out;

    public Console(PrintStream out) {
        this.out = out;
    }

    public void println(String line) {
        out.println(line);
    }

    public void printLines(List<String> lines) {
        lines.forEach(out::println);
    }

    /** Affiche des couples nom / valeur, valeurs alignées : {@code first name : Ada}. */
    public void printFields(Map<String, String> fields) {
        int width = fields.keySet().stream().mapToInt(String::length).max().orElse(0);
        fields.forEach((name, value) -> out.println(name + " ".repeat(width - name.length()) + " : " + value));
    }
}