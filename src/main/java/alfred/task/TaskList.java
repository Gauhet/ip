package alfred.task;

import java.util.ArrayList;
import java.util.List;

import alfred.AlfredException;
import alfred.Storage;

/**
 * The tasks the user is keeping, in the order they were added, with the
 * operations that change that order or its contents.
 *
 * <p>The operations that act on a stored task return that task, because each is
 * followed by showing the user what was affected. Those three also check the
 * index they are given, since it comes from a number the user typed and only
 * this class knows how many tasks there are. {@link #get(int)} is the exception
 * and takes its index on trust, being how the list is walked for display.
 */
public class TaskList {
    /** The tasks, in the order they were added, which is the order they are shown and saved in. */
    private final List<Task> tasks;

    /**
     * Creates a list holding the tasks named, in the order they are named, or an
     * empty list if none are named.
     *
     * <p>One varargs constructor covers both, rather than two that have to be
     * kept in step.
     *
     * @param tasks the tasks to start with, in the order they are to be kept
     */
    public TaskList(Task... tasks) {
        this.tasks = new ArrayList<>(List.of(tasks));
    }

    /**
     * Creates a list holding the given tasks, in the order given, which is how a
     * run starts from what the save file held.
     *
     * <p>The tasks are copied, so that whoever supplied them cannot go on
     * changing this list afterwards.
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
     * <p>Checked before the list is asked, because the list would answer with an
     * exception that reads as a fault rather than as a reply to the user.
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
     * Returns the tasks as a plain list, for the sake of {@link Storage}.
     *
     * <p>The list is a copy, so that saving cannot change what is stored.
     *
     * @return the tasks, in the order they are stored
     */
    public List<Task> toList() {
        return List.copyOf(tasks);
    }
}
