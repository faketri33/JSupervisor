package org.faketri.notification;


import org.faketri.utils.config.ApplicationProperties;
import org.faketri.utils.config.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class NotificationSystem {

    private static final Logger log = LoggerFactory.getLogger(NotificationSystem.class);
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private static final AtomicBoolean isLoad = new AtomicBoolean(false);

    static {
        log.debug("Loading native notification library...");
        var f = scheduler.submit(() -> {
            System.load(ApplicationProperties.getProperties("native.path"));
            isLoad.set(true);
        });
        interruptSchedule(f, 20);
        log.debug("Native notification library loaded");
    }

    private NotificationSystem() {
    }

    public static void notify(String message) {
        if (isLoad.get()) return;
        log.debug("Start notify");
        var future = scheduler.submit(() -> notify(Constants.Global.APP_NAME, message));
        interruptSchedule(future, 3);
    }

    private static void interruptSchedule(Future<?> future, int timeout) {
        scheduler.schedule(() -> {
            future.cancel(true);
            log.debug("Interrupt notify");
        }, timeout, TimeUnit.SECONDS);

        scheduler.schedule(scheduler::shutdown, 4, TimeUnit.SECONDS);
    }

    private static native void notify(String title, String message);
}
