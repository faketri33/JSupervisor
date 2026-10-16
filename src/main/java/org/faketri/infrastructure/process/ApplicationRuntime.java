package org.faketri.infrastructure.process;

import org.faketri.domain.Application;
import org.faketri.domain.State;
import org.faketri.domain.dto.AppSnapshot;
import org.faketri.domain.exceptions.application.ApplicationException;
import org.faketri.infrastructure.process.journal.Journal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

public final class ApplicationRuntime {

    private static final Logger log = LoggerFactory.getLogger(ApplicationRuntime.class);

    private final Application application;
    private final Journal journal;

    private volatile Process process;
    private volatile boolean stopRequested;

    public ApplicationRuntime(Application application, Journal journal) {
        this.application = Objects.requireNonNull(application);
        this.journal = Objects.requireNonNull(journal);
    }

    public synchronized void start() {
        if (isAlive()) throw new ApplicationException("Already running");
        try {
            journal.clear();
            stopRequested = false;

            process = new ProcessBuilder(application.commands())
                    .redirectErrorStream(true)
                    .start();

            application.updateState(State.RUNNING);

            Thread.ofPlatform()
                    .name("journal-" + application.name())
                    .daemon(true)
                    .start(() -> watch(process));

        } catch (IOException e) {
            journal.write("Can't start process, " + e.getMessage());
            log.error("Can't start process", e);
            application.updateState(State.FAILED);
            throw new ApplicationException(e.getMessage());
        }
    }

    private void watch(Process p) {
        try (var reader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) journal.write(line);
        } catch (IOException e) {
            journal.write("Read error: " + e.getMessage());
        }

        try {
            int code = p.waitFor();
            if (process != p) return;
            if (stopRequested || code == 0) {
                journal.write("Process finished");
                application.updateState(State.FINISHED);
            } else {
                journal.write("Process failed, exit code " + code);
                application.updateState(State.FAILED);

            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public UUID id() {
        return application.id();
    }

    public String name() {
        return application.name();
    }

    public State state() {
        return application.state();
    }

    public String profile() {
        return application.profile();
    }

    public void stop() {
        if (!isAlive()) return;
        journal.write("Stopping process");
        stopRequested = true;
        process.destroy();
    }

    public boolean isAlive() {
        return process != null && process.isAlive();
    }

    public Long pid() {
        return process == null ? -1L : process.pid();
    }

    public AppSnapshot snapshot() {
        return new AppSnapshot(id(), name(), state(), profile(), pid(), isAlive(), log());
    }

    public Collection<String> log() {
        return journal.log();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ApplicationRuntime that)) return false;

        return stopRequested == that.stopRequested && application.equals(that.application) && journal.equals(that.journal);
    }

    @Override
    public int hashCode() {
        int result = application.hashCode();
        result = 31 * result + journal.hashCode();
        result = 31 * result + Boolean.hashCode(stopRequested);
        return result;
    }
}
