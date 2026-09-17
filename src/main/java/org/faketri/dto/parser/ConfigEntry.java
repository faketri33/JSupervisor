package org.faketri.dto.parser;

import org.faketri.dto.RestartPolicy;
import org.faketri.utils.Constants;

import java.util.List;

public class ConfigEntry {
    private List<String> command;
    private RestartPolicy restartPolicy = Constants.ConfigurationConstants.RESTART_POLICY;
    private int maxRestartCount = Constants.ConfigurationConstants.MAX_RESTART_COUNT;
    private String profile = Constants.ConfigurationConstants.DEFAULT_PROFILE;

    public List<String> getCommand() {
        return command;
    }

    public void setCommand(List<String> command) {
        this.command = command;
    }

    public RestartPolicy isRestart() {
        return restartPolicy;
    }

    public void setRestartPolicy(RestartPolicy restart) {
        this.restartPolicy = restart;
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
