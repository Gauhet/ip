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
     * Returns the fields this task shares with every other kind, as the middle
     * of a save-file line, for example {@code 1 | read book}. The status is
     * written as a digit rather than a box, because the file is read by the
     * program rather than by a person.
     *
     * <p>Each subclass puts its own type letter in front of this and adds any
     * fields of its own after it, the same way {@link #toString()} is built up.
     *
     * @return the save-file form of this task, without a type letter
     */
    public String toFileFormat() {
        if (isDone) {
            return "1 | " + name;
        } else {
            return "0 | " + name;
        }
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
