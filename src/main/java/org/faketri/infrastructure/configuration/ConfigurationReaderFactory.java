package org.faketri.infrastructure.configuration;


import org.faketri.infrastructure.configuration.yaml.YamlConfigurationReader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;

public final class ConfigurationReaderFactory {
    private ConfigurationReaderFactory() {
    }

    public static ConfigurationReader getInstance(Path path) throws IOException {
        String filename = path.getFileName().toString().toLowerCase(Locale.ROOT);
        if (filename.endsWith(".yaml") || filename.endsWith(".yml")) {
            return new YamlConfigurationReader(path);
        }
        throw new IOException("Unsupported path: " + path);
    }
}
