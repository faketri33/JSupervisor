package org.faketri.net.io.dto.request;

import org.faketri.infrastructure.filter.Query;

import java.util.Optional;

public record RequestContext<T>(Args args, Query<T> query) {

    @Override
    public Query<T> query() {
        if (query == null)
            throw new IllegalStateException("Query was not parsed for this request. Check if QueryFactory is missing.");

        return query;
    }

    public Optional<Query<T>> optionalQuery() {
        return Optional.ofNullable(query);
    }
}
