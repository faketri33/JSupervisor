package org.faketri;

import org.faketri.api.dto.Application;

import java.io.IOException;
import java.util.*;

public class Main {

    static Set<Application> applicationManagers = new HashSet<>();

    public static void main(String[] args) throws IOException, InterruptedException {
        Application application = Application.of("My App", new String[]{"ls"});
        application.listen(System.out::println, System.out::println);

        startAndWatch(application);
        Thread.currentThread().join();
    }

    private static void startAndWatch(Application proc) throws IOException {
        applicationManagers.add(proc);

        proc.start();
        proc.subscribe(Main::reboot);

        System.out.println("test");
    }

    private static void reboot(Process p){
        System.out.println("Set size " + applicationManagers.size());
        long oldPid = p.pid();

        System.out.println("Process " + oldPid + " exited with value " + p.exitValue());

        Application app = applicationManagers.stream()
                .filter(a -> a.getPid() == oldPid)
                .findFirst()
                .orElseThrow();

        try {
            Thread.sleep(100);
            startAndWatch(app);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }
}