import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * Tells whether this task falls on the given day.
     *
     * <p>A task with no date attached to it falls on no day, which is the
     * answer given here and inherited by {@link ToDo}. The kinds that carry a
     * date override it.
     *
     * <p>Asking the task rather than testing what kind it is keeps the decision
     * next to the dates it is made from. A chain of {@code instanceof} checks
     * in the caller would work too, but it would sit far from those dates and
     * would have to be found and extended by hand whenever a new kind of task
     * is added, which is the kind of edit that gets missed.
     *
     * @param date the day being asked about
     * @return true if this task falls on that day
     */
    public boolean occursOn(LocalDate date) {
        return false;
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
     * Returns the fields this task shares with every other kind, in the order
     * they are saved: the status, then the description. The status is a digit
     * rather than a box, because the file is read by the program rather than by
     * a person.
     *
     * <p>Each subclass puts its own type letter at the front and adds any fields
     * of its own at the end, the same way {@link #toString()} is built up.
     *
     * <p>The fields are returned separately rather than already joined into a
     * line, so that only {@link Storage} knows what separates them and how a
     * field containing that separator is escaped. Were the joining done here, it
     * would have to be kept in step with the code that splits the line back up,
     * in another file.
     *
     * @return a mutable list holding this task's shared fields
     */
    public List<String> toFileFields() {
        List<String> fields = new ArrayList<>();
        if (isDone) {
            fields.add("1");
        } else {
            fields.add("0");
        }
        fields.add(name);
        return fields;
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
