package org.faketri.core;

import org.faketri.domain.mapper.ConfigMapper;
import org.faketri.domain.repository.ApplicationRepository;
import org.faketri.infrastructure.controllers.AppController;
import org.faketri.infrastructure.parser.ParserFactory;
import org.faketri.infrastructure.parser.RootConfig;
import org.faketri.infrastructure.repository.ApplicationInMemoryRepository;
import org.faketri.net.ServerChannel;
import org.faketri.net.ServerFactory;
import org.faketri.net.handlers.StupidDispatcher;
import org.faketri.service.ApplicationServiceImpl;
import org.faketri.utils.FilesExtension;

import java.io.IOException;
import java.nio.file.Path;


public final class AppInstance {

    private final ServerChannel server;

    private AppInstance(ServerChannel server) {
        this.server = server;
    }

    public static AppInstance create(Path configPath) throws IOException {
        RootConfig config = ParserFactory
                .of(FilesExtension.getFileExtension(configPath.toString()))
                .parse(configPath);

        ApplicationRepository repository = new ApplicationInMemoryRepository();
        config.getApp().forEach((name, app) ->
                repository.save(ConfigMapper.toDto(name, app)));

        var controller = new AppController(new ApplicationServiceImpl(repository));

        var dispatcher = new StupidDispatcher();
        controller.registerTo(dispatcher);

        return new AppInstance(ServerFactory.newUnixSocketServer(dispatcher));
    }

    public void start() {
        server.start();
    }
}
