package org.faketri.process;

import org.faketri.exceptions.ProcessNotFindException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ProcessHandler {

    private final ProcessBuilder processBuilder;
    private Process process;
    private volatile boolean listening = false;

    private final Set<Consumer<String>> inputListeners;
    private final Set<Consumer<String>> errListeners;

    public ProcessHandler(String[] commands) {
        this.processBuilder = new ProcessBuilder(commands);
        this.inputListeners = new HashSet<>();
        this.errListeners = new HashSet<>();
    }

    public void start() throws IOException {
        if (isAlive()) throw new RuntimeException("Procees already running");

        process = processBuilder.start();
        listening = false;
        listen();
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
    }

    public void listen(Consumer<String> in, Consumer<String> err){
        inputListeners.add(in);
        errListeners.add(err);
    }

    public CompletableFuture<Process> subscribeOnExit(){
        if (process == null) throw new ProcessNotFindException("Process doesn't start");
        return process.onExit();
    }

    public long pid(){
        if (process == null) throw new ProcessNotFindException("Process doesn't start");
        return process.pid();
    }

    public boolean isAlive(){
        return process != null && process.isAlive();
    }
}
