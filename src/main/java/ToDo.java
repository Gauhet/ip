/**
 * A task with no date attached to it, such as {@code borrow book}.
 * A todo is shown with a {@code [T]} type box in front of the status box
 * it inherits from {@link Task}, for example {@code [T][ ] borrow book}.
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

    /**
     * Returns the todo as it should appear in a list, which is the display
     * form inherited from {@link Task} behind a {@code [T]} box,
     * for example {@code [T][X] read book}.
     *
     * @return the display form of this todo
     */
    @Override
    public String toString() {
        return "[T]" + super.toString();
    }
}
