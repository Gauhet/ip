package alfred.command;

import alfred.AlfredException;
import alfred.Storage;
import alfred.Ui;
import alfred.task.TaskList;

/**
 * Marks one task as done.
 *
 * <p>Kept apart from {@link UnmarkCommand} rather than the two sharing a class
 * with a flag, because {@code new MarkCommand(index, true)} says nothing about
 * what the flag means at the point it is made.
 */
public class MarkCommand extends Command {
    /** Which task to mark, counting from 0, as yet unchecked against the list. */
    private final int index;

    /**
     * Creates a command that will mark one task as done.
     *
     * @param index which task, counting from 0
     */
    public MarkCommand(int index) {
        this.index = index;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws AlfredException {
        ui.showMarked(tasks.markDone(index));
        storage.save(tasks.toList());
    }
}
