package org.faketri.infrastructure.parser;

public class ParserFactory {
    private ParserFactory() {
        /* This utility class should not be instantiated */
    }

    // on future
    public static Parser of(String type) {
        return switch (type) {
            case "yaml" -> new YAMLConfigurationParser();
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }
}
