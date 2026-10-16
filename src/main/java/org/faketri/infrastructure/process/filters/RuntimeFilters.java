package org.faketri.infrastructure.process.filters;


import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Function;

public final class RuntimeFilters {

    // Soft check for finding multiple elements
    private static final Map<String, Function<String, Filters>> KEYS = Map.of(
            "id", v -> r -> r.id().toString().startsWith(v),
            "name", v -> r -> r.name().contains(v),
            "pid", v -> r -> r.pid().equals(Long.valueOf(v)),
            "profile", v -> r -> r.profile().equals(v),
            "status", v -> r -> r.name().equalsIgnoreCase(v)
    );

    private RuntimeFilters() {
    }

    public static Filters parse(List<String> raw) {
        Filters result = r -> true;
        for (String f : raw) {
            int eq = f.indexOf('=');
            if (eq <= 0) throw new IllegalArgumentException("Invalid filter '" + f + "', expected key=value");
            String key = f.substring(0, eq);
            var factory = KEYS.get(key);
            if (factory == null) {
                throw new IllegalArgumentException("Unknown filter key '" + key + "'. Available: " + new TreeSet<>(KEYS.keySet()));
            }
            result = result.and(factory.apply(f.substring(eq + 1)));
        }
        return result;
    }
}

