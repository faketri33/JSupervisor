package org.faketri.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationSystem {

    private static final Logger log = LoggerFactory.getLogger(NotificationSystem.class);

    static {
        log.debug("Loading native notification library...");
        System.load(ApplicationProperties.getProperties("native.path"));
        log.debug("Native notification library loaded");
    }

    private NotificationSystem() {}

    public static native void notify(String title, String message);
}
