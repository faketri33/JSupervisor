package org.faketri.core;

import org.faketri.domain.Application;
import org.faketri.domain.repository.ApplicationRepository;
import org.faketri.infrastructure.configuration.ConfigurationReaderFactory;
import org.faketri.infrastructure.controllers.AppController;
import org.faketri.infrastructure.repository.ApplicationInMemoryRepository;
import org.faketri.net.ServerChannel;
import org.faketri.net.ServerFactory;
import org.faketri.net.handlers.StupidDispatcher;
import org.faketri.service.ApplicationServiceImpl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;


public final class AppInstance {

    private final ServerChannel server;

    private AppInstance(ServerChannel server) {
        this.server = server;
    }

    public static AppInstance create(Path configPath) throws IOException {
        Collection<Application> apps = ConfigurationReaderFactory
                .getInstance(configPath)
                .read();

        ApplicationRepository repository = new ApplicationInMemoryRepository();
        repository.saveAll(apps);

        var controller = new AppController(new ApplicationServiceImpl(repository));

        var dispatcher = new StupidDispatcher();
        controller.registerTo(dispatcher);

        return new AppInstance(ServerFactory.newUnixSocketServer(dispatcher));
    }

    public void start() {
        server.start();
    }
}
