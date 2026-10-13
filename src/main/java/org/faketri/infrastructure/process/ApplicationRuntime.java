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
import java.util.UUID;

public final class ApplicationRuntime {

    private static final Logger log = LoggerFactory.getLogger(ApplicationRuntime.class);

    private final Application application;
    private final Journal journal;

    private volatile Process process;
    private volatile boolean stopRequested;

    public ApplicationRuntime(Application application, Journal journal) {
        this.application = application;
        this.journal = journal;
    }

    public synchronized void start() {
        if (isAlive()) throw new ApplicationException("Already running");
        try {
            journal.clear();
            stopRequested = false;

            Process p = new ProcessBuilder(application.getConfiguration().getCommands())
                    .redirectErrorStream(true)
                    .start();
            process = p;
            application.setState(State.RUNNING);

            Thread.ofPlatform()
                    .name("journal-" + application.getName())
                    .daemon(true)
                    .start(() -> watch(p));

        } catch (IOException e) {
            journal.write("Can't start process, " + e.getMessage());
            log.error("Can't start process", e);
            application.setState(State.FAILED);
            throw new ApplicationException(e.getMessage());
        }
    }

    private synchronized void watch(Process p) {
        try (var reader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) journal.write(line);
        } catch (IOException e) {
            journal.write("Read error: " + e.getMessage());
        }

        try {
            int code = p.waitFor();
            if (process != p) return;            // это уже старый процесс, состояние не трогаем
            if (stopRequested || code == 0) {
                journal.write("Process finished");
                application.setState(State.FINISHED);
            } else {
                journal.write("Process failed, exit code " + code);
                application.setState(State.FAILED);
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
        return process != null && process.isAlive();
    }

    private Long pid() {
        return process == null ? -1L : process.pid();
    }

    public AppSnapshot snapshot() {
        return new AppSnapshot(application, pid(), isAlive(), log());
    }

    public UUID id() {
        return application.getAppId();
    }

    public String name() {
        return application.getName();
    }

    public String profile() {
        return application.getConfiguration().getProfile();
    }

    public Collection<String> log() {
        return journal.log();
    }

    @Override
    public String toString() {
        return "ApplicationRuntime{" +
                "application=" + application +
                '}';
    }
}
