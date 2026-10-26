package com.romanscad.clientmanagement.cli.args;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ArgumentsTest {

    private static final Set<String> VALUES = Set.of("limit", "after", "description");
    private static final Set<String> FLAGS = Set.of("all");

    private static Arguments parse(String... tokens) {
        return Arguments.parse(List.of(tokens), VALUES, FLAGS);
    }

    private static String errorOf(Runnable action) {
        return assertThrows(UsageException.class, action::run).getMessage();
    }

    @Test
    void separatesPositionalsOptionsAndFlags() {
        Arguments args = parse("7", "--limit", "10", "--all", "VIP");

        assertEquals("7", args.positional(0, "id"));
        assertEquals("VIP", args.positional(1, "name"));
        assertEquals(Optional.of(10), args.optionInt("limit"));
        assertTrue(args.flag("all"));
    }

    @Test
    void acceptsTheEqualsForm() {
        Arguments args = parse("--limit=25", "--description=a b c");

        assertEquals(Optional.of(25L), args.optionLong("limit"));
        assertEquals(Optional.of("a b c"), args.option("description"));
    }

    @Test
    void absentOptionsAreEmptyAndFlagsAreFalse() {
        Arguments args = parse();

        assertEquals(Optional.empty(), args.option("limit"));
        assertEquals(Optional.empty(), args.optionLong("after"));
        assertFalse(args.flag("all"));
    }

    @Test
    void negativeNumbersAreValuesNotOptions() {
        assertEquals(Optional.of(-1L), parse("--after", "-1").optionLong("after"));
    }

    @Test
    void doubleDashEndsOptionParsing() {
        Arguments args = parse("--", "--limit", "5");

        assertEquals("--limit", args.positional(0, "a"));
        assertEquals("5", args.positional(1, "b"));
        assertEquals(Optional.empty(), args.option("limit"));
    }

    @Test
    void rejectsUnknownOption() {
        assertEquals("Unknown option --colour", errorOf(() -> parse("--colour", "red")));
    }

    @Test
    void rejectsOptionWithoutValue() {
        assertEquals("Option --limit requires a value", errorOf(() -> parse("--limit")));
    }

    @Test
    void rejectsValueGivenToAFlag() {
        assertEquals("Option --all does not take a value", errorOf(() -> parse("--all=yes")));
    }

    @Test
    void rejectsRepeatedOption() {
        assertEquals("Option --limit given more than once", errorOf(() -> parse("--limit", "1", "--limit", "2")));
    }

    @Test
    void missingPositionalNamesTheArgument() {
        assertEquals("Missing argument <customerId>", errorOf(() -> parse().positional(0, "customerId")));
    }

    @Test
    void missingRequiredOptionIsReported() {
        assertEquals("Missing option --description", errorOf(() -> parse().requiredOption("description")));
    }

    @Test
    void notANumberGivesAClearMessage() {
        assertEquals("<id> must be a whole number, got 'abc'", errorOf(() -> parse("abc").positionalLong(0, "id")));
        assertEquals("--limit must be a whole number, got 'ten'", errorOf(() -> parse("--limit", "ten").optionLong("limit")));
    }

    @Test
    void integerOptionOutOfRangeIsRejected() {
        assertEquals("--limit is out of range: 99999999999", errorOf(() -> parse("--limit", "99999999999").optionInt("limit")));
    }

    @Test
    void readsAListOfNumbersFromAGivenPosition() {
        Arguments args = parse("5", "10", "11", "12");

        assertEquals(List.of(10L, 11L, 12L), args.positionalLongsFrom(1, "customerId"));
        assertEquals(List.of(), parse("5").positionalLongsFrom(1, "customerId"));
    }

    @Test
    void rejectsExtraPositionals() {
        Arguments args = parse("1", "2");

        args.requireNoMorePositionalsThan(2); // pas d'erreur
        assertEquals("Unexpected argument '2'", errorOf(() -> args.requireNoMorePositionalsThan(1)));
    }
}