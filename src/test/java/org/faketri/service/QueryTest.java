package org.faketri.service;

import org.faketri.domain.AppConfig;
import org.faketri.domain.Application;
import org.faketri.domain.State;
import org.faketri.infrastructure.filter.Query;
import org.faketri.infrastructure.filter.QueryFactory;
import org.faketri.infrastructure.process.ApplicationRuntime;
import org.faketri.infrastructure.process.filters.RuntimeField;
import org.faketri.infrastructure.process.journal.JournalFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class QueryTest {

    private final QueryFactory<ApplicationRuntime> queries = new QueryFactory<>(List.of(RuntimeField.values()));

    private final ApplicationRuntime alpha = runtime("alpha", "WORKERS");
    private final ApplicationRuntime better = runtime("better", "WORKERS");
    private final ApplicationRuntime beta = runtime("beta", "DEFAULT");
    private final ApplicationRuntime crashed = runtime("crashed", "DEFAULT", State.FAILED);
    private final List<ApplicationRuntime> runtimes = List.of(alpha, better, beta, crashed);

    // ---------------- helpers ----------------
    private final Query<ApplicationRuntime> startsWithB = Query.named("startsWithB", r -> r.name().startsWith("b"));
    private final Query<ApplicationRuntime> endsWithA = Query.named("endsWithA", r -> r.name().endsWith("a"));

    private static ApplicationRuntime runtime(String name, String profile) {
        var config = new AppConfig.Builder().profile(profile).build();
        return new ApplicationRuntime(new Application(name, config), JournalFactory.defaultInstance());
    }

    private static ApplicationRuntime runtime(String name, String profile, State state) {
        var config = new AppConfig.Builder().profile(profile).build();
        var app = new Application(name, config);
        app.updateState(state);
        return new ApplicationRuntime(app, JournalFactory.defaultInstance());
    }

    private static Set<String> names(Collection<ApplicationRuntime> source, Query<ApplicationRuntime> q) {
        return source.stream().filter(q::matches).map(ApplicationRuntime::name).collect(Collectors.toSet());
    }

    static Stream<Bad> invalidConditions() {
        return Stream.of(
                new Bad("blank name", "name=", "Invalid value"),
                new Bad("blank profile", "profile=", "Invalid value"),
                new Bad("pid not a number", "pid=abc", "Invalid value"),
                new Bad("pid zero", "pid=0", "Invalid value"),
                new Bad("pid negative", "pid=-1", "Invalid value"),
                new Bad("pid empty", "pid=", "Invalid value"),
                new Bad("pid too long", "pid=12345678901", "Invalid value"),
                new Bad("unknown status", "status=bogus", "Invalid value"),
                new Bad("unknown key", "bogus=1", "Unknown field"),
                new Bad("empty key", "=x", "Unknown field"),
                new Bad("empty token", "", "Cannot interpret"),
                new Bad("blank token", "   ", "Cannot interpret")
        );
    }

    // =====================================================================
    //  Query: комбинаторы
    // =====================================================================

    private Set<String> match(Query<ApplicationRuntime> q) {
        return names(runtimes, q);
    }

    private Query<ApplicationRuntime> parse(String... conditions) {
        return queries.parse(List.of(conditions));
    }

    private Set<String> run(String... conditions) {
        return match(parse(conditions));
    }

    @Test
    void allMatchesEverything() {
        assertEquals(Set.of("alpha", "better", "beta", "crashed"), match(Query.all()));
    }

    @Test
    void andRequiresBoth() {
        assertEquals(Set.of("beta"), match(startsWithB.and(endsWithA)));
    }

    @Test
    void orRequiresEither() {
        assertEquals(Set.of("alpha", "better", "beta"), match(startsWithB.or(endsWithA)));
    }

    @Test
    void negateInverts() {
        assertEquals(Set.of("alpha", "crashed"), match(startsWithB.negate()));
    }

    @Test
    void doubleNegationIsIdentity() {
        assertEquals(match(startsWithB), match(startsWithB.negate().negate()));
    }

    @Test
    void allIsNeutralForAnd() {
        assertEquals(match(startsWithB), match(Query.<ApplicationRuntime>all().and(startsWithB)));
    }

    // =====================================================================
    //  Query: описание для отладки
    // =====================================================================

    @Test
    void combinatorsRejectNull() {
        assertThrows(NullPointerException.class, () -> startsWithB.and(null));
        assertThrows(NullPointerException.class, () -> startsWithB.or(null));
    }

    @Test
    void combinatorsBuildReadableDescription() {
        Query<ApplicationRuntime> a = Query.named("a", r -> true);
        Query<ApplicationRuntime> b = Query.named("b", r -> true);
        Query<ApplicationRuntime> c = Query.named("c", r -> true);

        assertEquals("a AND b AND c", a.and(b).and(c).describe());
        assertEquals("(a OR b)", a.or(b).describe());
        assertEquals("NOT a", a.negate().describe());
        assertEquals("all", Query.<ApplicationRuntime>all().describe());
        assertEquals("a", Query.<ApplicationRuntime>all().and(a).describe());
    }

    @Test
    void namedQueryUsesDescriptionAsToString() {
        assertEquals("startsWithB", startsWithB.describe());
        assertEquals("startsWithB", startsWithB.toString());
    }

    @Test
    void describeShowsParsedConditions() {
        assertEquals("profile=WORKERS AND name=bet", parse("profile=WORKERS", "name=bet").describe());
        assertEquals("all", parse().describe());
    }

    @Test
    void toStringEqualsDescribe() {
        var q = parse("profile=WORKERS", "name=bet");

        assertEquals(q.describe(), q.toString());
    }

    @Test
    void explainShowsHowManyEntriesEachConditionKeeps() {
        var q = parse("profile=WORKERS", "name=x");

        assertEquals("profile=WORKERS -> 2/4, name=x -> 0/4", Query.explain(q, runtimes));
    }

    // =====================================================================
    //  Query: чтение данных из запроса
    // =====================================================================

    @Test
    void explainWorksForSingleCondition() {
        assertEquals("name=zzz -> 0/4", Query.explain(parse("name=zzz"), runtimes));
    }

    @Test
    void usesReportsFieldsTheQueryWasBuiltFrom() {
        var q = parse("name=a", "status=failed");

        assertTrue(q.uses(RuntimeField.STATUS));
        assertTrue(q.uses(RuntimeField.NAME));
        assertFalse(q.uses(RuntimeField.PID));
        assertFalse(parse().uses(RuntimeField.STATUS));
    }

    @Test
    void usesSurvivesAndWithAnotherQuery() {
        var alive = Query.named("alive", ApplicationRuntime::isAlive);

        assertTrue(parse("status=failed").and(alive).uses(RuntimeField.STATUS));
    }

    // =====================================================================
    //  Поля: key=value
    // =====================================================================

    @Test
    void bareValueWithoutKeyIsRejected() {
        assertEquals("name=server", parse("server").describe());
    }

    @Test
    void emptyConditionListMatchesEverything() {
        assertEquals(Set.of("alpha", "better", "beta", "crashed"), run());
    }

    @Test
    void nameWithKeyMatchesExactlyIgnoringCase() {
        assertEquals(Set.of(), run("name=bet"));
        assertEquals(Set.of("beta"), run("name=beta"));
        assertEquals(Set.of("beta"), run("name=BETA"));
    }

    @Test
    void profileIsExact() {
        assertEquals(Set.of("alpha", "better"), run("profile=WORKERS"));
        assertEquals(Set.of(), run("profile=WORK"));
    }

    @Test
    void idMatchesFullUuidIgnoringCase() {
        String id = alpha.id().toString();

        assertEquals(Set.of("alpha"), run("id=" + id));
        assertEquals(Set.of("alpha"), run("id=" + id.toUpperCase(Locale.ROOT)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"xyz", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"})
    void invalidUuidFailsWhenMatching(String value) {
        var query = parse("id=" + value);

        assertThrows(IllegalArgumentException.class, () -> query.matches(alpha));
    }

    @Test
    void statusMatchesStateIgnoringCase() {
        assertEquals(Set.of("crashed"), run("status=failed"));
        assertEquals(Set.of("crashed"), run("status=FAILED"));
    }

    @Test
    void statusOfOtherStateExcludesFailed() {
        assertNotEquals(State.FAILED, alpha.state());

        assertEquals(Set.of("alpha", "better", "beta"), run("status=" + alpha.state().name()));
    }

    /**
     * Регрессия: status раньше сравнивался с именем приложения.
     */
    @Test
    void statusDoesNotMatchAppName() {
        var named = runtime("failed", "DEFAULT");
        assertNotEquals(State.FAILED, named.state());

        assertFalse(parse("status=failed").matches(named));
    }

    @Test
    void notStartedAppNeverMatchesAnyPid() {
        assertEquals(Set.of(), run("pid=1"));
        assertEquals(Set.of(), run("pid=4821"));
    }

    @Test
    void differentKeysAreAnded() {
        assertEquals(Set.of("better"), run("profile=WORKERS", "name=better"));
        assertEquals(Set.of(), run("profile=DEFAULT", "name=better"));
    }

    @Test
    void sameKeyRepeatedIsAnded() {
        assertEquals(Set.of("better"), run("name=better", "name=better"));
        assertEquals(Set.of(), run("name=better", "name=beta"));
    }

    // =====================================================================
    //  Условия без ключа
    // =====================================================================

    @Test
    void valueMayContainEqualsSign() {
        assertDoesNotThrow(() -> run("name=a=b"));
        assertEquals(Set.of(), run("name=a=b"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "1-2-3-4-5",
            "zzzzzzzz-zzzz-zzzz-zzzz-zzzzzzzzzzzz",
            "-5",
            "4821abc"
    })
    void incorrectConditionIsRejected(String raw) {
        assertEquals("", parse(raw).describe());
    }

    // =====================================================================
    //  Некорректные условия
    // =====================================================================

    @Test
    void keyedConditionsCombine() {
        assertEquals(Set.of("beta"), run("name=beta", "profile=DEFAULT"));
        assertEquals(Set.of(), run("name=beta", "profile=WORKERS"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidConditions")
    void rejectsInvalidCondition(Bad b) {
        var e = assertThrows(IllegalArgumentException.class, () -> queries.parse(List.of(b.condition())));

        assertTrue(e.getMessage().contains(b.messagePart()), e.getMessage());
    }

    @Test
    void unknownStatusListsAvailableStates() {
        var e = assertThrows(IllegalArgumentException.class, () -> parse("status=bogus"));

        for (State s : State.values()) {
            assertTrue(e.getMessage().contains(s.name()), e.getMessage());
        }
    }

    @Test
    void unknownKeyListsAvailableFields() {
        var e = assertThrows(IllegalArgumentException.class, () -> parse("bogus=1"));

        for (RuntimeField f : RuntimeField.values()) {
            assertTrue(e.getMessage().contains(f.key()), e.getMessage());
        }
    }

    /**
     * Если в enum добавят поле, switch перестанет компилироваться, пока не добавят образец значения.
     */
    @ParameterizedTest
    @EnumSource(RuntimeField.class)
    void everyFieldIsReachableByItsKey(RuntimeField field) {
        String sample = switch (field) {
            case ID -> UUID.randomUUID().toString();
            case NAME -> "x";
            case PID -> "1";
            case PROFILE -> "P";
            case STATUS -> State.values()[0].name();
        };

        assertEquals(field.name().toLowerCase(Locale.ROOT), field.key());
        assertDoesNotThrow(() -> parse(field.key() + "=" + sample));
        assertTrue(parse(field.key() + "=" + sample).uses(field));
    }

    // =====================================================================
    //  Согласованность описания полей
    // =====================================================================

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void pidMatchesRunningProcess() throws Exception {
        var config = new AppConfig.Builder().commands(List.of("sleep", "30")).build();
        var rt = new ApplicationRuntime(new Application("sleeper", config), JournalFactory.defaultInstance());
        try {
            rt.start();
            long pid = rt.pid();

            assertTrue(pid > 0);
            assertTrue(parse("pid=" + pid).matches(rt));
            var v = parse(String.valueOf(pid));
            assertEquals("pid="+pid, v.describe());
            assertFalse(parse("pid=" + (pid + 1)).matches(rt));
        } finally {
            rt.stop();
        }
    }

    // =====================================================================
    //  pid настоящего процесса
    // =====================================================================

    record Bad(String name, String condition, String messagePart) {
        @Override
        public String toString() {
            return name;
        }
    }
}
