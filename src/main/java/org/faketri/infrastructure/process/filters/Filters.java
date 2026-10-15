package org.faketri.infrastructure.process.filters;

import org.faketri.infrastructure.process.ApplicationRuntime;

import java.util.Objects;
import java.util.function.Predicate;

@FunctionalInterface
public interface Filters extends Predicate<ApplicationRuntime> {

    static Filters all() {
        return r -> true;
    }

    @Override
    default Filters and(Predicate<? super ApplicationRuntime> other) {
        Objects.requireNonNull(other);
        return r -> test(r) && other.test(r);
    }

    @Override
    default Filters or(Predicate<? super ApplicationRuntime> other) {
        Objects.requireNonNull(other);
        return r -> test(r) || other.test(r);
    }

    @Override
    default Filters negate() {
        return r -> !test(r);
    }

}

