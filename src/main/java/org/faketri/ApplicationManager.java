package org.faketri;

import org.faketri.api.dto.Application;
import org.faketri.utils.CheckedPredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class ApplicationManager {

    private static final Logger log = LoggerFactory.getLogger(ApplicationManager.class);

    private final Set<Application> applications;

    public ApplicationManager() {
        this.applications = new HashSet<>();
    }

    public Collection<Application> getAll(){
        return new ArrayList<>(applications);
    }

    public Collection<Application> getByProfile(String profile){
        return applications.stream()
                .filter(a -> a.getConfiguration().getProfile().equalsIgnoreCase(profile))
                .toList();
    }

    public Application getByName(String name){
        return applications.stream()
                .filter(a -> a.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow();
    }

    public Application getByPid(long pid){
        return applications.stream()
            // skip not started process from list
            .filter(CheckedPredicate.unchecked(a -> a.getPid() == pid))
            .findFirst()
            .orElseThrow();
    }

    public void startAllByProfile(String profile) {
        getByProfile(profile).forEach(a -> {
            try {
                a.start();
            } catch (IOException e) {
                log.error("Cannot start application - {}\nError - {}", a.getName(), e.getMessage());
            }
        });
    }

    public void save(Application app){
        applications.add(app);
    }
}
