package alfred.task;

import java.time.LocalDate;
import java.util.List;

import alfred.Dates;

/**
 * Represents a task that spans a stretch of days, for example
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
     * <p>Two events are the same task only if they start and end on the same
     * days.
     */
    @Override
    public boolean isSameTask(Task other) {
        // The parent's check comes first, so that the cast below is only
        // reached once the other task is known to be an event. Cast before
        // the check, a todo or a deadline compared with this event would
        // throw rather than merely differ.
        if (!super.isSameTask(other)) {
            return false;
        }
        Event otherEvent = (Event) other;
        return from.equals(otherEvent.from) && to.equals(otherEvent.to);
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
