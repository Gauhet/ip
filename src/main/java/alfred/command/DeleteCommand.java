package alfred.command;

import alfred.AlfredException;
import alfred.Storage;
import alfred.Ui;
import alfred.task.Task;
import alfred.task.TaskList;

/**
 * Drops one task from the list.
 *
 * <p>The task is held on to as it is removed, because it is shown to the user
 * afterwards and by then it is no longer in the list to look up.
 */
public class DeleteCommand extends Command {
    /** Which task to remove, counting from 0, as yet unchecked against the list. */
    private final int index;

    /**
     * Creates a command that will remove one task.
     *
     * @param index which task, counting from 0
     */
    public DeleteCommand(int index) {
        this.index = index;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws AlfredException {
        Task removed = tasks.delete(index);
        ui.showRemoved(removed, tasks.size());
        storage.save(tasks.toList());
    }
}
