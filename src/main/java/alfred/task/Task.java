package alfred.task;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import alfred.Storage;

/**
 * A task the user has asked Alfred to remember, together with whether it has
 * been completed. Each kind of task is a subclass that puts its own type box,
 * such as {@code [T]}, in front of the display form defined here.
 */
public abstract class Task {
    /** What the user has to do, in the words they described it in. */
    private final String name;

    /** Whether the task has been completed, the one thing about it that changes. */
    private boolean isDone;

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

    /**
     * Returns this task's fields, in the order they are saved: the type letter,
     * the status, the description, and then whatever the kind of task carries of
     * its own.
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
     * its own. The status is a digit rather than a box, because the file is read
     * by the program rather than by a person.
     *
     * <p>The extra fields are varargs because each kind has a different number
     * of them: none for a todo, one for a deadline, two for an event.
     *
     * @param type the letter naming the kind of task, such as {@code D}.
     * @param extraFields the fields this kind adds after the description, in the
     *        order they are saved, and none for a kind that adds none.
     * @return a mutable list holding the whole line's fields, in order.
     */
    protected List<String> buildFileFields(String type, String... extraFields) {
        List<String> fields = new ArrayList<>();
        fields.add(type);
        fields.add(isDone ? "1" : "0");
        fields.add(name);
        fields.addAll(List.of(extraFields));
        return fields;
    }

    /**
     * Returns the status box and description, for example {@code [X] read book}.
     *
     * @return the display form of this task, without any type box.
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
