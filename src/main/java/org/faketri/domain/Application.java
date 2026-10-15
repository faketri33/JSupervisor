package org.faketri.domain;

import org.faketri.domain.exceptions.application.ApplicationException;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Application {

    private final UUID id;
    private final String name;
    private final AppConfig config;
    private volatile State state;
    private int restartCount;

    public Application(String name, AppConfig config, State state, int restartCount) {
        if (name == null || name.isBlank()) throw new ApplicationException("Name cannot be empty");

        this.id = UUID.randomUUID();
        this.name = name;
        this.config = Objects.requireNonNull(config);
        this.state = Objects.requireNonNull(state);
        this.restartCount = restartCount;
    }

    public Application(String name, AppConfig config) {
        this(name, config, State.PENDING, 0);
    }

    public UUID id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String profile() {
        return config.profile();
    }

    public List<String> commands() {
        return config.commands();
    }

    public State state() {
        return state;
    }

    public synchronized void updateState(State state) {
        this.state = state;
    }

    public synchronized boolean tryRegisterRestart(int exitCode, boolean stopRequested) {
        if (stopRequested) return false;
        if (!config.restartPolicy().shouldRestart(exitCode)) return false;
        if (restartCount >= config.maxRestart()) return false;
        restartCount++;
        return true;
    }

    public synchronized void resetRestarts() {
        restartCount = 0;
    }

    public synchronized int restartCount() {
        return restartCount;
    }
}
