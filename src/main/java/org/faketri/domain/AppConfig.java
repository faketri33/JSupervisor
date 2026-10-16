package org.faketri.domain;


import org.faketri.utils.config.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record AppConfig(List<String> commands, RestartPolicy restartPolicy, int maxRestart, String profile) {

    public AppConfig(List<String> commands, RestartPolicy restartPolicy, int maxRestart, String profile) {
        this.commands = List.copyOf(commands);
        this.restartPolicy = restartPolicy;
        this.maxRestart = maxRestart;
        this.profile = profile;
    }

    private AppConfig(Builder builder) {
        this(builder.commands, builder.restartPolicy, builder.maxRestart, builder.profile);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AppConfig(List<String> commands1, RestartPolicy policy, int restart, String profile1)))
            return false;

        return maxRestart == restart
                && commands.equals(commands1)
                && restartPolicy == policy
                && Objects.equals(profile, profile1);
    }

    @Override
    public int hashCode() {
        int result = commands.hashCode();
        result = 31 * result + Objects.hashCode(restartPolicy);
        result = 31 * result + Integer.hashCode(maxRestart);
        result = 31 * result + Objects.hashCode(profile);
        return result;
    }

    public static class Builder {
        private final List<String> commands = new ArrayList<>();
        private RestartPolicy restartPolicy = Constants.ConfigurationConstants.RESTART_POLICY;
        private int maxRestart = Constants.ConfigurationConstants.MAX_RESTART_COUNT;
        private String profile = Constants.ConfigurationConstants.DEFAULT_PROFILE;

        public Builder commands(String command) {
            this.commands.add(command);
            return this;
        }

        public Builder commands(List<String> command) {
            this.commands.addAll(command);
            return this;
        }


        public Builder setRestartable(RestartPolicy restartable) {
            this.restartPolicy = restartable;
            return this;
        }

        public Builder maxRestartCount(int maxRestart) {
            this.maxRestart = maxRestart;
            return this;
        }

        public Builder profile(String profile) {
            this.profile = profile;
            return this;
        }

        public AppConfig build() {
            return new AppConfig(this);
        }
    }
}
