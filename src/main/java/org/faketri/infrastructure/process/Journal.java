package org.faketri.infrastructure.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Journal {
    private static final Logger log = LoggerFactory.getLogger(Journal.class);

    private static final int MAX_CAPACITY = 200;

    private final Queue<String> lines = new ConcurrentLinkedQueue<>();

    public void write(String line){
        if (lines.size() >= MAX_CAPACITY) {
            log.debug("Journal size {}, remove first element", lines.size());
        }
        lines.add(line);
        log.debug("Line write {}", line);
    }

    public List<String> getJournal(){
        return new ArrayList<>(lines);
    }

    public void clear(){
        log.debug("Current {} size, clear all element.", lines.size());
        lines.clear();
    }

}
