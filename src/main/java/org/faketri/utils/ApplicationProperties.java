package org.faketri.utils;

import org.faketri.Main;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApplicationProperties {

    private ApplicationProperties() {
    }

    private static final Properties properties = new Properties();
    private static boolean isLoad = false;

    private static final Pattern ENV_PATTERN =
            Pattern.compile("\\$\\{([^}]+)}");

    private static void load() {
        try (InputStream input = Main.class
                .getClassLoader()
                .getResourceAsStream("application.properties")) {

            if (input == null) {
                throw new IllegalStateException(
                        "application.properties not found"
                );
            }

            properties.load(input);
            isLoad = true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int getPropertiesInt(String key) {
        return Integer.parseInt(getProperties(key));
    }

    public static String getProperties(String key) {
        if (!isLoad) load();

        String value = properties.getProperty(key);

        if (value == null) return "";
        return resolveEnvironmentVariables(value);
    }

    private static String resolveEnvironmentVariables(String value) {
        Matcher matcher = ENV_PATTERN.matcher(value);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String envName = matcher.group(1);
            String envValue = System.getenv(envName);

            if (envValue == null) {
                throw new IllegalStateException(
                        "Environment variable not found: " + envName
                );
            }

            matcher.appendReplacement(
                    result,
                    Matcher.quoteReplacement(envValue)
            );
        }

        matcher.appendTail(result);
        return result.toString();
    }
}
