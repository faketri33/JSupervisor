package org.faketri.net;

import org.faketri.net.io.dto.request.Args;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ArgsParserTest {

    static final List<ArgsCase> VALID = List.of(
            // --- пустые ---
            c("empty", List.of(), List.of(), Set.of(), Map.of()),

            // --- позиционные ---
            c("one positional", List.of("server"), List.of("server"), Set.of(), Map.of()),
            c("two positional", List.of("server", "50"), List.of("server", "50"), Set.of(), Map.of()),
            c("pid as positional", List.of("4821"), List.of("4821"), Set.of(), Map.of()),
            c("uuid as positional", List.of("3f2c1a9e-7b4d-4c1a-9e2f-1a2b3c4d5e6f"),
                    List.of("3f2c1a9e-7b4d-4c1a-9e2f-1a2b3c4d5e6f"), Set.of(), Map.of()),
            c("negative number", List.of("-9"), List.of("-9"), Set.of(), Map.of()),
            c("single dash", List.of("-"), List.of("-"), Set.of(), Map.of()),
            c("empty string token", List.of(""), List.of(""), Set.of(), Map.of()),

            // --- короткие флаги ---
            c("short flag", List.of("-a"), List.of(), Set.of("a"), Map.of()),
            c("combined short flags", List.of("-aq"), List.of(), Set.of("a", "q"), Map.of()),
            c("two short flags", List.of("-a", "-q"), List.of(), Set.of("a", "q"), Map.of()),

            // --- длинные флаги ---
            c("long flag", List.of("--all"), List.of(), Set.of("all"), Map.of()),
            c("unknown long flag", List.of("--foo"), List.of(), Set.of("foo"), Map.of()),
            c("flag then positional", List.of("--all", "server"), List.of("server"), Set.of("all"), Map.of()),
            c("positional then flag", List.of("server", "--all"), List.of("server"), Set.of("all"), Map.of()),
            c("short flag + positional", List.of("-a", "server"), List.of("server"), Set.of("a"), Map.of()),

            // --- опции со значением ---
            c("filter separate value", List.of("--filter", "name=app"), List.of(), Set.of(), Map.of("filter", List.of("name=app"))),
            c("filter with equals", List.of("--filter=name=app"), List.of(), Set.of(), Map.of("filter", List.of("name=app"))),
            c("filter repeated", List.of("--filter", "name=a", "--filter", "profile=W"),
                    List.of(), Set.of(), Map.of("filter", List.of("name=a", "profile=W"))),
            c("filter mixed styles", List.of("--filter=name=a", "--filter", "status=failed"),
                    List.of(), Set.of(), Map.of("filter", List.of("name=a", "status=failed"))),
            c("filter empty value", List.of("--filter="), List.of(), Set.of(), Map.of("filter", List.of(""))),
            c("tail with positional", List.of("--tail", "50", "server"), List.of("server"), Set.of(), Map.of("tail", List.of("50"))),
            c("positional then tail", List.of("server", "--tail", "50"), List.of("server"), Set.of(), Map.of("tail", List.of("50"))),
            c("tail equals", List.of("--tail=50", "server"), List.of("server"), Set.of(), Map.of("tail", List.of("50"))),
            c("profile option", List.of("--profile", "WORKERS"), List.of(), Set.of(), Map.of("profile", List.of("WORKERS"))),

            // --- комбинации (то, что реально придёт от jsup ps) ---
            c("ps -a", List.of("-a"), List.of(), Set.of("a"), Map.of()),
            c("ps -a --filter", List.of("-a", "--filter", "name=myName"),
                    List.of(), Set.of("a"), Map.of("filter", List.of("name=myName"))),
            c("ps --filter -a", List.of("--filter", "name=x", "-a"),
                    List.of(), Set.of("a"), Map.of("filter", List.of("name=x"))),
            c("ps full", List.of("-a", "--all", "--filter", "name=a", "--filter=profile=W", "extra"),
                    List.of("extra"), Set.of("a", "all"), Map.of("filter", List.of("name=a", "profile=W"))),
            c("filter value with spaces", List.of("--filter", "name=my app"), List.of(), Set.of(), Map.of("filter", List.of("name=my app")))
    );
    /**
     * Варианты, при которых конструктор должен бросить IllegalArgumentException
     */
    static final List<List<String>> INVALID = List.of(
            List.of("--filter"),                    // нет значения
            List.of("--tail"),
            List.of("--profile"),
            List.of("server", "--filter")           // значение пропущено в конце
    );

    private static ArgsCase c(String name, List<String> raw, List<String> pos,
                              Set<String> flags, Map<String, List<String>> opts) {
        return new ArgsCase(name, raw, pos, flags, opts);
    }

    static Stream<ArgsCase> valid() {
        return VALID.stream();
    }

    static Stream<List<String>> invalid() {
        return INVALID.stream();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("valid")
    void parses(ArgsCase c) {
        Args a = new Args(c.raw());

        assertEquals(c.positional().size(), a.positionalCount());
        for (int i = 0; i < c.positional().size(); i++) {
            assertEquals(c.positional().get(i), a.positional(i));
        }
        c.flags().forEach(f -> assertTrue(a.flag(f), STR."flag \{f}"));
        c.options().forEach((k, v) -> assertEquals(v, a.values(k), STR."option \{k}"));
    }

    @ParameterizedTest
    @MethodSource("invalid")
    void rejects(List<String> raw) {
        assertThrows(IllegalArgumentException.class, () -> new Args(raw));
    }

    record ArgsCase(String name,
                    List<String> raw,
                    List<String> positional,
                    Set<String> flags,
                    Map<String, List<String>> options) {
        @Override
        public String toString() {
            return name;
        }
    }
}
