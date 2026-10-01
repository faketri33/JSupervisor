package org.faketri;


import org.faketri.core.AppInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;


public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            log.error("No one argument present");
            return;
        }

        AppInstance.create(Path.of(args[0])).start();
    }
}