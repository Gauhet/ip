import java.util.ArrayList;
import java.util.List;

/**
 * The tasks the user is keeping, in the order they were added, with the
 * operations that change that order or its contents.
 *
 * <p>Holding the list behind a class rather than passing a bare
 * {@code List<Task>} around is what lets the rest of the program ask for what
 * it wants — mark the third task, delete the first — instead of reaching
 * through the list to do it. The command loop no longer writes
 * {@code tasks.get(index).markDone()}, which is two steps into someone else's
 * data for one idea.
 *
 * <p>The operations that act on a stored task return that task, because every
 * one of them is followed by showing the user what was affected, and having to
 * fetch it again afterwards is how the wrong task ends up shown after a delete.
 *
 * <p>The three operations that act on a stored task check the index they are
 * given, because it comes from a number the user typed and a mistyped one
 * deserves a reply rather than a crash. Only this class can make that check:
 * it is the one that knows how many tasks there are.
 *
 * <p>{@link #get(int)} is the exception, and takes its index on trust. It is
 * how the list is walked for display, one index at a time up to {@link #size()},
 * so a bad index there would be a fault in this program rather than a mistyped
 * command — and making it refusable would put a refusal to handle in the middle
 * of every loop that prints the list.
 */
public class TaskList {
    /** The tasks, in the order they were added, which is the order they are shown and saved in. */
    private final List<Task> tasks;

    /** Creates an empty list, which is what a first run starts from. */
    public TaskList() {
        this.tasks = new ArrayList<>();
    }

    /**
     * Creates a list holding the given tasks, in the order given, which is how
     * a run starts from what the save file held.
     *
     * <p>The tasks are copied rather than kept as the list that was passed in,
     * so that whoever supplied them cannot go on changing this list afterwards.
     *
     * @param tasks the tasks to start with, in the order they are to be kept
     */
    public TaskList(List<Task> tasks) {
        this.tasks = new ArrayList<>(tasks);
    }

    /**
     * Returns how many tasks are stored.
     *
     * @return the number of tasks
     */
    public int size() {
        return tasks.size();
    }

    /**
     * Says whether there are no tasks at all.
     *
     * @return true if nothing is stored
     */
    public boolean isEmpty() {
        return tasks.isEmpty();
    }

    /**
     * Returns the task at one position, counting from 0.
     *
     * @param index which task, from 0 up to one less than {@link #size()}
     * @return the task stored there
     */
    public Task get(int index) {
        return tasks.get(index);
    }

    /**
     * Adds a task to the end of the list.
     *
     * @param task the task to store
     */
    public void add(Task task) {
        tasks.add(task);
    }

    /**
     * Removes the task at one position, closing the gap so that the tasks after
     * it move up a number.
     *
     * @param index which task, counting from 0
     * @return the task that was removed, so it can be shown to the user
     * @throws AlfredException if no task is stored at that index
     */
    public Task delete(int index) throws AlfredException {
        checkIndex(index);
        return tasks.remove(index);
    }

    /**
     * Marks the task at one position as done.
     *
     * @param index which task, counting from 0
     * @return the task, now carrying its new mark
     * @throws AlfredException if no task is stored at that index
     */
    public Task markDone(int index) throws AlfredException {
        checkIndex(index);
        Task task = tasks.get(index);
        task.markDone();
        return task;
    }

    /**
     * Marks the task at one position as not done after all.
     *
     * @param index which task, counting from 0
     * @return the task, now carrying its new mark
     * @throws AlfredException if no task is stored at that index
     */
    public Task unmarkDone(int index) throws AlfredException {
        checkIndex(index);
        Task task = tasks.get(index);
        task.unmarkDone();
        return task;
    }

    /**
     * Refuses an index that names no stored task.
     *
     * <p>Checked before the list is asked, because the list would answer with
     * an exception of its own, and one that reads as a fault in the program
     * rather than as an answer to the person who typed the number.
     *
     * @param index the index to check, counting from 0
     * @throws AlfredException if it falls outside the stored tasks
     */
    private void checkIndex(int index) throws AlfredException {
        if (index < 0 || index >= tasks.size()) {
            throw new AlfredException("There is no such task, sir.");
        }
    }

    /**
     * Returns the tasks as a plain list, for the sake of {@link Storage}, which
     * writes them one to a line.
     *
     * <p>The list is a copy, so that saving cannot change what is stored. That
     * matters more than the copying costs: this is the one place the tasks
     * leave the class, and handing out the real list would undo the point of
     * keeping it private.
     *
     * @return the tasks, in the order they are stored
     */
    public List<Task> toList() {
        return List.copyOf(tasks);
    }
}
