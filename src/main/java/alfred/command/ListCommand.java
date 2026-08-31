package alfred.command;

import alfred.Storage;
import alfred.Ui;
import alfred.task.TaskList;

/**
 * Shows every stored task, in the order they are stored.
 *
 * <p>Nothing is saved, because nothing has changed. A command that alters the
 * list saves it; this one has nothing to save, and that shows in what
 * {@link #execute(TaskList, Ui, Storage)} does rather than in a flag the loop
 * has to read.
 */
public class ListCommand extends Command {
    /**
     * Creates a command that will show the whole list.
     *
     * <p>Nothing is passed in: which tasks to show is not settled when the
     * command is made but when it is carried out, since the list arrives as an
     * argument to {@link #execute(TaskList, Ui, Storage)}.
     */
    public ListCommand() {
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showList(tasks);
    }
}
