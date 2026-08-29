import java.util.List;

/**
 * A task that spans a stretch of time, for example
 * {@code [E][ ] project meeting (from: Mon 2pm to: 4pm)}.
 */
public class Event extends Task {
    /** When the event starts and ends, kept as the words the user typed rather than as parsed dates. */
    private final String from;

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
     * {@inheritDoc}
     *
     * <p>The start and the end each get their own field, rather than being run
     * together into one, so that reading the line back does not have to split
     * them apart again.
     */
    @Override
    public List<String> toFileFields() {
        List<String> fields = super.toFileFields();
        fields.add(0, "E");
        fields.add(from);
        fields.add(to);
        return fields;
    }

    @Override
    public String toString() {
        return "[E]" + super.toString() + " (from: " + from + " to: " + to + ")";
    }
}
