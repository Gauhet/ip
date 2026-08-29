import java.util.List;

/**
 * A task that has to be finished by a stated time, for example
 * {@code [D][ ] return book (by: Sunday)}.
 */
public class Deadline extends Task {
    /**
     * When the task is due, kept as the words the user typed rather than as a
     * parsed date, since nothing yet needs to compare or sort by dates.
     */
    private final String by;

    /**
     * Creates a deadline that starts out not done.
     *
     * @param description what the user has to do
     * @param by when it has to be done by, in the user's own words
     */
    public Deadline(String description, String by) {
        super(description);
        this.by = by;
    }

    @Override
    public List<String> toFileFields() {
        List<String> fields = super.toFileFields();
        fields.add(0, "D");
        fields.add(by);
        return fields;
    }

    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + by + ")";
    }
}
