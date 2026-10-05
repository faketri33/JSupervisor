package org.faketri.infrastructure.process;

import org.faketri.domain.Application;
import org.faketri.infrastructure.exceptions.application.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class ApplicationProcessContainer {

    private static final Logger log = LoggerFactory.getLogger(ApplicationProcessContainer.class);

    private final Application application;
    private Process process;

    private final Set<String> journal;

    public ApplicationProcessContainer(Application application) {
        this.application = application;
        this.journal = ConcurrentHashMap.newKeySet();
    }

    public void start() throws ApplicationException {
        ProcessBuilder processBuilder = new ProcessBuilder();

        try {
            journal.clear();

            processBuilder.environment().putAll(System.getenv());

            process = processBuilder
                    .command(application.getConfiguration().getCommands())
                    .start();

            journal.add("Starting process");
            processListen();
        } catch (IOException e) {
            journal.add("Can't start process");
            log.error("Can't start process", e);
            throw new ApplicationException(e.getMessage());
        }
    }

    private void processListen(){
        try (var buffer = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = buffer.readLine()) != null) writeJournal(line);
        } catch (IOException e) {
            writeJournal(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void writeJournal(String journal) {
        this.journal.add(journal);
    }

    public void stop() {
        journal.add("Stopping process");
        process.destroy();
    }
}
