/**
 * A task that has to be finished by a stated time, such as
 * {@code return book} by {@code Sunday}.
 * A deadline is shown with a {@code [D]} type box in front of the status box
 * it inherits from {@link Task} and the due time after the description,
 * for example {@code [D][ ] return book (by: Sunday)}.
 */
public class Deadline extends Task {
    /**
     * When the task is due, kept as the words the user typed after
     * {@code /by} rather than as a parsed date, since nothing yet needs to
     * compare or sort by dates.
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

    /**
     * Returns the deadline as it should appear in a list: the display form
     * inherited from {@link Task} behind a {@code [D]} box, followed by the
     * due time in brackets,
     * for example {@code [D][X] return book (by: Sunday)}.
     *
     * @return the display form of this deadline
     */
    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + by + ")";
    }
}
