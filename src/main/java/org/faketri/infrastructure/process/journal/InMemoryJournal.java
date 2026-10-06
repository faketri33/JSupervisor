package org.faketri.infrastructure.process.journal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * An in-memory implementation of {@link Journal}.
 *
 * <p>The journal stores process output in memory for the lifetime of the
 * process. It has a maximum capacity. When the capacity is reached,
 * the oldest log entry is removed before a new entry is added.</p>
 *
 * <p>This implementation therefore keeps only the most recent log entries
 * up to the configured capacity.</p>
 */
public class InMemoryJournal implements Journal {

    private static final Logger log = LoggerFactory.getLogger(InMemoryJournal.class);

    private static final int MAX_CAPACITY = 200;

    private final Queue<String> lines = new ConcurrentLinkedQueue<>();

    public void write(String line) {
        if (lines.size() >= MAX_CAPACITY) {
            log.debug("Journal size {}, remove first element", lines.size());
            lines.poll();
        }
        lines.add(line);
        log.debug("Line write {}", line);
    }

    public Collection<String> log() {
        return Collections.unmodifiableCollection(lines);
    }

    public void clear() {
        log.debug("Current {} size, clear all element.", lines.size());
        lines.clear();
    }

}
