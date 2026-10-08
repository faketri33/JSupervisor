package org.faketri.infrastructure.process.journal;

import java.util.Collection;

/**
 * Represents a journal associated with a process lifecycle.
 *
 * <p>A journal receives output produced by the process and defines how
 * that output is recorded. Implementations may store the output in memory,
 * persist it to a file, filter specific types of output, or use another
 * storage strategy.</p>
 *
 * <p>The journal is intended to be attached to a process for the duration
 * of its lifecycle.</p>
 */
public interface Journal {

    /**
     * Records a line of output produced by the process.
     *
     * <p>The implementation defines how the line is handled and where
     * it is stored.</p>
     *
     * @param line a line of process output
     */
    void write(String line);

    /**
     * Clears the journal.
     *
     * <p>The effect of this operation depends on the implementation.
     * For example, an in-memory journal may discard all stored entries,
     * while a persistent journal may truncate or remove its underlying
     * storage.</p>
     */
    void clear();

    /**
     * Returns the output recorded by this journal.
     *
     * <p>The returned collection represents the journal contents available
     * through this implementation. Implementations may define their own
     * ordering and storage semantics.</p>
     *
     * @return recorded process output
     */
    Collection<String> log();

    String toString();
}