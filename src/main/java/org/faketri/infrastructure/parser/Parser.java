package org.faketri.infrastructure.parser;

import java.io.IOException;
import java.nio.file.Path;

public interface Parser {
    RootConfig parse(Path path) throws IOException;
}
