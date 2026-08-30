package alfred.task;

import java.time.LocalDate;
import java.util.List;

import alfred.Dates;

/**
 * A task that has to be finished by a stated date, for example
 * {@code [D][ ] return book (by: Oct 15 2019)}.
 */
public class Deadline extends Task {
    /** The day the task is due. A real date, so that it can be compared and reformatted. */
    private final LocalDate by;

    /**
     * Creates a deadline that starts out not done.
     *
     * @param description what the user has to do
     * @param by the day it has to be done by
     */
    public Deadline(String description, LocalDate by) {
        super(description);
        this.by = by;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The date is written in the {@code yyyy-mm-dd} form that
     * {@link LocalDate#toString()} produces, which is the same form
     * {@link LocalDate#parse(CharSequence)} reads, so the field survives the
     * round trip through the save file without a format having to be agreed
     * separately at each end.
     */
    @Override
    public List<String> toFileFields() {
        List<String> fields = super.toFileFields();
        fields.add(0, "D");
        fields.add(by.toString());
        return fields;
    }

    /**
     * {@inheritDoc}
     *
     * <p>A deadline falls on the single day it is due.
     */
    @Override
    public boolean occursOn(LocalDate date) {
        return by.equals(date);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The due date is shown in the reader's form, not the one it was typed
     * or saved in, which is what having stored a real date rather than the
     * user's words makes possible.
     */
    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + Dates.format(by) + ")";
    }
}
