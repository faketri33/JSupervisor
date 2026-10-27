package org.faketri.net;

import org.faketri.infrastructure.commands.Command;
import org.faketri.net.io.dto.request.DispatchableRequest;
import org.faketri.net.io.dto.request.InternalRequest;
import org.faketri.net.socket.RequestProcessingPipeline;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class RequestProcessingPipelineTest {

    /**
     * Если процессор бросает другой тип (например BadRequestException), поменяй здесь одну строку.
     */
    private static final Class<? extends Exception> BAD = IllegalArgumentException.class;

    private final RequestProcessingPipeline processing = new RequestProcessingPipeline();

    static Stream<Arguments> conditions() {
        return Stream.of(
                Arguments.of("run", List.of("test"), List.of("test")),
                Arguments.of("run", List.of("name=test"), List.of("name=test")),
                Arguments.of("stop", List.of("4821"), List.of("4821")),
                Arguments.of("help", List.of(), List.of()),
                Arguments.of("ps", List.of(), List.of()),
                Arguments.of("ps", List.of("-a"), List.of()),
                Arguments.of("ps", List.of("--filter", "profile=W", "--filter", "name=x"),
                        List.of("profile=W", "name=x")),
                Arguments.of("ps", List.of("--filter=profile=W"), List.of("profile=W"))
        );
    }

    // =====================================================================
    //  Имя команды
    // =====================================================================

    static Stream<List<String>> acceptedPsArgs() {
        return Stream.of(
                List.of("-a"),
                List.of("--all"),
                List.of("-a", "--all"),
                List.of("-a", "--filter", "name=x"),
                List.of("--filter", "name=x", "-a"),
                List.of("--filter=profile=DEV"),
                List.of("server")
        );
    }

    static Stream<Bad> badRequests() {
        return Stream.of(
                new Bad("run without target", "run", List.of(), "expects exactly one"),
                new Bad("stop without target", "stop", List.of(), "expects exactly one"),
                new Bad("restart without target", "restart", List.of(), "expects exactly one"),
                new Bad("run with two targets", "run", List.of("a", "b"), "expects exactly one"),
                new Bad("ps with positional", "ps", List.of("server"), "takes no positional"),
                new Bad("help with positional", "help", List.of("x"), "takes no positional")
        );
    }

    private DispatchableRequest process(String command, String... args) throws Exception {
        return processing.process(new InternalRequest(command, List.of(args)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ps", "PS", "Ps", "pS"})
    void commandNameIsCaseInsensitive(String raw) throws Exception {
        assertEquals(Command.PS, process(raw).command());
    }

    // =====================================================================
    //  Условия: позиционные аргументы и --filter собираются в один список
    // =====================================================================

    /**
     * Если в enum добавили команду, а процессор её не умеет обработать, упадёт этот тест.
     */
    @ParameterizedTest
    @EnumSource(Command.class)
    void everyCommandIsProcessableWithMinimalArgs(Command c) throws Exception {
        String[] args = c.target() == Command.Target.ONE ? new String[]{"test"} : new String[0];

        assertEquals(c, process(c.name().toLowerCase(Locale.ROOT), args).command());
    }

    @ParameterizedTest
    @ValueSource(strings = {"foo", "psx", "p s"})
    void unknownCommandIsRejectedWithListOfAvailable(String raw) {
        var e = assertThrows(BAD, () -> process(raw));

        assertTrue(e.getMessage().contains("Unknown command '" + raw + "'"), e.getMessage());
        assertTrue(e.getMessage().contains("ps"), e.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void blankCommandIsRejected(String raw) {
        assertThrows(BAD, () -> processing.process(new InternalRequest(raw, List.of())));
    }

    @ParameterizedTest
    @MethodSource("conditions")
    void conditionsAreCollectedFromArgs(String command, List<String> args, List<String> expected) throws Exception {
        var p = processing.process(new InternalRequest(command, args));

        assertEquals(expected, p.conditions());
    }

    // =====================================================================
    //  Принятые флаги
    // =====================================================================

    @Test
    void processedRequestKeepsCommandAndArgs() throws Exception {
        var p = process("run", "test");

        assertEquals(Command.RUN, p.command());
        assertEquals("test", p.args().positional(0));
    }

    // =====================================================================
    //  Некорректные запросы
    // =====================================================================

    /**
     * Разбор условий принадлежит фабрике запросов: процессор не проверяет их синтаксис.
     */
    @ParameterizedTest
    @ValueSource(strings = {"bogus=1", "pid=abc", "name"})
    void processorDoesNotValidateConditionSyntax(String condition) throws Exception {
        assertEquals(List.of(condition), process("ps", "--filter", condition).conditions());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("badRequests")
    void rejectsBadRequest(Bad b) {
        var e = assertThrows(BAD, () -> processing.process(new InternalRequest(b.command(), b.args())));

        assertTrue(e.getMessage().contains(b.messagePart()), e.getMessage());
    }

    @ParameterizedTest
    @EnumSource(Command.class)
    void flagsAndOptionsDoNotOverlap(Command c) {
        assertTrue(Collections.disjoint(c.flags(), c.options()), c.name());
    }

    // =====================================================================
    //  Согласованность описания команд
    // =====================================================================

    @ParameterizedTest
    @EnumSource(Command.class)
    void everyCommandHasDescription(Command c) {
        assertFalse(c.description().isBlank(), c.name());
    }

    record Bad(String name, String command, List<String> args, String messagePart) {
        @Override
        public String toString() {
            return name;
        }
    }
}