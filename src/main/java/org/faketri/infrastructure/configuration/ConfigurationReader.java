package org.faketri.infrastructure.configuration;

import org.faketri.domain.Application;

import java.io.IOException;
import java.util.Collection;

public interface ConfigurationReader {
    Collection<Application> read() throws IOException;
}
