package org.faketri.infrastructure.process.filters;

import org.faketri.domain.State;
import org.faketri.infrastructure.filter.Query;
import org.faketri.infrastructure.filter.QueryField;
import org.faketri.infrastructure.process.ApplicationRuntime;
import org.faketri.utils.data.DataTypeDefine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

public enum RuntimeField implements QueryField<ApplicationRuntime> {
    /**
     * The order of fields matters when validating data types.
     * An incorrect field order can lead to unexpected situations
     * where a field might be validated differently.
     *
     * TODO: FIXME
     * */
    ID(v -> r -> r.id().equals(UUID.fromString(v)), DataTypeDefine::isUUID),
    PID(v -> {
        if (!v.matches("[0-9]{1,10}") || Long.parseLong(v) <= 0) throw invalid("pid", v, "< 0");
        long pid = Long.parseLong(v);
        return r -> r.pid() == pid;
    }, DataTypeDefine::isPid),
    NAME(v -> {
        notBlank("name", v);
        return r -> r.name().equalsIgnoreCase(v);
    }, v -> !v.isBlank() && !v.matches("/^\\p{L}+$/u")),
    PROFILE(v -> {
        notBlank("profile", v);
        return r -> r.profile().equals(v);
    }, r -> false),
    STATUS(v -> {
        State state = Arrays.stream(State.values())
                .filter(s -> s.name().equalsIgnoreCase(v))
                .findFirst()
                .orElseThrow(() -> invalid("status", v, Arrays.toString(State.values())));
        return r -> r.state() == state;
    }, r -> false);

    private static final Logger log = LoggerFactory.getLogger(RuntimeField.class);
    private final Function<String, Query<ApplicationRuntime>> compiler;
    private final Predicate<String> bareFormat;

    RuntimeField(Function<String, Query<ApplicationRuntime>> compiler, Predicate<String> bareFormat) {
        this.compiler = compiler;
        this.bareFormat = bareFormat;
    }

    private static void notBlank(String key, String v) {
        if (v.isBlank()) throw invalid(key, v, "Blank text");
    }

    private static IllegalArgumentException invalid(String key, String value, String expected) {
        return new IllegalArgumentException("Invalid value '" + value + "' for '" + key + "', expected " + expected);
    }

    @Override
    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }

    @Override
    public Query<ApplicationRuntime> of(String value) {
        return new Query.FieldQuery<>(this, value, compiler.apply(value));
    }

    @Override
    public Optional<Query<ApplicationRuntime>> ofBare(String value) {
        return bareFormat.test(value) ? Optional.of(of(value)) : Optional.empty();
    }


}