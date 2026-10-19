package org.faketri.net;

import org.faketri.net.io.dto.request.Args;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ArgsParserTest {

    private static final Set<String> NONE = Set.of();
    private static final Set<String> FILTER = Set.of("filter");
    private static final Set<String> TAIL = Set.of("tail");
    private static final Set<String> SORT = Set.of("sort");
    private static final Set<String> PROFILE = Set.of("profile");
    private static final Set<String> FILTER_TAIL = Set.of("filter", "tail");

    record ArgsCase(String name,
                    Set<String> valueOptions,
                    List<String> raw,
                    List<String> positional,
                    Set<String> flags,
                    Map<String, List<String>> options) {
        @Override
        public String toString() {
            return name;
        }
    }

    record InvalidCase(String name, Set<String> valueOptions, List<String> raw) {
        @Override
        public String toString() {
            return name;
        }
    }

    private static ArgsCase c(String name, Set<String> valueOptions, List<String> raw,
                              List<String> positional, Set<String> flags,
                              Map<String, List<String>> options) {
        return new ArgsCase(name, valueOptions, raw, positional, flags, options);
    }

    static Stream<ArgsCase> valid() {
        return Stream.of(
                // --- пустой ввод ---
                c("empty", NONE, List.of(), List.of(), Set.of(), Map.of()),

                // --- позиционные ---
                c("one positional", NONE, List.of("server"), List.of("server"), Set.of(), Map.of()),
                c("two positional", NONE, List.of("server", "50"), List.of("server", "50"), Set.of(), Map.of()),
                c("pid as positional", NONE, List.of("4821"), List.of("4821"), Set.of(), Map.of()),
                c("uuid as positional", NONE, List.of("3f2c1a9e-7b4d-4c1a-9e2f-1a2b3c4d5e6f"),
                        List.of("3f2c1a9e-7b4d-4c1a-9e2f-1a2b3c4d5e6f"), Set.of(), Map.of()),
                c("negative number is not a flag", NONE, List.of("-9"), List.of("-9"), Set.of(), Map.of()),
                c("single dash is positional", NONE, List.of("-"), List.of("-"), Set.of(), Map.of()),
                c("empty token is positional", NONE, List.of(""), List.of(""), Set.of(), Map.of()),

                // --- короткие флаги ---
                c("short flag", NONE, List.of("-a"), List.of(), Set.of("a"), Map.of()),
                c("combined short flags", NONE, List.of("-aq"), List.of(), Set.of("a", "q"), Map.of()),
                c("two short flags", NONE, List.of("-a", "-q"), List.of(), Set.of("a", "q"), Map.of()),

                // --- длинные флаги ---
                c("long flag", NONE, List.of("--all"), List.of(), Set.of("all"), Map.of()),
                c("unknown long flag", NONE, List.of("--foo"), List.of(), Set.of("foo"), Map.of()),
                c("flag then positional", NONE, List.of("--all", "server"), List.of("server"), Set.of("all"), Map.of()),
                c("positional then flag", NONE, List.of("server", "--all"), List.of("server"), Set.of("all"), Map.of()),
                c("short flag and positional", NONE, List.of("-a", "server"), List.of("server"), Set.of("a"), Map.of()),

                // --- опция НЕ объявлена командой: это обычный флаг, а её "значение" позиционное ---
                c("undeclared option is a flag", NONE, List.of("--filter", "name=app"),
                        List.of("name=app"), Set.of("filter"), Map.of()),
                c("undeclared tail swallows nothing", NONE, List.of("--tail", "50", "server"),
                        List.of("50", "server"), Set.of("tail"), Map.of()),

                // --- опция объявлена командой: забирает значение ---
                c("filter separate value", FILTER, List.of("--filter", "name=app"),
                        List.of(), Set.of(), Map.of("filter", List.of("name=app"))),
                c("filter with equals", FILTER, List.of("--filter=name=app"),
                        List.of(), Set.of(), Map.of("filter", List.of("name=app"))),
                c("filter repeated", FILTER, List.of("--filter", "name=a", "--filter", "profile=W"),
                        List.of(), Set.of(), Map.of("filter", List.of("name=a", "profile=W"))),
                c("filter mixed styles", FILTER, List.of("--filter=name=a", "--filter", "status=failed"),
                        List.of(), Set.of(), Map.of("filter", List.of("name=a", "status=failed"))),
                c("filter empty value after equals", FILTER, List.of("--filter="),
                        List.of(), Set.of(), Map.of("filter", List.of(""))),
                c("filter value with spaces", FILTER, List.of("--filter", "name=my app"),
                        List.of(), Set.of(), Map.of("filter", List.of("name=my app"))),
                c("option value that looks like a flag", FILTER, List.of("--filter", "-a"),
                        List.of(), Set.of(), Map.of("filter", List.of("-a"))),
                c("flag after option value", FILTER, List.of("--filter", "name=x", "-a"),
                        List.of(), Set.of("a"), Map.of("filter", List.of("name=x"))),

                c("tail before positional", TAIL, List.of("--tail", "50", "server"),
                        List.of("server"), Set.of(), Map.of("tail", List.of("50"))),
                c("tail after positional", TAIL, List.of("server", "--tail", "50"),
                        List.of("server"), Set.of(), Map.of("tail", List.of("50"))),
                c("tail with equals", TAIL, List.of("--tail=50", "server"),
                        List.of("server"), Set.of(), Map.of("tail", List.of("50"))),

                c("sort with dash value", SORT, List.of("--sort", "-pid"),
                        List.of(), Set.of(), Map.of("sort", List.of("-pid"))),
                c("sort with dash value and equals", SORT, List.of("--sort=-pid"),
                        List.of(), Set.of(), Map.of("sort", List.of("-pid"))),

                c("profile option", PROFILE, List.of("--profile", "WORKERS"),
                        List.of(), Set.of(), Map.of("profile", List.of("WORKERS"))),

                c("two different options", FILTER_TAIL, List.of("--tail", "5", "--filter", "name=a", "server"),
                        List.of("server"), Set.of(),
                        Map.of("tail", List.of("5"), "filter", List.of("name=a"))),

                // --- комбинации, которые реально придут от `jsup ps` ---
                c("ps -a", FILTER, List.of("-a"), List.of(), Set.of("a"), Map.of()),
                c("ps -a --filter", FILTER, List.of("-a", "--filter", "name=myName"),
                        List.of(), Set.of("a"), Map.of("filter", List.of("name=myName"))),
                c("ps full", FILTER, List.of("-a", "--all", "--filter", "name=a", "--filter=profile=W", "extra"),
                        List.of("extra"), Set.of("a", "all"),
                        Map.of("filter", List.of("name=a", "profile=W")))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("valid")
    void parses(ArgsCase c) {
        Args a = new Args(c.raw(), c.valueOptions());

        assertEquals(c.positional().size(), a.positionalCount());
        for (int i = 0; i < c.positional().size(); i++) {
            assertEquals(c.positional().get(i), a.positional(i), "positional #" + i);
        }

        assertEquals(c.flags(), a.flagNames());

        c.options().forEach((k, v) -> assertEquals(v, a.values(k), "option " + k));
        for (String declared : c.valueOptions()) {
            if (!c.options().containsKey(declared)) {
                assertTrue(a.values(declared).isEmpty(), "unexpected values for " + declared);
            }
        }
    }

    static Stream<InvalidCase> invalid() {
        return Stream.of(
                new InvalidCase("option without value", FILTER, List.of("--filter")),
                new InvalidCase("option at the very end", FILTER, List.of("server", "--filter")),
                new InvalidCase("tail without value", TAIL, List.of("--tail")),
                new InvalidCase("profile without value", PROFILE, List.of("--profile")),
                new InvalidCase("second option has no value", FILTER_TAIL, List.of("--tail", "5", "--filter"))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalid")
    void rejects(InvalidCase c) {
        var e = assertThrows(IllegalArgumentException.class, () -> new Args(c.raw(), c.valueOptions()));
        assertTrue(e.getMessage().contains("requires a value"), e.getMessage());
    }

    @Test
    void optionReturnsLastValue() {
        var a = new Args(List.of("--filter", "a", "--filter", "b"), FILTER);

        assertEquals(Optional.of("b"), a.option("filter"));
        assertEquals(Optional.empty(), a.option("tail"));
    }

    @Test
    void valuesOfUnknownKeyIsEmpty() {
        var a = new Args(List.of("server"), FILTER);

        assertTrue(a.values("filter").isEmpty());
        assertTrue(a.values("nope").isEmpty());
    }

    @Test
    void flagMatchesAnyOfGivenNames() {
        var a = new Args(List.of("--all"), NONE);

        assertTrue(a.flag("a", "all"));
        assertFalse(a.flag("a"));
        assertFalse(a.flag());
    }

    @Test
    void positionalOutOfRangeNamesTheMissingArgument() {
        var a = new Args(List.of("x"), NONE);

        var e = assertThrows(IllegalArgumentException.class, () -> a.positional(1));
        assertTrue(e.getMessage().contains("#2"), e.getMessage());
    }

    @Test
    void positionalOrFallsBackToDefault() {
        var a = new Args(List.of("x"), NONE);

        assertEquals("x", a.positionalOr(0, "def"));
        assertEquals("def", a.positionalOr(1, "def"));
    }

    @Test
    void positionalWithParserReturnsParsedValue() {
        var a = new Args(List.of("42"), NONE);

        assertEquals(42, (int) a.positional(0, Integer::parseInt));
    }

    @Test
    void positionalWithParserWrapsFailureWithArgument() {
        var a = new Args(List.of("abc"), NONE);

        var e = assertThrows(IllegalArgumentException.class, () -> a.positional(0, Integer::parseInt));
        assertTrue(e.getMessage().contains("Invalid argument 'abc'"), e.getMessage());
    }
}