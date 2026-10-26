package com.romanscad.clientmanagement.cli.output;

import java.util.ArrayList;
import java.util.List;

/**
 * Met des lignes en colonnes alignées.
 *
 * Utilisable page après page (liste complète de 1 million de clients) : on ne garde jamais
 * toutes les lignes en mémoire. La largeur d'une colonne ne fait que grandir au fil des pages ;
 * si une page tardive contient une valeur plus large que toutes les précédentes, cette ligne
 * déborde un peu, c'est le prix de l'affichage au fil de l'eau.
 */
public final class TableFormatter {

    private static final String GAP = "  ";

    private final String[] headers;
    private final int[] widths;

    public TableFormatter(String... headers) {
        this.headers = headers.clone();
        this.widths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            widths[i] = headers[i].length();
        }
    }

    /**
     * @param rows          les lignes à afficher (chacune a autant de cellules que de colonnes)
     * @param includeHeader vrai pour la première page : ajoute l'en-tête et son soulignement
     */
    public List<String> format(List<String[]> rows, boolean includeHeader) {
        List<String[]> cleaned = new ArrayList<>(rows.size());
        for (String[] row : rows) {
            if (row.length != headers.length) {
                throw new IllegalArgumentException("Expected " + headers.length + " cells but got " + row.length);
            }
            String[] cells = new String[row.length];
            for (int i = 0; i < row.length; i++) {
                cells[i] = clean(row[i]);
                widths[i] = Math.max(widths[i], cells[i].length());
            }
            cleaned.add(cells);
        }

        List<String> lines = new ArrayList<>(cleaned.size() + 2);
        if (includeHeader) {
            lines.add(line(headers));
            lines.add(line(underline()));
        }
        for (String[] cells : cleaned) {
            lines.add(line(cells));
        }
        return lines;
    }

    private String[] underline() {
        String[] dashes = new String[headers.length];
        for (int i = 0; i < dashes.length; i++) {
            dashes[i] = "-".repeat(widths[i]);
        }
        return dashes;
    }

    /**
     * La dernière colonne n'est pas complétée d'espaces, et une dernière cellule vide ne laisse
     * pas d'espaces en fin de ligne.
     */
    private String line(String[] cells) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < cells.length; i++) {
            if (i == cells.length - 1) {
                line.append(cells[i]);
            } else {
                line.append(cells[i]).append(" ".repeat(widths[i] - cells[i].length())).append(GAP);
            }
        }
        return line.toString().stripTrailing();
    }

    /** Un retour à la ligne dans une cellule casserait le tableau : on le remplace par une espace. */
    private static String clean(String cell) {
        if (cell == null) {
            return "";
        }
        return cell.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ');
    }
}