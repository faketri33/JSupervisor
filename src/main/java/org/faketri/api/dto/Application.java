package org.faketri.api.dto;

import org.faketri.process.ProcessHandler;

import java.io.IOException;
import java.util.UUID;
import java.util.function.Consumer;

public class Application {

    private final AppId appId;
    private String name;
    private final ProcessHandler processHandler;
    private final ApplicationConfiguration configuration;
    private State state;


    private Application(String name, ApplicationConfiguration configuration, State state) {
        this.appId = new AppId();
        this.name = name;
        this.configuration = configuration;
        processHandler = new ProcessHandler(configuration.getCommands());
        changeStatus(state);
    }

    public static Application of(String name, String[] commands){
        return new Application(name, new ApplicationConfiguration(commands), State.PENDING);
    }

    public static Application of(String name, ApplicationConfiguration conf){
        return new Application(name, conf, State.PENDING);
    }

    public UUID getAppId() {
        return appId.getId();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getPid(){
        return processHandler.pid();
    }

    public void start() throws IOException {
        processHandler.start();
        changeStatus(State.RUNNING);

        subscribe(p -> {
            if (p.exitValue() != 0) changeStatus(State.FAILED);
            changeStatus(State.FINISHED);
        });
    }

    public void listen(Consumer<String> in){
        processHandler.listen(in);
    }

    public void listen(Consumer<String> in, Consumer<String> err){
        processHandler.listen(in, err);
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

    public ApplicationConfiguration getConfiguration() {
        return configuration;
    }

    public boolean isAlive(){
        return processHandler.isAlive();
    }
}
