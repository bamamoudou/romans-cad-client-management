package com.romanscad.clientmanagement.cli.output;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class TableFormatterTest {

    @Test
    void alignsColumnsAndDoesNotPadTheLastOne() {
        TableFormatter table = new TableFormatter("ID", "NAME");

        List<String> lines = table.format(List.of(new String[] {"1", "Ada"}, new String[] {"100", "Grace"}), true);

        assertEquals(List.of(
                "ID   NAME",
                "---  -----",
                "1    Ada",
                "100  Grace"), lines);
    }

    @Test
    void headerIsOnlyPrintedWhenRequested() {
        TableFormatter table = new TableFormatter("ID", "NAME");

        List<String> lines = table.format(List.<String[]>of(new String[] {"1", "Ada"}), false);

        assertEquals(List.of("1   Ada"), lines);
    }

    @Test
    void columnsOnlyGrowAcrossPages() {
        TableFormatter table = new TableFormatter("ID", "NAME");
        table.format(List.<String[]>of(new String[] {"1000", "Ada"}), true);

        List<String> second = table.format(List.<String[]>of(new String[] {"2", "Bob"}), false);

        assertEquals(List.of("2     Bob"), second); // la colonne ID garde la largeur de la page précédente
    }

    @Test
    void nullCellsAreEmptyAndLineBreaksAreFlattened() {
        TableFormatter table = new TableFormatter("ID", "TEXT");

        List<String> lines = table.format(List.of(new String[] {"1", null}, new String[] {"2", "a\nb\tc"}), false);

        assertEquals(List.of("1", "2   a b c"), lines);
    }

    @Test
    void rejectsARowOfTheWrongSize() {
        TableFormatter table = new TableFormatter("ID", "NAME");

        assertThrows(IllegalArgumentException.class, () -> table.format(List.<String[]>of(new String[] {"1"}), true));
    }
}