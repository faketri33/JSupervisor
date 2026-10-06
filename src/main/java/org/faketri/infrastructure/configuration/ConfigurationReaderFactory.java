package org.faketri.infrastructure.configuration;


import org.faketri.infrastructure.configuration.yaml.YamlConfigurationReader;

import java.io.IOException;
import java.nio.file.Path;

public final class ConfigurationReaderFactory {
    private ConfigurationReaderFactory() {
    }

    public static ConfigurationReader getInstance(Path path) throws IOException {
        if (path.endsWith(".yaml") || path.endsWith(".yml")) {
            return new YamlConfigurationReader(path);
        }
        throw new IOException("Unsupported path: " + path);
    }
}
