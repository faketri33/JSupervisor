package org.faketri.infrastructure.filter;

import java.util.*;


public final class QueryFactory<T> {

    private final Map<String, QueryField<T>> byKey = new TreeMap<>();
    private final List<QueryField<T>> ordered;

    public QueryFactory(Collection<? extends QueryField<T>> fields) {
        this.ordered = List.copyOf(fields);
        for (QueryField<T> f : ordered) byKey.put(f.key(), f);
    }

    public Query<T> parse(Collection<String> raw) {
        Query<T> result = Query.all();
        for (String token : raw) result = result.and(condition(token));
        return result;
    }

    private Query<T> condition(String token) {
        int eq = token.indexOf('=');
        if (eq < 0) return bare(token);

        String key = token.substring(0, eq);
        QueryField<T> field = byKey.get(key);
        if (field == null) {
            throw new IllegalArgumentException("Unknown field '" + key + "'. Available: " + byKey.keySet());
        }
        return field.of(token.substring(eq + 1));
    }

    private Query<T> bare(String token) {
        for (QueryField<T> f : ordered) {
            Optional<Query<T>> q = f.ofBare(token);
            if (q.isPresent()) return q.get();
        }
        throw new IllegalArgumentException("Cannot interpret '" + token + "', use key=value. Available: " + byKey.keySet());
    }
}
