/**
 * A task with no date attached to it, for example {@code [T][ ] borrow book}.
 */
public class ToDo extends Task {

    /**
     * Creates a todo that starts out not done.
     *
     * @param description what the user has to do
     */
    public ToDo(String description) {
        super(description);
    }

    @Override
    public String toFileFormat() {
        return "T | " + super.toFileFormat();
    }

    @Override
    public String toString() {
        return "[T]" + super.toString();
    }
}
