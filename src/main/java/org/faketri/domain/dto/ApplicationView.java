package org.faketri.domain.dto;

import org.faketri.domain.AppConfig;
import org.faketri.domain.State;

import java.util.Collection;
import java.util.UUID;

public record ApplicationView(
        UUID appId,
        String name,
        State state,
        Long pid,
        boolean alive,
        AppConfig configuration,
        Collection<String> journal) {
}