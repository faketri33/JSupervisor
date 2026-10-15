package org.faketri.service;

import org.faketri.domain.AppConfig;
import org.faketri.domain.Application;
import org.faketri.domain.exceptions.application.ApplicationException;
import org.faketri.infrastructure.process.ApplicationRuntime;
import org.faketri.infrastructure.process.filters.AppRef;
import org.faketri.infrastructure.process.filters.RuntimeFilters;
import org.faketri.infrastructure.process.journal.JournalFactory;
import org.faketri.utils.config.Constants;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class AppRegistryImplTest {

    private final AppRegistryImpl registry = new AppRegistryImpl();

    private static ApplicationRuntime runtime(String name) {
        return runtime(name, Constants.ConfigurationConstants.DEFAULT_PROFILE);
    }

    private static ApplicationRuntime runtime(String name, String profile) {
        var config = new AppConfig.Builder()
                .commands(List.of("ps"))
                .profile(profile)
                .build();
        return new ApplicationRuntime(new Application(name, config), JournalFactory.defaultInstance());
    }

    private int size() {
        return registry.findAll(r -> true).size();
    }

    @Test
    void addStoresRuntime() {
        var rt = runtime("alpha");

        registry.add(rt);

        assertEquals(1, size());
        assertSame(rt, registry.find(new AppRef.ById(rt.id())).orElseThrow());
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
        var pool = Executors.newFixedThreadPool(threads);
        var start = new CountDownLatch(1);
        var succeeded = new AtomicInteger();
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
        for (var f : futures) f.get(5, TimeUnit.SECONDS);
        pool.shutdown();
        pool.close();
        assertEquals(1, succeeded.get());
        assertEquals(1, size());
    }

    @Test
    void removeReturnsRuntimeAndForgetsIt() {
        var rt = runtime("alpha");
        registry.add(rt);

        assertSame(rt, registry.remove(rt.id()).orElseThrow());

        assertTrue(registry.find(new AppRef.ById(rt.id())).isEmpty());
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
        assertFalse(registry.remove(null).isPresent());
    }

    @Test
    void findOnEmptyRegistryReturnsEmpty() {
        assertTrue(registry.find(r -> true).isEmpty());
    }

    @Test
    void findReturnsMatchingRuntime() {
        var alpha = runtime("alpha");
        var beta = runtime("beta");
        registry.add(alpha);
        registry.add(beta);

        assertSame(beta, registry.find(r -> r.name().equals("beta")).orElseThrow());
    }

    @Test
    void findReturnsEmptyWhenNothingMatches() {
        registry.add(runtime("alpha"));

        assertTrue(registry.find(r -> r.name().equals("nope")).isEmpty());
    }

    @Test
    void findReturnsOneOfSeveralMatches() {
        var a = runtime("a1", "WORKERS");
        var b = runtime("a2", "WORKERS");
        registry.add(a);
        registry.add(b);

        var found = registry.find(r -> r.profile().equals("WORKERS")).orElseThrow();

        assertTrue(found == a || found == b);
    }

    @Test
    void findWorksWithAppRef() {
        var rt = runtime("alpha");
        registry.add(rt);

        assertSame(rt, registry.find(new AppRef.ById(rt.id())).orElseThrow());
        assertSame(rt, registry.find(new AppRef.ByName("alpha")).orElseThrow());
        assertSame(rt, registry.find(new AppRef.ByName("ALPHA")).orElseThrow());
        assertTrue(registry.find(new AppRef.ByName("nope")).isEmpty());
        assertTrue(registry.find(new AppRef.ById(UUID.randomUUID())).isEmpty());
    }

    @Test
    void notStartedRuntimeNeverMatchesAnyPid() {
        registry.add(runtime("alpha"));

        assertTrue(registry.find(new AppRef.ByPid(1)).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> registry.find(new AppRef.ByPid(-1)));
    }


    @Test
    void findAllOnEmptyRegistryReturnsEmptyList() {
        assertTrue(registry.findAll(r -> true).isEmpty());
    }

    @Test
    void findAllWithAlwaysTrueReturnsEverything() {
        var a = runtime("a");
        var b = runtime("b");
        var c = runtime("c");
        registry.add(a);
        registry.add(b);
        registry.add(c);

        assertEquals(Set.of(a, b, c), Set.copyOf(registry.findAll(r -> true)));
    }

    @Test
    void findAllFiltersByPredicate() {
        var w1 = runtime("w1", "WORKERS");
        var w2 = runtime("w2", "WORKERS");
        var d1 = runtime("d1", "DEFAULT");
        registry.add(w1);
        registry.add(w2);
        registry.add(d1);

        var result = registry.findAll(r -> r.profile().equals("WORKERS"));

        assertEquals(Set.of(w1, w2), Set.copyOf(result));
    }

    @Test
    void findAllReturnsEmptyListWhenNothingMatches() {
        registry.add(runtime("alpha"));

        var result = registry.findAll(r -> false);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findAllCombinesWithFilters() {
        var a = runtime("alpha", "WORKERS");
        registry.add(a);
        registry.add(runtime("beta", "WORKERS"));

        var result = registry.findAll(RuntimeFilters.parse(List.of("profile=WORKERS", "name=alp")));

        assertEquals(List.of(a), result);
    }

    @Test
    void findAllResultIsDetachedFromRegistry() {
        registry.add(runtime("alpha"));
        var result = registry.findAll(r -> true);

        registry.add(runtime("beta"));

        assertEquals(1, result.size());
        assertEquals(2, size());
    }
}