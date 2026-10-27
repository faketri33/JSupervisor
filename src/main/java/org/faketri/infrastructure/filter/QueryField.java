package org.faketri.infrastructure.filter;

import java.util.Optional;

public interface QueryField<T> {
    String key();

    Query<T> of(String value);

    default Optional<Query<T>> ofBare(String value) {
        return Optional.empty();
    }
}
