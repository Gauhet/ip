/**
 * A task the user has asked Alfred to remember, together with whether it has
 * been completed. Each kind of task is a subclass that puts its own type box,
 * such as {@code [T]}, in front of the display form defined here.
 */
public abstract class Task {
    private final String name;

    private boolean isDone;

    /**
     * Creates a task that starts out not done.
     *
     * @param name the description of the task
     */
    protected Task(String name) {
        this.name = name;
        this.isDone = false;
    }

    /** Marks this task as completed. */
    public void markDone() {
        isDone = true;
    }

    /** Marks this task as not completed. */
    public void unmarkDone() {
        isDone = false;
    }

    /**
     * Returns the status box and description, for example {@code [X] read book}.
     *
     * @return the display form of this task, without any type box
     */
    @Override
    public String toString() {
        if (isDone) {
            return "[X] " + name;
        } else {
            return "[ ] " + name;
        }
    }
}
