package org.faketri.utils;

import org.faketri.domain.parser.ConfigEntry;
import org.faketri.domain.parser.RootConfig;
import org.faketri.exceptions.parser.InvalidConfigException;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class YAMLConfigurationParser implements Parser {

    public RootConfig parse(Path path) throws IOException {
        try (InputStream in = Files.newInputStream(path)) {
            return parse(in);
        }
    }

    private static RootConfig parse(InputStream in) {
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
}
