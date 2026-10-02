package org.faketri.infrastructure.process.reader;


import java.io.PrintStream;

public class ConsoleOutputProcessReader implements ProcessReader {
    private final PrintStream stream;

    public ConsoleOutputProcessReader(PrintStream output) {
        this.stream = output;
    }

    @Override
    public void read(String line) {
        stream.println(line);
    }

    @Override
    public void close() {
        if (stream != null) stream.flush();
    }
}
