package org.faketri.process.reader;


import java.io.IOException;
import java.io.PrintStream;

public class ConsoleOutputProcessReader implements ProcessReader {
    private final PrintStream stream;

    public ConsoleOutputProcessReader(PrintStream output) {
        this.stream = output;
    }

    @Override
    public void read(String line) throws IOException {
        stream.println(line);
    }

    @Override
    public void close() throws Exception {
        if (stream != null) stream.flush();
    }
}
