package org.faketri.utils;

import org.faketri.domain.parser.RootConfig;

import java.io.IOException;
import java.nio.file.Path;

public interface Parser {
    RootConfig parse(Path path) throws IOException;
}
