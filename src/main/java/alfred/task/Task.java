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

    /**
     * Whether the task has been completed. The one thing about a task that
     * changes after it is made, which is why it is the one field that is not
     * final.
     */
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

    /**
     * Tells whether this task's description contains the given keyword,
     * ignoring the difference between uppercase and lowercase.
     *
     * <p>Case is ignored because the user is searching, not quoting: someone
     * looking for {@code book} means the word, not one particular spelling of
     * it, and a search that answers "nothing found" over a capital letter reads
     * as a fault rather than as an answer.
     *
     * <p>Only the description is searched, not the dates or the type box, so
     * that what is matched is the words the user wrote. A keyword is matched
     * anywhere inside it rather than only at a word boundary, which is the
     * simpler rule and the one a user expects from a search box; matching whole
     * words only would mean deciding what separates one word from another, and
     * would make {@code find book} miss {@code bookshop}.
     *
     * <p>Defined here rather than on each subclass because every kind of task
     * has a description and none of them searches it differently, unlike
     * {@link #occursOn(LocalDate)}, where the answer depends on dates only some
     * kinds carry.
     *
     * @param keyword the text being searched for
     * @return true if the description contains it
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
     * <p>Every subclass builds its answer with
     * {@link #buildFileFields(String, String...)}, which is what settles the
     * order once for all three kinds. There is no shared answer to inherit here,
     * because no kind of task can be saved without the type letter that says
     * which kind it is, and only the subclass knows that letter.
     *
     * <p>The fields are returned separately rather than already joined into a
     * line, so that only {@link Storage} knows what separates them and how a
     * field containing that separator is escaped. Were the joining done here, it
     * would have to be kept in step with the code that splits the line back up,
     * in another file.
     *
     * @return a list holding this task's fields, in the order they are saved
     */
    public abstract List<String> toFileFields();

    /**
     * Returns the fields of one save line: the type letter, then the status and
     * the description that every task saves, then any fields this kind of task
     * adds of its own. The status is a digit rather than a box, because the file
     * is read by the program rather than by a person.
     *
     * <p>The extra fields are a varargs parameter because each kind of task has
     * a different number of them: none for a todo, one for a deadline, two for an
     * event. A subclass therefore names its fields in the order they are saved,
     * in one call, rather than assembling a list and pushing its type letter in
     * at the front afterwards.
     *
     * @param type the letter naming the kind of task, such as {@code D}
     * @param extraFields the fields this kind adds after the description, in the
     *        order they are saved, and none for a kind that adds none
     * @return a mutable list holding the whole line's fields, in order
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
