package org.faketri.core;

import org.faketri.domain.Application;
import org.faketri.infrastructure.configuration.ConfigurationReaderFactory;
import org.faketri.infrastructure.controllers.HelpController;
import org.faketri.infrastructure.controllers.LifecycleController;
import org.faketri.infrastructure.controllers.SearchApplicationController;
import org.faketri.infrastructure.process.ApplicationRuntime;
import org.faketri.infrastructure.process.journal.JournalFactory;
import org.faketri.net.ServerChannel;
import org.faketri.net.ServerFactory;
import org.faketri.net.handlers.StupidDispatcher;
import org.faketri.service.AppRegistryImpl;
import org.faketri.service.ApplicationLifecycle;
import org.faketri.service.ApplicationLifecycleImpl;
import org.faketri.service.ApplicationRegistry;
import org.faketri.utils.config.Constants;

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

        ApplicationRegistry registry = new AppRegistryImpl();
        ApplicationLifecycle lifecycle = new ApplicationLifecycleImpl(registry);

        apps.forEach(a -> registry.add(new ApplicationRuntime(a, JournalFactory.defaultInstance())));

        var dispatcher = getDispatcher(registry, lifecycle);

        return new AppInstance(ServerFactory.newUnixSocketServer(dispatcher));
    }

    private static StupidDispatcher getDispatcher(ApplicationRegistry registry, ApplicationLifecycle lifecycle) {
        var controller = new SearchApplicationController(registry);
        var lifeController = new LifecycleController(lifecycle);
        var helpController = new HelpController();

        var dispatcher = new StupidDispatcher();
        controller.register(dispatcher);
        lifeController.register(dispatcher);
        helpController.register(dispatcher);

        if (!Constants.Global.IS_DEBUG && dispatcher.verifyRoutes())
            throw new IllegalStateException("Not all commands have their own handlers.");
        return dispatcher;
    }

    public void start() {
        server.start();
    }

}
