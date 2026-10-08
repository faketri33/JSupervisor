package org.faketri.infrastructure.process;

import org.faketri.domain.Application;
import org.faketri.domain.State;
import org.faketri.domain.exceptions.application.ApplicationException;
import org.faketri.infrastructure.process.journal.Journal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

public final class ApplicationProcessContainer {

    private static final Logger log = LoggerFactory.getLogger(ApplicationProcessContainer.class);

    private final Application application;
    private final Journal journal;

    private volatile Process process;
    private volatile State state = State.PENDING;
    private volatile boolean stopRequested;

    public ApplicationProcessContainer(Application application, Journal journal) {
        this.application = application;
        this.journal = journal;
    }

    public void start() throws ApplicationException {
        try {
            journal.clear();

            process = new ProcessBuilder(application.getConfiguration().getCommands())
                    .redirectErrorStream(true)
                    .start();

            state = State.RUNNING;

            Thread.ofVirtual()
                    .name("journal-" + application.getName())
                    .start(this::watch);

        } catch (IOException e) {
            journal.write("Can't start process, " + e.getMessage());
            log.error("Can't start process", e);
            state = State.FAILED;
            throw new ApplicationException(e.getMessage());
        }
    }

    private void watch() {
        try (var reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) journal.write(line);
        } catch (IOException e) {
            journal.write("Read error: " + e.getMessage());
            log.error("Can't read process output", e);
        }

        try {
            int code = process.waitFor();
            if (stopRequested || code == 0) {
                journal.write("Process finished");
                state = State.FINISHED;
            } else {
                journal.write("Process failed, exit code " + code);
                state = State.FAILED;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void stop() {
        if (!isAlive()) return;
        journal.write("Stopping process");
        stopRequested = true;
        process.destroy();
    }

    public boolean isAlive() {
        var p = process;
        return p != null && p.isAlive();
    }

    public Long pid() {
        var p = process;
        return p == null ? null : p.pid();
    }

    public Application application() {
        return application;
    }

    public Collection<String> log() {
        return journal.log();
    }

    public State getState() {
        return state;
    }
}
