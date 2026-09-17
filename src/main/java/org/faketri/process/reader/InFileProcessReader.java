package org.faketri.process.reader;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class InFileProcessReader implements ProcessReader {

    private final FileOutputStream file;

    public InFileProcessReader(Path path) throws IOException {
        Files.createDirectories(path.getParent());
        if (!path.toFile().exists()) Files.createFile(path);
        file = new FileOutputStream(path.toFile());
    }

    @Override
    public void read(String line) throws IOException {
        file.write(line.concat("\n").getBytes());
    }

    @Override
    public void close() throws Exception {
        if (file != null) file.close();
    }
}
