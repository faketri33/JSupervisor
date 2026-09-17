package org.faketri.process;

import org.faketri.exceptions.ProcessAlreadyRunningException;
import org.faketri.exceptions.ProcessNotFindException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ProcessHandler {

    private static final Logger log = LoggerFactory.getLogger(ProcessHandler.class);
    private final ProcessBuilder processBuilder;
    private Process process;
    private volatile boolean listening = false;

    private final Set<Consumer<String>> inputListeners;
    private final Set<Consumer<String>> errListeners;

    public ProcessHandler(List<String> commands) {
        this.processBuilder = new ProcessBuilder(commands);
        this.processBuilder.redirectErrorStream(true);
        this.inputListeners = new HashSet<>();
        this.errListeners = new HashSet<>();
    }

    public void start() throws IOException {
        if (isAlive()) throw new ProcessAlreadyRunningException("Process already running");

        process = processBuilder.start();
        log.debug("Start process with pid {}", process.pid());
        neededStartListen();
        subscribeOnExit().thenAccept(p -> log.debug("Process with pid {} cancel work", p.pid()));
    }

    private void listen(){
        if (listening) return;
        listening = true;

        Thread.ofVirtual().start(() -> {
            try(InputStreamReader str = new InputStreamReader(process.getInputStream())){
                BufferedReader bf = new BufferedReader(str);
                String line;
                while ((line = bf.readLine()) != null)
                    for (var subscriber : inputListeners) subscriber.accept(line);
            } catch (IOException ex){
                errListeners.forEach(s -> s.accept(ex.getLocalizedMessage()));
            }finally {
                listening = false;
            }
        });
    }

    public void listen(Consumer<String> subscriber){
        inputListeners.add(subscriber);
        neededStartListen();
    }

    public void listen(Consumer<String> in, Consumer<String> err){
        inputListeners.add(in);
        errListeners.add(err);
        neededStartListen();
    }

    private void neededStartListen(){
        if (listening || process == null) return;
        boolean notHaveListener = inputListeners.isEmpty() && errListeners.isEmpty();
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

    public boolean isAlive(){
        return process != null && process.isAlive();
    }
}
