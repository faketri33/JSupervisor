package org.faketri;

import org.faketri.mapper.ConfigMapper;
import org.faketri.utils.Constants;
import org.faketri.utils.YAMLConfigurationParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static final ApplicationManager manager = new ApplicationManager();

    public static void main(String[] args) throws IOException {
        var cnf = new YAMLConfigurationParser().parse(Path.of("/home/faketri/git/my/JSupervisor/src/main/resources/config.yaml"));
        cnf.getApp().forEach((k, v) -> manager.save(ConfigMapper.toDto(k, v)));

        manager.getByName("test").listen(log::info, log::error);
        manager.startAllByProfile(Constants.ConfigurationConstants.DEFAULT_PROFILE);
    }
}