package alfred.task;

import java.util.List;

/**
 * A task with no date attached to it, for example {@code [T][ ] borrow book}.
 */
public class ToDo extends Task {

    /**
     * Creates a todo that starts out not done.
     *
     * @param description what the user has to do.
     */
    public ToDo(String description) {
        super(description);
    }

    /**
     * {@inheritDoc}
     *
     * <p>A todo carries no dates, so it adds nothing at the end.
     */
    @Override
    public List<String> toFileFields() {
        return buildFileFields("T");
    }

    @Override
    public String toString() {
        return "[T]" + super.toString();
    }
}
