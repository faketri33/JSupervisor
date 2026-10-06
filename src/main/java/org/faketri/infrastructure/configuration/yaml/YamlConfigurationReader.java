package org.faketri.infrastructure.configuration.yaml;

import org.faketri.domain.Application;
import org.faketri.infrastructure.configuration.ConfigurationReader;
import org.faketri.infrastructure.configuration.exception.InvalidConfigException;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

public final class YamlConfigurationReader implements ConfigurationReader {

    private final Path path;

    public YamlConfigurationReader(Path path) {
        this.path = path;
    }

    private static RootConfig read(InputStream in) {
        LoaderOptions options = new LoaderOptions();
        Constructor constructor = new Constructor(RootConfig.class, options);

        TypeDescription appConfigDesc = new TypeDescription(RootConfig.class);
        appConfigDesc.addPropertyParameters("app", String.class, ConfigEntry.class);
        constructor.addTypeDescription(appConfigDesc);

        Yaml yaml = new Yaml(constructor);
        RootConfig config = yaml.load(in);

        if (config == null || config.getApp() == null) {
            throw new InvalidConfigException("Config is empty or has no 'app' section");
        }

        validate(config);
        return config;
    }

    private static void validate(RootConfig config) {
        for (Map.Entry<String, ConfigEntry> entry : config.getApp().entrySet()) {
            String appName = entry.getKey();
            ConfigEntry appEntry = entry.getValue();

            if (appEntry == null) {
                throw new InvalidConfigException("App '" + appName + "' has no configuration body");
            }
            if (appEntry.getCommand() == null || appEntry.getCommand().isEmpty()) {
                throw new InvalidConfigException("App '" + appName + "' is missing required field 'command'");
            }
        }
    }

    @Override
    public Collection<Application> read() throws IOException {
        try (InputStream in = Files.newInputStream(path)) {
            return read(in).applications();
        }
    }
}
