package org.faketri.domain.dto;

import org.faketri.domain.State;

import java.util.Collection;
import java.util.UUID;

public record AppSnapshot(
        UUID appId,
        String name,
        State state,
        String profile,
        Long pid,
        boolean alive,
        Collection<String> journal
) {
}
