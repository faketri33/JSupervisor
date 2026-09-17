package org.faketri.process.reader;

import java.io.IOException;

public interface ProcessReader extends AutoCloseable {
    void read(String line) throws IOException;
}
