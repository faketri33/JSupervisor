package org.faketri.infrastructure.process.filters;

import org.faketri.infrastructure.process.ApplicationRuntime;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public sealed interface AppRef extends Predicate<ApplicationRuntime> {

    static AppRef parse(String raw) {
        if (raw == null || raw.isBlank()) throw new IllegalArgumentException("Empty app reference");

        Optional<UUID> uuid = tryUuid(raw);
        if (uuid.isPresent()) return new ById(uuid.get());
        if (isPid(raw)) return new ByPid(Long.parseLong(raw));
        return new ByName(raw);
    }

    private static Optional<UUID> tryUuid(String s) {
        if (s.length() != 36) return Optional.empty();
        try {
            UUID u = UUID.fromString(s);
            return u.toString().equalsIgnoreCase(s) ? Optional.of(u) : Optional.empty();
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private static boolean isPid(String s) {
        if (s.isEmpty() || s.length() > 10) return false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') return false;
        }
        return true;
    }

    record ById(UUID id) implements AppRef {
        public boolean test(ApplicationRuntime r) {
            return r.id().equals(id);
        }
    }

    record ByName(String name) implements AppRef {
        public boolean test(ApplicationRuntime r) {
            return r.name().equals(name);
        }
    }

    record ByPid(long pid) implements AppRef {
        public boolean test(ApplicationRuntime r) {
            return r.snapshot().pid() == pid;
        }
    }
}
