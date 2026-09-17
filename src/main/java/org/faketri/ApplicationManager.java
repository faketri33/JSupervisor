package org.faketri;

import org.faketri.api.dto.Application;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ApplicationManager {
    private final Set<Application> applications;

    public ApplicationManager() {
        this.applications = new HashSet<>();
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

    public void save(Application app){
        applications.add(app);
    }
}
