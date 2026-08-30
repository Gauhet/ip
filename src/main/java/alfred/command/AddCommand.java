package alfred.command;

import alfred.AlfredException;
import alfred.Parser;
import alfred.Storage;
import alfred.Ui;
import alfred.task.Task;
import alfred.task.TaskList;

/**
 * Stores a new task at the end of the list.
 *
 * <p>One class covers {@code todo}, {@code deadline}, and {@code event}, because
 * the three differ only in the kind of task they build, and that is settled by
 * {@link Parser} before the command is made. What happens afterwards — store
 * it, confirm it, save the list — is the same for all three, and saying it once
 * is what keeps it the same.
 */
public class AddCommand extends Command {
    /** The task to store, already built from what the user typed. */
    private final Task task;

    /**
     * Creates a command that will store one task.
     *
     * @param task the task to add
     */
    public AddCommand(Task task) {
        this.task = task;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws AlfredException {
        tasks.add(task);
        ui.showAdded(task, tasks.size());
        // Saved after the reply, so that a task the user has been told about is
        // on disk before the next command is read.
        storage.save(tasks.toList());
    }
}
