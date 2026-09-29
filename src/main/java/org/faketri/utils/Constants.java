package org.faketri.utils;

import org.faketri.domain.RestartPolicy;

public interface Constants {

    class Global {
        private Global(){}

        public static final String APP_NAME = ApplicationProperties.getProperties("app.name");
    }

    class ConfigurationConstants{
        private ConfigurationConstants(){}

        public static final int MAX_RESTART_COUNT = 1;
        public static final String DEFAULT_PROFILE = "DEFAULT";
        public static final RestartPolicy RESTART_POLICY = RestartPolicy.NEVER;
    }

    class UnixServerConfiguration{
        private UnixServerConfiguration(){}

        public static final String DIR_NAME = "jsup";
        public static final String SOCK_NAME = DIR_NAME + ".sock";

        public static final int MAX_PAYLOAD = 16 * 16 * 1024; // 256 kb
    }

    class HeaderConfiguration{
        private HeaderConfiguration(){}

        public static int VERSION = 1;
        public static final int MIN_SIZE = 9;
        public static final short MAGIC = (short) 0xC11D;
    }
}
