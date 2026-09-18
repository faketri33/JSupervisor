package org.faketri.utils;

import org.faketri.domain.RestartPolicy;

public interface Constants {
    class ConfigurationConstants{
        private ConfigurationConstants(){}

        public static final int MAX_RESTART_COUNT = 1;
        public static final String DEFAULT_PROFILE = "DEFAULT";
        public static final RestartPolicy RESTART_POLICY = RestartPolicy.NEVER;
    }
    class UnixServerConfiguration{
        private UnixServerConfiguration(){}

        public static final int PORT = ApplicationProperties.getPropertiesInt("server.port");
    }
}
