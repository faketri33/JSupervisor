package org.faketri.dto;

import org.faketri.process.ProcessHandler;
import org.faketri.process.reader.ProcessReader;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class Application {

    private final UUID id;
    private final String name;
    private final ProcessHandler processHandler;
    private final AppConfig configuration;
    private State state;

    private Application(String name, AppConfig configuration, State state) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.configuration = configuration;
        processHandler = new ProcessHandler(configuration.getCommands());
        changeStatus(state);
    }

    public static Application of(String name, List<String> commands){
        return new Application(name, new AppConfig.Builder().commands(commands).build(), State.PENDING);
    }

    public static Application of(String name, AppConfig conf){
        return new Application(name, conf, State.PENDING);
    }

    public UUID getAppId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getPid(){
        return processHandler.pid();
    }

    public void start() throws IOException {
        processHandler.start();
        changeStatus(State.RUNNING);

        subscribe(p -> changeStatus(p.exitValue() != 0 ? State.FAILED : State.FINISHED));
    }

    public void listen(ProcessReader pr){
        processHandler.listen(pr);
    }

    public void subscribe(Consumer<? super Process> consumer){
        processHandler.subscribeOnExit().thenAccept(consumer);
    }

    private void changeStatus(State state){
        this.state = state;
    }

    public State getState() {
        return state;
    }

    public AppConfig getConfiguration() {
        return configuration;
    }

    public boolean isAlive(){
        return processHandler.isAlive();
    }
}
