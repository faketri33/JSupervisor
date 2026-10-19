package org.faketri.net;

import org.faketri.domain.AppConfig;
import org.faketri.domain.Application;
import org.faketri.infrastructure.process.ApplicationRuntime;
import org.faketri.infrastructure.process.filters.AppRef;
import org.faketri.infrastructure.process.filters.Filters;
import org.faketri.infrastructure.process.journal.JournalFactory;
import org.faketri.net.io.dto.request.ProcessedRequest;
import org.faketri.net.io.dto.request.Request;
import org.faketri.infrastructure.commands.Command;
import org.faketri.net.socket.UnixRequestProcessing;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class UnixRequestProcessingTest {

    private static final UUID ID = UUID.fromString("3f2c1a9e-7b4d-4c1a-9e2f-1a2b3c4d5e6f");

    private final UnixRequestProcessing processing = new UnixRequestProcessing();

    private final List<ApplicationRuntime> runtimes = List.of(
            runtime("alpha", "WORKERS"),
            runtime("better", "WORKERS"),
            runtime("test", "DEFAULT")
    );

    // ---------------- helpers ----------------

    private static ApplicationRuntime runtime(String name, String profile) {
        var config = new AppConfig.Builder().profile(profile).build();
        return new ApplicationRuntime(new Application(name, config), JournalFactory.defaultInstance());
    }

    private static Request req(String command, String... args) {
        return new Request(command, List.of(args));
    }

    private ProcessedRequest process(String command, String... args) {
        return processing.process(req(command, args));
    }

    private Set<String> match(Filters filter) {
        return runtimes.stream()
                .filter(filter)
                .map(ApplicationRuntime::name)
                .collect(Collectors.toSet());
    }

    // =====================================================================
    //  Разбор имени команды
    // =====================================================================

    @ParameterizedTest
    @ValueSource(strings = {"ps", "PS", "Ps", "pS"})
    void commandNameIsCaseInsensitive(String raw) {
        assertEquals(Command.PS, process(raw).command());
    }

    @ParameterizedTest
    @EnumSource(Command.class)
    void everyCommandIsProcessableWithMinimalArgs(Command c) {
        String[] args = c.target() == Command.Target.ONE ? new String[]{"test"} : new String[0];

        assertEquals(c, process(c.name().toLowerCase(Locale.ROOT), args).command());
    }

    @Test
    void unknownCommandIsRejectedWithListOfAvailable() {
        var e = assertThrows(IllegalArgumentException.class, () -> process("foo"));

        assertTrue(e.getMessage().contains("Unknown command 'foo'"), e.getMessage());
        assertTrue(e.getMessage().contains("ps"), e.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void blankCommandIsRejected(String raw) {
        assertThrows(IllegalArgumentException.class, () -> processing.process(new Request(raw, List.of())));
    }

    // =====================================================================
    //  Команды с одной целью: run, restart, stop
    // =====================================================================

    static Stream<Arguments> refs() {
        return Stream.of(
                Arguments.of("run", "test", new AppRef.ByName("test")),
                Arguments.of("stop", "4821", new AppRef.ByPid(4821)),
                Arguments.of("restart", ID.toString(), new AppRef.ById(ID))
        );
    }

    @ParameterizedTest
    @MethodSource("refs")
    void targetIsBuiltFromPositionalArgument(String command, String raw, AppRef expected) {
        assertEquals(expected, process(command, raw).filters());
    }

    @Test
    void processedRequestKeepsParsedArgs() {
        var p = process("run", "test");

        assertEquals(Command.RUN, p.command());
        assertEquals("test", p.args().positional(0));
    }

    /**
     * Регрессия: правило "только запущенные" принадлежит обработчику ps, а не процессору.
     * Ни одно из приложений не запущено, и фильтр обязан пропускать их всех.
     */
    @Test
    void psWithoutFiltersMatchesEverythingEvenNotAlive() {
        assertEquals(Set.of("alpha", "better", "test"), match(process("ps").filters()));
    }

    @Test
    void psFilterNarrowsResult() {
        assertEquals(Set.of("alpha", "better"), match(process("ps", "--filter", "profile=WORKERS").filters()));
    }

    @Test
    void psFiltersAreAnded() {
        var p = process("ps", "--filter", "profile=WORKERS", "--filter", "name=bet");

        assertEquals(Set.of("better"), match(p.filters()));
    }

    static Stream<List<String>> acceptedPsArgs() {
        return Stream.of(
                List.of("-a"),
                List.of("--all"),
                List.of("-a", "--all"),
                List.of("-a", "--filter", "name=x"),
                List.of("--filter", "name=x", "-a"),
                List.of("--filter=profile=DEV")
        );
    }

    @ParameterizedTest
    @MethodSource("acceptedPsArgs")
    void psAcceptsItsFlagsAndOptions(List<String> args) {
        assertDoesNotThrow(() -> processing.process(new Request("ps", args)));
    }

    // =====================================================================
    //  help: без цели
    // =====================================================================

    @Test
    void helpMatchesEverything() {
        assertEquals(Set.of("alpha", "better", "test"), match(process("help").filters()));
    }

    // =====================================================================
    //  Некорректные запросы
    // =====================================================================

    record Bad(String name, String command, List<String> args, String messagePart) {
        @Override
        public String toString() {
            return name;
        }
    }

    static Stream<Bad> badRequests() {
        return Stream.of(
                // --- цель обязательна и ровно одна ---
                new Bad("run without target", "run", List.of(), "exactly one"),
                new Bad("stop without target", "stop", List.of(), "exactly one"),
                new Bad("run with two targets", "run", List.of("a", "b"), "exactly one"),
                new Bad("run with blank target", "run", List.of(""), "Invalid argument"),

                // --- флаги и опции, которых у команды нет ---
                new Bad("restart with unknown flag", "restart", List.of("-a", "test"), "Unknown option 'a'"),
                new Bad("run with filter option", "run", List.of("--filter", "name=x", "test"), "Unknown option 'filter'"),
                new Bad("ps with unknown long flag", "ps", List.of("--foo"), "Unknown option 'foo'"),
                new Bad("ps with unknown combined flag", "ps", List.of("-aq"), "Unknown option 'q'"),
                new Bad("help with flag", "help", List.of("-a"), "Unknown option 'a'"),

                // --- лишние позиционные ---
                new Bad("ps with positional", "ps", List.of("server"), "takes no positional"),
                new Bad("help with positional", "help", List.of("x"), "takes no positional"),

                // --- ошибки разбора, пришедшие снизу ---
                new Bad("ps option without value", "ps", List.of("--filter"), "requires a value"),
                new Bad("ps filter without equals", "ps", List.of("--filter", "name"), ""),
                new Bad("ps unknown filter key", "ps", List.of("--filter", "bogus=1"), ""),
                new Bad("ps non-numeric pid", "ps", List.of("--filter", "pid=abc"), ""),
                new Bad("ps zero pid", "ps", List.of("--filter", "pid=0"), "")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("badRequests")
    void rejectsBadRequest(Bad b) {
        var e = assertThrows(IllegalArgumentException.class,
                () -> processing.process(new Request(b.command(), b.args())));

        assertTrue(e.getMessage().contains(b.messagePart()), e.getMessage());
    }

    @ParameterizedTest
    @EnumSource(Command.class)
    void flagsAndOptionsDoNotOverlap(Command c) {
        assertTrue(Collections.disjoint(c.flags(), c.options()), c.name());
    }

    @ParameterizedTest
    @EnumSource(Command.class)
    void everyCommandHasDescription(Command c) {
        assertFalse(c.description().isBlank(), c.name());
    }
}