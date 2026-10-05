package org.faketri.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);
    private final UUID id;
    private final String name;

    private final AppConfig configuration;

    private final AtomicInteger restartCount = new AtomicInteger(0);

    private State state;

    private Application(String name, AppConfig configuration, State state) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.configuration = configuration;
        changeStatus(state);
    }

    public static Application of(String name, List<String> commands) {
        return new Application(name, new AppConfig.Builder().commands(commands).build(), State.PENDING);
    }

    public static Application of(String name, AppConfig conf) {
        return new Application(name, conf, State.PENDING);
    }

    public UUID getAppId() {
        return id;
    }

    public String getName() {
        return name;
    }

    private void changeStatus(State state) {
        this.state = state;
    }

    public State getState() {
        return state;
    }

    public AppConfig getConfiguration() {
        return configuration;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Application that)) return false;

        return id.equals(that.id) && name.equals(that.name) && configuration.equals(that.configuration);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + configuration.hashCode();
        return result;
    }
}
