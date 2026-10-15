package org.faketri.net.io.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.*;
import java.util.function.Function;

public final class Args {

    private static final Set<String> VALUE_OPTIONS = Set.of("filter", "tail", "profile");

    private final List<String> positional = new ArrayList<>();
    private final Set<String> flags = new HashSet<>();
    private final Map<String, List<String>> options = new HashMap<>();

    @JsonCreator
    public Args(List<String> raw) {
        for (int i = 0; i < raw.size(); i++) {
            String a = raw.get(i);

            if (a.startsWith("--") && a.length() > 2) {
                String body = a.substring(2);
                int eq = body.indexOf('=');
                String key = eq < 0 ? body : body.substring(0, eq);

                if (VALUE_OPTIONS.contains(key)) {
                    String value;
                    if (eq >= 0) {
                        value = body.substring(eq + 1);
                    } else if (i + 1 < raw.size()) {
                        value = raw.get(++i);
                    } else {
                        throw new IllegalArgumentException("Option --" + key + " requires a value");
                    }
                    options.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
                } else {
                    flags.add(key);
                }

            } else if (a.startsWith("-") && a.length() > 1 && !isNumber(a)) {
                for (char c : a.substring(1).toCharArray()) {
                    flags.add(String.valueOf(c));
                }

            } else {
                positional.add(a);
            }
        }
    }

    private static boolean isNumber(String s) {
        try {
            Long.parseLong(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public String positional(int i) {
        if (i >= positional.size()) {
            throw new IllegalArgumentException("Missing argument #" + (i + 1));
        }
        return positional.get(i);
    }

    public String positionalOr(int i, String def) {
        return i < positional.size() ? positional.get(i) : def;
    }

    public <T> T positional(int i, Function<String, T> parser) {
        String raw = positional(i);
        try {
            return parser.apply(raw);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid argument '" + raw + "': " + e.getMessage(), e);
        }
    }

    public int positionalCount() {
        return positional.size();
    }

    public boolean flag(String... names) {
        for (String n : names) {
            if (flags.contains(n)) return true;
        }
        return false;
    }

    public Optional<String> option(String key) {
        List<String> v = options.get(key);
        return v == null ? Optional.empty() : Optional.of(v.getLast());
    }

    public List<String> values(String key) {
        return options.getOrDefault(key, List.of());
    }

}