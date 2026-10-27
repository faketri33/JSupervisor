package org.faketri.service;

import org.faketri.domain.AppConfig;
import org.faketri.domain.Application;
import org.faketri.domain.exceptions.application.ApplicationException;
import org.faketri.infrastructure.filter.Query;
import org.faketri.infrastructure.filter.QueryFactory;
import org.faketri.infrastructure.process.ApplicationRuntime;
import org.faketri.infrastructure.process.filters.RuntimeField;
import org.faketri.infrastructure.process.journal.JournalFactory;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class AppRegistryImplTest {

    private final AppRegistryImpl registry = new AppRegistryImpl();
    private final QueryFactory<ApplicationRuntime> queries = new QueryFactory<>(List.of(RuntimeField.values()));

    // ---------------- helpers ----------------

    private static ApplicationRuntime runtime(String name) {
        return runtime(name, "DEFAULT");
    }

    private static ApplicationRuntime runtime(String name, String profile) {
        var config = new AppConfig.Builder().profile(profile).build();
        return new ApplicationRuntime(new Application(name, config), JournalFactory.defaultInstance());
    }

    private static Query<ApplicationRuntime> byName(String name) {
        return Query.named("name=" + name, r -> r.name().equals(name));
    }

    private int size() {
        return registry.findAll(Query.all()).size();
    }

    // ---------------- add ----------------

    @Test
    void addStoresRuntime() {
        var rt = runtime("alpha");

        registry.add(rt);

        assertEquals(1, size());
        assertSame(rt, registry.find(byName("alpha")).orElseThrow());
    }

    @Test
    void addAllowsDifferentNames() {
        registry.add(runtime("alpha"));
        registry.add(runtime("beta"));

        assertEquals(2, size());
    }

    @Test
    void addRejectsSameInstanceTwice() {
        var rt = runtime("alpha");
        registry.add(rt);

        assertThrows(ApplicationException.class, () -> registry.add(rt));
        assertEquals(1, size());
    }

    @Test
    void addRejectsDuplicateName() {
        registry.add(runtime("alpha"));

        assertThrows(ApplicationException.class, () -> registry.add(runtime("alpha")));
        assertEquals(1, size());
    }

    @Test
    void addRejectsDuplicateNameIgnoringCase() {
        registry.add(runtime("alpha"));

        assertThrows(ApplicationException.class, () -> registry.add(runtime("ALPHA")));
        assertEquals(1, size());
    }

    @Test
    void addRejectsNull() {
        assertThrows(NullPointerException.class, () -> registry.add(null));
        assertEquals(0, size());
    }

    @Test
    void concurrentAddOfSameNameLetsExactlyOneThrough() throws Exception {
        int threads = 16;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger succeeded = new AtomicInteger();
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            var rt = runtime("same");
            futures.add(pool.submit(() -> {
                start.await();
                try {
                    registry.add(rt);
                    succeeded.incrementAndGet();
                } catch (ApplicationException ignored) {
                }
                return null;
            }));
        }
        start.countDown();
        for (Future<?> f : futures) f.get(5, TimeUnit.SECONDS);
        pool.shutdown();

        assertEquals(1, succeeded.get());
        assertEquals(1, size());
    }

    // ---------------- remove ----------------

    @Test
    void removeReturnsRuntimeAndForgetsIt() {
        var rt = runtime("alpha");
        registry.add(rt);

        assertSame(rt, registry.remove(rt.id()).orElseThrow());

        assertTrue(registry.find(byName("alpha")).isEmpty());
        assertEquals(0, size());
    }

    @Test
    void removeFreesTheName() {
        var rt = runtime("alpha");
        registry.add(rt);
        registry.remove(rt.id());

        assertDoesNotThrow(() -> registry.add(runtime("alpha")));
    }

    @Test
    void removeUnknownIdReturnsEmpty() {
        registry.add(runtime("alpha"));

        assertEquals(Optional.empty(), registry.remove(UUID.randomUUID()));
        assertEquals(1, size());
    }

    @Test
    void removeNullThrows() {
        assertEquals(Optional.empty(), registry.remove(null));
    }

    // ---------------- find ----------------

    @Test
    void findOnEmptyRegistryReturnsEmpty() {
        assertTrue(registry.find(Query.all()).isEmpty());
    }

    @Test
    void findReturnsMatchingRuntime() {
        var alpha = runtime("alpha");
        var beta = runtime("beta");
        registry.add(alpha);
        registry.add(beta);

        assertSame(beta, registry.find(byName("beta")).orElseThrow());
    }

    @Test
    void findReturnsEmptyWhenNothingMatches() {
        registry.add(runtime("alpha"));

        assertTrue(registry.find(byName("nope")).isEmpty());
    }

    @Test
    void findReturnsOneOfSeveralMatches() {
        var a = runtime("a1", "WORKERS");
        var b = runtime("a2", "WORKERS");
        registry.add(a);
        registry.add(b);

        var found = registry.find(queries.parse(List.of("profile=WORKERS"))).orElseThrow();

        assertTrue(found == a || found == b);
    }

    @Test
    void findWorksWithParsedQueries() {
        var rt = runtime("alpha", "WORKERS");
        registry.add(rt);
        registry.add(runtime("beta"));

        assertSame(rt, registry.find(queries.parse(List.of("name=alpha"))).orElseThrow());
        assertSame(rt, registry.find(queries.parse(List.of("name=ALPHA"))).orElseThrow());
        assertSame(rt, registry.find(queries.parse(List.of("id=" + rt.id().toString()))).orElseThrow());
        assertSame(rt, registry.find(queries.parse(List.of("profile=WORKERS", "name=alpha"))).orElseThrow());
        assertTrue(registry.find(queries.parse(List.of("name=nope"))).isEmpty());
        assertTrue(registry.find(queries.parse(List.of("id=" + UUID.randomUUID()))).isEmpty());
    }

    @Test
    void notStartedRuntimeNeverMatchesAnyPid() {
        registry.add(runtime("alpha"));

        assertTrue(registry.find(queries.parse(List.of("pid=1"))).isEmpty());
        assertTrue(registry.find(queries.parse(List.of("pid=4821"))).isEmpty());
    }

    // ---------------- findAll ----------------

    @Test
    void findAllOnEmptyRegistryReturnsEmptyList() {
        assertTrue(registry.findAll(Query.all()).isEmpty());
    }

    @Test
    void findAllWithAllReturnsEverything() {
        var a = runtime("a");
        var b = runtime("b");
        var c = runtime("c");
        registry.add(a);
        registry.add(b);
        registry.add(c);

        assertEquals(Set.of(a, b, c), Set.copyOf(registry.findAll(Query.all())));
    }

    @Test
    void findAllFiltersByQuery() {
        var w1 = runtime("w1", "WORKERS");
        var w2 = runtime("w2", "WORKERS");
        var d1 = runtime("d1", "DEFAULT");
        registry.add(w1);
        registry.add(w2);
        registry.add(d1);

        var result = registry.findAll(queries.parse(List.of("profile=WORKERS")));

        assertEquals(Set.of(w1, w2), Set.copyOf(result));
    }

    @Test
    void findAllReturnsEmptyListWhenNothingMatches() {
        registry.add(runtime("alpha"));

        var result = registry.findAll(byName("nope"));

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findAllCombinesSeveralConditions() {
        var a = runtime("alpha", "WORKERS");
        registry.add(a);
        registry.add(runtime("beta", "WORKERS"));

        var result = registry.findAll(queries.parse(List.of("profile=WORKERS", "name=alpha")));

        assertEquals(Set.of(a), Set.copyOf(result));
    }

    @Test
    void findAllResultIsDetachedFromRegistry() {
        registry.add(runtime("alpha"));
        var result = registry.findAll(Query.all());

        registry.add(runtime("beta"));

        assertEquals(1, result.size());
        assertEquals(2, size());
    }
}