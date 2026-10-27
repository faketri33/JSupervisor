package org.faketri.infrastructure.filter;

import java.util.*;
import java.util.stream.Collectors;

@FunctionalInterface
public interface Query<T> {

    static <T> Query<T> all() {
        return new AllOf<>(List.of());
    }

    static <T> Query<T> named(String description, Query<T> q) {
        Objects.requireNonNull(q);
        return new Query<>() {
            @Override
            public boolean matches(T t) {
                return q.matches(t);
            }

            @Override
            public String describe() {
                return description;
            }

            @Override
            public String toString() {
                return description;
            }
        };
    }

    static <T> String explain(Query<T> q, Collection<T> entries) {
        List<Query<? super T>> parts = new ArrayList<>();
        if (q instanceof AllOf<T>(List<Query<? super T>> parts1)) parts.addAll(parts1);
        else parts.add(q);

        StringJoiner out = new StringJoiner(", ");
        for (Query<? super T> p : parts) {
            long n = entries.stream().filter(p::matches).count();
            out.add(p.describe() + " -> " + n + "/" + entries.size());
        }
        return out.toString();
    }

    boolean matches(T t);

    default String describe() {
        return "<custom>";
    }

    default Query<T> and(Query<? super T> other) {
        Objects.requireNonNull(other);
        List<Query<? super T>> parts = new ArrayList<>();
        if (this instanceof AllOf<T>(List<Query<? super T>> parts1)) parts.addAll(parts1);
        else parts.add(this);
        parts.add(other);
        return new AllOf<>(parts);
    }

    default Query<T> or(Query<? super T> other) {
        Objects.requireNonNull(other);
        return named("(" + describe() + " OR " + other.describe() + ")", t -> matches(t) || other.matches(t));
    }

    default Query<T> negate() {
        return named("NOT " + describe(), t -> !matches(t));
    }

    default boolean uses(QueryField<?> field) {
        if (this instanceof FieldQuery<T> f) return f.field() == field;
        if (this instanceof AllOf<T>(List<Query<? super T>> parts)) return parts.stream().anyMatch(p -> p.uses(field));
        return false;
    }

    record AllOf<T>(List<Query<? super T>> parts) implements Query<T> {
        public AllOf {
            parts = List.copyOf(parts);
        }

        @Override
        public boolean matches(T t) {
            for (Query<? super T> p : parts) if (!p.matches(t)) return false;
            return true;
        }

        @Override
        public String describe() {
            return parts.isEmpty() ? "all" : parts.stream().map(Query::describe).collect(Collectors.joining(" AND "));
        }

        @Override
        public String toString() {
            return describe();
        }
    }

    record FieldQuery<T>(QueryField<T> field, String value, Query<T> test) implements Query<T> {
        @Override
        public boolean matches(T t) {
            return test.matches(t);
        }

        @Override
        public String describe() {
            return field.key() + "=" + value;
        }

        @Override
        public String toString() {
            return describe();
        }
    }
}
