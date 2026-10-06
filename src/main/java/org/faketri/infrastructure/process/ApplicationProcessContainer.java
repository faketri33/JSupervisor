package org.faketri.infrastructure.process;

import org.faketri.domain.Application;
import org.faketri.domain.State;
import org.faketri.infrastructure.exceptions.application.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public final class ApplicationProcessContainer {

    private static final Logger log = LoggerFactory.getLogger(ApplicationProcessContainer.class);

    private final Application application;
    private Process process;

    private final Journal journal;

    public ApplicationProcessContainer(Application application, Journal journal) {
        this.application = application;
        this.journal = journal;
    }

    public void start() throws ApplicationException {
        ProcessBuilder processBuilder = new ProcessBuilder();

        try {
            journal.clear();
            journal.write("Starting process");

            processBuilder.environment().putAll(System.getenv());

            process = processBuilder
                    .command(application.getConfiguration().getCommands())
                    .start();
            application.setState(State.RUNNING);

            process.onExit().thenAccept(p -> {
                if (p.exitValue() == 0) {
                    journal.write("Process finished");
                    application.setState(State.FINISHED);
                } else {
                    journal.write("Process failed");
                    application.setState(State.FAILED);
                }
            });

            processListen();
        } catch (IOException e) {
            journal.write("Can't start process, " + e.getMessage());
            log.error("Can't start process", e);
            application.setState(State.FAILED);
            throw new ApplicationException(e.getMessage());
        }
    }

    private void processListen(){
        try (var buffer = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = buffer.readLine()) != null) journal.write(line);
        } catch (IOException e) {
            journal.write(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        journal.write("Stopping process");
        process.destroy();
        application.setState(State.FINISHED);
    }

    public Process getProcess() {
        return process;
    }

    public Application getApplication() {
        return application;
    }

    public List<String> journal(){
        return journal.getJournal();
    }
}
