package org.faketri.utils;


public class NotificationSystem {

    static {
        System.err.println("Loading native notification library...");
        System.load("/Users/vilkov/projects/JSupervisor/src/main/native/build/libnotify_native.dylib");
        System.err.println("Native notification library loaded");
    }

    private NotificationSystem() {}

    public static native void notify(String title, String message);
}
