package org.faketri.core;

import org.faketri.domain.mapper.ConfigMapper;
import org.faketri.domain.repository.ApplicationRepository;
import org.faketri.infrastructure.controllers.AppController;
import org.faketri.infrastructure.parser.ParserFactory;
import org.faketri.infrastructure.parser.RootConfig;
import org.faketri.infrastructure.repository.ApplicationInMemoryRepository;
import org.faketri.net.ServerChannel;
import org.faketri.net.ServerFactory;
import org.faketri.service.ApplicationServiceImpl;
import org.faketri.utils.FilesExtends;

import java.io.IOException;
import java.nio.file.Path;


public class AppInstance {

    private final ServerChannel server;

    private AppInstance(ServerChannel server, AppController controller) {
        this.server = server;
        controller.registerTo(server);
    }

    public static AppInstance create(Path configPath) throws IOException {
        RootConfig config = ParserFactory
                .of(FilesExtends.getFileExtension(configPath.toString()))
                .parse(configPath);

        ApplicationRepository repository = new ApplicationInMemoryRepository();
        config.getApp().forEach((name, app) ->
                repository.save(ConfigMapper.toDto(name, app)));

        var controller = new AppController(new ApplicationServiceImpl(repository));
        return new AppInstance(ServerFactory.newUnixSocketServer(), controller);
    }

    public int start() {
        return server.start();
    }
}
