package org.faketri.domain;

public enum RestartPolicy {
    NEVER {
        public boolean shouldRestart(int exitCode) {
            return false;
        }
    },
    ON_FAILURE {
        public boolean shouldRestart(int exitCode) {
            return exitCode != 0;
        }
    },
    ALWAYS {
        public boolean shouldRestart(int exitCode) {
            return true;
        }
    };

    public abstract boolean shouldRestart(int exitCode);
}
