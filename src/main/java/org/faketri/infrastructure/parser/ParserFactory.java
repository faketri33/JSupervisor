package org.faketri.infrastructure.parser;

public class ParserFactory {
    private ParserFactory() {
        /* This utility class should not be instantiated */
    }

    // on future
    public static Parser of(String type) {
        if (type.equalsIgnoreCase("yaml"))
            return new YAMLConfigurationParser();

        throw new IllegalStateException("Unexpected value: " + type);
    }
}
