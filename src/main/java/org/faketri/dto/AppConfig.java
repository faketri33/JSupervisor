package org.faketri.dto;


import java.util.ArrayList;
import java.util.List;

public class AppConfig {

    private final List<String> commands;

    private final RestartPolicy restartPolicy;
    private final int maxRestart;
    private final String profile;

    private AppConfig(Builder builder){
        commands = builder.commands;
        restartPolicy = builder.restartPolicy;
        maxRestart = builder.maxRestart;
        profile = builder.profile;
    }

    public List<String>  getCommands() {
        return commands;
    }

    public RestartPolicy isRestartable() {
        return restartPolicy;
    }

    public int getMaxRestart() {
        return maxRestart;
    }

    public String getProfile() {
        return profile;
    }

    public static class Builder {
        private final List<String> commands = new ArrayList<>();
        private RestartPolicy restartPolicy;
        private int maxRestart;
        private String profile;

        public Builder commands(String command){
            this.commands.add(command);
            return this;
        }

        public Builder commands(List<String> command){
            this.commands.addAll(command);
            return this;
        }


        public Builder setRestartable(RestartPolicy restartable){
            this.restartPolicy = restartable;
            return this;
        }

        public Builder maxRestartCount(int maxRestart){
            this.maxRestart = maxRestart;
            return this;
        }

        public Builder profile(String profile){
            this.profile = profile;
            return this;
        }

        public AppConfig build(){
            return new AppConfig(this);
        }
    }
}
