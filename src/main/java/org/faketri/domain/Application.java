package org.faketri.domain;

import java.util.List;
import java.util.UUID;

public class Application {

    private final UUID id;
    private final String name;

    private final AppConfig configuration;

    private State state;

    private Application(String name, AppConfig configuration) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.configuration = configuration;
        this.state = State.PENDING;
    }

    public static Application of(String name, List<String> commands) {
        return new Application(name, new AppConfig.Builder().commands(commands).build());
    }

    public static Application of(String name, AppConfig conf) {
        return new Application(name, conf);
    }

    public UUID getAppId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setState(State state){
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
