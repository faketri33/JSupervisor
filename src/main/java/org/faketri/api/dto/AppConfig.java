package org.faketri.api.dto;


import java.util.ArrayList;
import java.util.List;

public class AppConfig {

    private final List<String> commands;

    private final boolean restartable;
    private final int maxRestart;
    private final String profile;

    private AppConfig(Builder builder){
        commands = builder.commands;
        restartable = builder.restartable;
        maxRestart = builder.maxRestart;
        profile = builder.profile;
    }

    public List<String>  getCommands() {
        return commands;
    }

    public boolean isRestartable() {
        return restartable;
    }

    public int getMaxRestart() {
        return maxRestart;
    }

    public String getProfile() {
        return profile;
    }

    public static class Builder {
        private final List<String> commands = new ArrayList<>();
        private boolean restartable;
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


        public Builder setRestartable(boolean restartable){
            this.restartable = restartable;
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
