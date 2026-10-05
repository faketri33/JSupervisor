package org.faketri.infrastructure.process;

import org.faketri.infrastructure.exceptions.application.ApplicationException;
import org.faketri.infrastructure.exceptions.process.ProcessAlreadyRunningException;
import org.faketri.infrastructure.exceptions.process.ProcessException;
import org.faketri.infrastructure.exceptions.process.ProcessNotFindException;
import org.faketri.infrastructure.process.reader.ProcessReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ProcessHandler {

    private static final Logger log = LoggerFactory.getLogger(ProcessHandler.class);

    private final ProcessBuilder processBuilder;
    private final Set<ProcessReader> inputListeners;
    private final Set<ProcessErrorHandler> processErrorHandler;

    private Process process;

    private volatile boolean listening = false;

    public ProcessHandler(List<String> commands) {
        this.processBuilder = new ProcessBuilder(commands);
        this.processBuilder.redirectErrorStream(true);

        this.inputListeners = ConcurrentHashMap.newKeySet();
        this.processErrorHandler = ConcurrentHashMap.newKeySet();
    }

    public void start() throws ProcessException {
        if (isAlive()) throw new ProcessAlreadyRunningException("Process already running");

        try {
            process = processBuilder.start();
            log.debug("Start process with pid {}", process.pid());
            log.debug("commands {}", processBuilder.command());
        } catch (IOException ex) {
            processErrorHandler.forEach(h -> h.handelException(ex));
            throw new ApplicationException(ex.getMessage());
        }

        needToListen();

        subscribeOnExit()
                .thenAccept(p -> log.debug("Process with pid {} cancel work", p.pid()));
    }

    private void listen() {
        if (listening) return;
        listening = true;

        Thread.ofVirtual().start(() -> {
            try (BufferedReader bf = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = bf.readLine()) != null)
                    for (var subscriber : inputListeners)
                        subscriber.read(line);
            } catch (IOException ex) {
                processErrorHandler.forEach(err -> err.handelException(ex));
            } finally {
                for (var subscriber : inputListeners) {
                    try {
                        subscriber.close();
                    } catch (Exception e) {
                        log.error(e.getMessage());
                    }
                }

                inputListeners.clear();
                listening = false;
            }
        });
    }

    public void listen(ProcessReader pr) {
        inputListeners.add(pr);
        needToListen();
    }

    public void errHandle(ProcessErrorHandler err) {
        processErrorHandler.add(err);
    }

    private void needToListen() {
        if (listening || process == null) return;
        boolean notHaveListener = inputListeners.isEmpty() || processErrorHandler.isEmpty();
        if (notHaveListener) return;
        listen();
    }

    public CompletableFuture<Process> subscribeOnExit() throws ProcessNotFindException {
        if (process == null) throw new ProcessNotFindException("Process doesn't start");
        return process.onExit();
    }

    public long pid() throws ProcessNotFindException {
        if (process == null) throw new ProcessNotFindException("Process doesn't start");
        return process.pid();
    }

    public boolean isAlive() {
        return process != null && process.isAlive();
    }
}
