import java.time.LocalDate;
import java.util.List;

/**
 * A task that spans a stretch of days, for example
 * {@code [E][ ] project meeting (from: 2019-12-02 to: 2019-12-03)}.
 */
public class Event extends Task {
    /**
     * The days the event starts and ends on. Real dates, so that they can
     * later be compared or reformatted.
     */
    private final LocalDate from;

    private final LocalDate to;

    /**
     * Creates an event that starts out not done.
     *
     * @param description what the event is
     * @param from the day it starts
     * @param to the day it ends
     */
    public Event(String description, LocalDate from, LocalDate to) {
        super(description);
        this.from = from;
        this.to = to;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The start and the end each get their own field, rather than being run
     * together into one, so that reading the line back does not have to split
     * them apart again. Each is written in the {@code yyyy-mm-dd} form that
     * {@link LocalDate#parse(CharSequence)} reads back.
     */
    @Override
    public List<String> toFileFields() {
        List<String> fields = super.toFileFields();
        fields.add(0, "E");
        fields.add(from.toString());
        fields.add(to.toString());
        return fields;
    }

    @Override
    public String toString() {
        return "[E]" + super.toString() + " (from: " + from + " to: " + to + ")";
    }
}
