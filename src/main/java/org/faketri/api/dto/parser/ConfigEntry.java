package org.faketri.api.dto.parser;

import org.faketri.utils.Constants;

import java.util.List;

public class ConfigEntry {
    private List<String> command;
    private boolean restart = Constants.ConfigurationConstants.CAN_RESTART;
    private int maxRestartCount = Constants.ConfigurationConstants.MAX_RESTART_COUNT;
    private String profile = Constants.ConfigurationConstants.DEFAULT_PROFILE;

    public List<String> getCommand() {
        return command;
    }

    public void setCommand(List<String> command) {
        this.command = command;
    }

    public boolean isRestart() {
        return restart;
    }

    public void setRestart(boolean restart) {
        this.restart = restart;
    }

    public int getMaxRestartCount() {
        return maxRestartCount;
    }

    public void setMaxRestartCount(int maxRestartCount) {
        this.maxRestartCount = maxRestartCount;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }
}
