package alfred.task;

import java.time.LocalDate;
import java.util.List;

import alfred.Dates;

/**
 * A task that spans a stretch of days, for example
 * {@code [E][ ] project meeting (from: Dec 02 2019 to: Dec 03 2019)}.
 */
public class Event extends Task {
    /** The days the event starts and ends on. Real dates, so that they can be compared. */
    private final LocalDate from;

    private final LocalDate to;

    /**
     * Creates an event that starts out not done.
     *
     * @param description what the event is.
     * @param from the day it starts.
     * @param to the day it ends.
     */
    public Event(String description, LocalDate from, LocalDate to) {
        super(description);
        this.from = from;
        this.to = to;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The start and the end each get their own field, so that reading the
     * line back does not have to split them apart again.
     */
    @Override
    public List<String> toFileFields() {
        return buildFileFields("E", from.toString(), to.toString());
    }

    /**
     * {@inheritDoc}
     *
     * <p>An event falls on every day it spans, its first and its last included,
     * so that asking about a day in the middle of a long event finds it.
     */
    @Override
    public boolean occursOn(LocalDate date) {
        return !date.isBefore(from) && !date.isAfter(to);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Both dates are shown in the reader's form.
     */
    @Override
    public String toString() {
        return "[E]" + super.toString()
                + " (from: " + Dates.format(from)
                + " to: " + Dates.format(to) + ")";
    }
}
