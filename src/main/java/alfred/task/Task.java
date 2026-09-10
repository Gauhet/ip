package alfred.task;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import alfred.Storage;

/**
 * A task the user has asked Alfred to remember, together with whether it has
 * been completed and how much it matters. Each kind of task is a subclass that
 * puts its own type box, such as {@code [T]}, in front of the display form
 * defined here.
 */
public abstract class Task {
    /** What the user has to do, in the words they described it in. */
    private final String name;

    /** Whether the task has been completed. */
    private boolean isDone;

    /** How much the task matters. */
    private Priority priority = Priority.NONE;

    /**
     * Creates a task that starts out not done.
     *
     * @param name the description of the task.
     */
    protected Task(String name) {
        this.name = name;
        this.isDone = false;
    }

    /**
     * Tells whether this task falls on the given day.
     *
     * <p>A task with no date falls on no day, which is the answer inherited by
     * {@link ToDo}. The kinds that carry a date override it.
     *
     * @param date the day being asked about.
     * @return true if this task falls on that day.
     */
    public boolean occursOn(LocalDate date) {
        return false;
    }

    /**
     * Tells whether this task's description contains the given keyword, ignoring
     * the difference between uppercase and lowercase.
     *
     * <p>Only the description is searched, and a keyword matches anywhere inside
     * it, so that {@code find book} also finds {@code bookshop}.
     *
     * @param keyword the text being searched for.
     * @return true if the description contains it.
     */
    public boolean matches(String keyword) {
        return name.toLowerCase().contains(keyword.toLowerCase());
    }

    /** Marks this task as completed. */
    public void markDone() {
        isDone = true;
    }

    /** Marks this task as not completed. */
    public void unmarkDone() {
        isDone = false;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Priority getPriority() {
        return priority;
    }

    /**
     * Returns this task's fields, in the order they are saved: the type letter,
     * the status, the description, whatever the kind of task carries of its own,
     * and last of all the priority, if it has one.
     *
     * <p>The fields are returned separately rather than joined into a line, so
     * that only {@link Storage} knows what separates them.
     *
     * @return a list holding this task's fields, in the order they are saved.
     */
    public abstract List<String> toFileFields();

    /**
     * Returns the fields of one save line: the type letter, then the status and
     * the description that every task saves, then any fields this kind adds of
     * its own, then the priority. The status is a digit rather than a box,
     * because the file is read by the program rather than by a person.
     *
     * <p>The extra fields are varargs because each kind has a different number
     * of them: none for a todo, one for a deadline, two for an event.
     *
     * <p>The priority goes last and is left out when there is none, so a list
     * without priorities saves as the file earlier versions wrote.
     *
     * @param type the letter naming the kind of task, such as {@code D}.
     * @param extraFields the fields this kind adds after the description, in the
     *        order they are saved, and none for a kind that adds none.
     * @return an unmodifiable list holding the whole line's fields, in order.
     */
    protected List<String> buildFileFields(String type, String... extraFields) {
        // Storage tells one kind of task from another by this single letter, so
        // a longer one would go into a line that could never be read back. Only
        // the subclasses call this, so a wrong letter is a fault here.
        assert type.length() == 1 : "a save line's type is one letter, not '" + type + "'";

        Stream<String> fields = Stream.concat(Stream.of(type, isDone ? "1" : "0", name),
                Arrays.stream(extraFields));
        if (priority != Priority.NONE) {
            fields = Stream.concat(fields, Stream.of(priority.name()));
        }
        return fields.toList();
    }

    /**
     * Returns the status box, the priority box if the task has one, and the
     * description, for example {@code [X][HIGH] read book}.
     *
     * @return the display form of this task, without any type box.
     */
    @Override
    public String toString() {
        return (isDone ? "[X]" : "[ ]") + priority.box() + " " + name;
    }
}
