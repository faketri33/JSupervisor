package org.faketri.service;

import org.faketri.domain.AppConfig;
import org.faketri.domain.Application;
import org.faketri.domain.State;
import org.faketri.infrastructure.process.ApplicationRuntime;
import org.faketri.infrastructure.process.filters.AppRef;
import org.faketri.infrastructure.process.filters.Filters;
import org.faketri.infrastructure.process.filters.RuntimeFilters;
import org.faketri.infrastructure.process.journal.JournalFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class FiltersTest {

    private final AppRegistryImpl registry = new AppRegistryImpl();


    private static ApplicationRuntime runtime(String name) {
        return runtime(name, "DEFAULT");
    }

    private static ApplicationRuntime runtime(String name, String profile) {
        var config = new AppConfig.Builder().profile(profile).build();
        return new ApplicationRuntime(new Application(name, config), JournalFactory.defaultInstance());
    }

    private static Set<String> names(Collection<ApplicationRuntime> rts) {
        return rts.stream().map(ApplicationRuntime::name).collect(Collectors.toSet());
    }

    private ApplicationRuntime add(String name, String profile) {
        var rt = runtime(name, profile);
        registry.add(rt);
        return rt;
    }

    private Set<String> match(Filters filter) {
        return names(registry.findAll(filter));
    }

    @Nested
    class Combinators {

        private final Filters startsWithT = r -> r.name().startsWith("t");
        private final Filters containsE = r -> r.name().contains("e");

        @BeforeEach
        void fill() {
            add("test", "DEFAULT");
            add("alpha", "DEFAULT");
            add("better", "DEFAULT");
        }

        @Test
        void allMatchesEverything() {
            assertEquals(Set.of("test", "alpha", "better"), match(Filters.all()));
        }

        @Test
        void andRequiresBoth() {
            assertEquals(Set.of("test"), match(startsWithT.and(containsE)));
        }

        @Test
        void orRequiresEither() {
            assertEquals(Set.of("test", "better"), match(startsWithT.or(containsE)));
        }

        @Test
        void negateInverts() {
            assertEquals(Set.of("alpha", "better"), match(startsWithT.negate()));
        }

        @Test
        void andWithAllChangesNothing() {
            assertEquals(match(startsWithT), match(startsWithT.and(Filters.all())));
        }

        @Test
        void doubleNegationIsIdentity() {
            assertEquals(match(startsWithT), match(startsWithT.negate().negate()));
        }

        @Test
        void combinatorsRejectNull() {
            assertThrows(NullPointerException.class, () -> startsWithT.and(null));
            assertThrows(NullPointerException.class, () -> startsWithT.or(null));
        }

        @Test
        void appRefComposesWithPlainFilters() {
            Filters f = new AppRef.ByName("test").or(new AppRef.ByName("alpha"));

            assertEquals(Set.of("test", "alpha"), match(f));
        }
    }

    @Nested
    class AppRefParse {

        @Test
        void uuidBecomesById() {
            UUID id = UUID.randomUUID();

            assertEquals(new AppRef.ById(id), AppRef.parse(id.toString()));
            assertEquals(new AppRef.ById(id), AppRef.parse(id.toString().toUpperCase()));
        }

        @Test
        void digitsBecomePid() {
            assertEquals(new AppRef.ByPid(4821), AppRef.parse("4821"));
        }

        @Test
        void plainTextBecomesName() {
            assertEquals(new AppRef.ByName("server"), AppRef.parse("server"));
            assertEquals(new AppRef.ByName("my-app"), AppRef.parse("my-app"));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "12345678901",
                "1-2-3-4-5",
                "zzzzzzzz-zzzz-zzzz-zzzz-zzzzzzzzzzzz",
                "-5",
                "4821abc"
        })
        void lookalikesFallBackToName(String raw) {
            assertEquals(new AppRef.ByName(raw), AppRef.parse(raw));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   ", "\t"})
        void blankIsRejected(String raw) {
            assertThrows(IllegalArgumentException.class, () -> AppRef.parse(raw));
        }

        @Test
        void nonPositivePidIsRejected() {
            assertThrows(IllegalArgumentException.class, () -> new AppRef.ByPid(0));
            assertThrows(IllegalArgumentException.class, () -> new AppRef.ByPid(-1));
            assertThrows(IllegalArgumentException.class, () -> AppRef.parse("0"));
        }
    }

    @Nested
    class AppRefMatching {

        private ApplicationRuntime test;

        @BeforeEach
        void fill() {
            test = add("test", "DEFAULT");
            add("alpha", "DEFAULT");
            add("better", "DEFAULT");
        }

        @Test
        void byIdFindsExactlyThatOne() {
            assertEquals(Set.of("test"), match(new AppRef.ById(test.id())));
        }

        @Test
        void byIdUnknownFindsNothing() {
            assertTrue(registry.find(new AppRef.ById(UUID.randomUUID())).isEmpty());
        }

        @Test
        void byNameIsExactAndIgnoresCase() {
            assertEquals(Set.of("test"), match(new AppRef.ByName("test")));
            assertEquals(Set.of("test"), match(new AppRef.ByName("TEST")));
        }

        @Test
        void byNameIsNotSubstring() {
            assertEquals(Set.of(), match(new AppRef.ByName("tes")));
            assertEquals(Set.of(), match(new AppRef.ByName("testing")));
        }

        @Test
        void idAndNameBothMatchingFindsOne() {
            Filters idAndName = new AppRef.ById(test.id()).and(new AppRef.ByName(test.name()));

            var found = registry.find(idAndName);

            assertTrue(found.isPresent());
            assertSame(test, found.get());
        }

        @Test
        void idAndNameOfDifferentAppsFindsNothing() {
            Filters mismatch = new AppRef.ById(test.id()).and(new AppRef.ByName("alpha"));

            assertTrue(registry.find(mismatch).isEmpty());
        }

        @Test
        void negatedRefMeansEverythingElse() {
            assertEquals(Set.of("alpha", "better"), match(new AppRef.ByName("test").negate()));
        }

        @Test
        void notStartedAppNeverMatchesAnyPid() {
            assertTrue(registry.find(new AppRef.ByPid(1)).isEmpty());
            assertTrue(registry.find(new AppRef.ByPid(4821)).isEmpty());
        }

        @Test
        @DisabledOnOs(OS.WINDOWS)
        void byPidFindsRunningProcess() {
            var config = new AppConfig.Builder().commands(List.of("sleep", "30")).build();
            var rt = new ApplicationRuntime(new Application("sleeper", config), JournalFactory.defaultInstance());
            registry.add(rt);
            try {
                rt.start();
                long pid = rt.pid();

                assertTrue(pid > 0);
                assertSame(rt, registry.find(new AppRef.ByPid(pid)).orElseThrow());
                assertSame(rt, registry.find(AppRef.parse(String.valueOf(pid))).orElseThrow());
            } finally {
                rt.stop();
            }
        }
    }

    @Nested
    class RuntimeFiltersParse {

        private ApplicationRuntime alpha;

        static Stream<String> invalidFilters() {
            return Stream.of(
                    "name",
                    "=value",
                    "",
                    "unknown=value",
                    "pid=abc",
                    "pid="
            );
        }

        @BeforeEach
        void fill() {
            add("test", "DEFAULT");
            alpha = add("alpha", "WORKERS");
            add("better", "WORKERS");
            add("beta", "DEFAULT");
        }

        private Set<String> run(String... filters) {
            return match(RuntimeFilters.parse(List.of(filters)));
        }

        @Test
        void emptyListMatchesEverything() {
            assertEquals(Set.of("test", "alpha", "better", "beta"), run());
        }

        @Test
        void nameIsSubstring() {
            assertEquals(Set.of("better", "beta"), run("name=et"));
        }

        @Test
        void profileIsExact() {
            assertEquals(Set.of("alpha", "better"), run("profile=WORKERS"));
            assertEquals(Set.of(), run("profile=WORK"));
        }

        @Test
        void idIsPrefix() {
            String prefix = alpha.id().toString().substring(0, 8);

            assertEquals(Set.of("alpha"), run("id=" + prefix));
        }

        @Test
        void differentKeysAreAnded() {
            assertEquals(Set.of("better"), run("profile=WORKERS", "name=et"));
        }

        @Test
        void sameKeyRepeatedIsAnded() {
            // содержат и "e", и "t"
            assertEquals(Set.of("test", "better", "beta"), run("name=e", "name=t"));
        }

        @Test
        void statusMatchesCurrentStateIgnoringCase() {
            String state = alpha.state().name();

            assertEquals(4, run("status=" + state.toLowerCase()).size());
            assertEquals(4, run("status=" + state.toUpperCase()).size());
        }

        /**
         * Регрессия: status раньше сравнивался с именем приложения.
         */
        @Test
        void statusDoesNotMatchAppName() {
            State other = Arrays.stream(State.values())
                    .filter(s -> s != alpha.state())
                    .findFirst().orElseThrow();
            var named = runtime(other.name().toLowerCase());

            assertFalse(RuntimeFilters.parse(List.of("status=" + other.name())).test(named));
        }

        @Test
        void notStartedAppNeverMatchesPid() {
            assertEquals(Set.of(), run("pid=1"));
            assertEquals(Set.of(), run("pid=4821"));
        }

        @Test
        void valueMayContainEqualsSign() {
            // ключ до первого '=', остальное значение
            assertDoesNotThrow(() -> run("name=a=b"));
            assertEquals(Set.of(), run("name=a=b"));
        }

        @Test
        void combiningSeveralFiltersDoesNotThrow() {
            assertDoesNotThrow(() -> RuntimeFilters.parse(List.of("name=a", "profile=WORKERS", "status=x")));
        }

        @ParameterizedTest
        @MethodSource("invalidFilters")
        void invalidFilterIsRejected(String raw) {
            assertThrows(IllegalArgumentException.class, () -> RuntimeFilters.parse(List.of(raw)));
        }

        /**
         * Требует проверку pid > 0 в фабрике фильтра (см. ниже), иначе pid=-1 совпадёт со всеми неживыми.
         */
        @Test
        void nonPositivePidFilterIsRejected() {
            assertThrows(IllegalArgumentException.class, () -> RuntimeFilters.parse(List.of("pid=-1")));
            assertThrows(IllegalArgumentException.class, () -> RuntimeFilters.parse(List.of("pid=0")));
        }
    }
}