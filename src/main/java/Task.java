import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A task the user has asked Alfred to remember, together with whether it has
 * been completed. Each kind of task is a subclass that puts its own type box,
 * such as {@code [T]}, in front of the display form defined here.
 */
public abstract class Task {
    /**
     * How a date is written when it is shown to the user, for example
     * {@code Oct 15 2019}.
     *
     * <p>Deliberately not the {@code yyyy-mm-dd} form the user types and the
     * save file holds. The two forms answer to different readers: the file
     * needs one that reads back exactly, and a person reading the list wants a
     * month named rather than numbered. Keeping a date as a {@code LocalDate}
     * rather than as text is what allows both at once.
     *
     * <p>The locale is pinned to English so the month name does not change
     * with the computer the program runs on. Left to the default locale, the
     * same task would show {@code Oct} on one machine and {@code okt} on
     * another, and the text UI tests would pass or fail by machine rather than
     * by behavior.
     *
     * <p>It lives here rather than separately in {@link Deadline} and
     * {@link Event} so that the two cannot drift apart. A class of its own
     * would be worth it once there is more than one format to keep.
     */
    protected static final DateTimeFormatter DISPLAY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMM dd yyyy", Locale.ENGLISH);

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
