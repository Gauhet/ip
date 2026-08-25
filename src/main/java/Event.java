/**
 * A task that spans a stretch of time, such as {@code project meeting}
 * running from {@code Mon 2pm} to {@code 4pm}.
 * An event is shown with an {@code [E]} type box in front of the status box
 * it inherits from {@link Task} and its start and end after the description,
 * for example {@code [E][ ] project meeting (from: Mon 2pm to: 4pm)}.
 */
public class Event extends Task {
    /**
     * When the event starts, kept as the words the user typed after
     * {@code /from} rather than as a parsed date, since nothing yet needs to
     * compare or sort by dates.
     */
    private final String from;

    /** When the event ends, kept as typed for the same reason as {@link #from}. */
    private final String to;

    /**
     * Creates an event that starts out not done.
     *
     * @param description what the event is
     * @param from when it starts, in the user's own words
     * @param to when it ends, in the user's own words
     */
    public Event(String description, String from, String to) {
        super(description);
        this.from = from;
        this.to = to;
    }

    /**
     * Returns the event as it should appear in a list: the display form
     * inherited from {@link Task} behind an {@code [E]} box, followed by the
     * start and end times in brackets,
     * for example {@code [E][X] project meeting (from: Mon 2pm to: 4pm)}.
     *
     * @return the display form of this event
     */
    @Override
    public String toString() {
        return "[E]" + super.toString() + " (from: " + from + " to: " + to + ")";
    }
}
