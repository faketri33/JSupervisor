package org.faketri.api.dto;

import org.faketri.utils.Constants;

public class ApplicationConfiguration {

    private String[] commands;

    private boolean restartable = Constants.ConfigurationConstants.CAN_RESTART;
    private int maxRestart = Constants.ConfigurationConstants.MAX_RESTART_COUNT;
    private String profile = Constants.ConfigurationConstants.DEFAULT_PROFILE;

    public ApplicationConfiguration(String[] commands){
        this.commands = commands;
    }

    public ApplicationConfiguration(String[] commands, boolean restartable, int maxRestart, String profile) {
        this.commands = commands;
        this.restartable = restartable;
        this.maxRestart = maxRestart;
        this.profile = profile;
    }

    public String[] getCommands() {
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
}
