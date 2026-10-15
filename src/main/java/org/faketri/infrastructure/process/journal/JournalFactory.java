package org.faketri.infrastructure.process.journal;

public final class JournalFactory {
    private JournalFactory() {
    }

    public static Journal defaultInstance() {
        return new InMemoryJournal();
    }
}
