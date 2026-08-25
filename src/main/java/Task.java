/**
 * A single task the user has asked Alfred to remember,
 * together with whether it has been completed.
 */
public class Task {
    /** Description of the task, fixed once the task is created. */
    private final String name;

    /** Whether the task has been completed; false until marked. */
    private boolean isDone;

    /**
     * Creates a task that starts out not done.
     *
     * @param name the description of the task
     */
    public Task(String name) {
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
     * Returns the task as it should appear in a list, with a status box
     * that holds an {@code X} when done and a space when not,
     * for example {@code [X] read book}.
     *
     * @return the display form of this task
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
