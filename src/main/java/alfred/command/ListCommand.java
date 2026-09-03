package alfred.command;

import alfred.Storage;
import alfred.Ui;
import alfred.task.TaskList;

/**
 * Shows every stored task, in the order they are stored.
 *
 * <p>Nothing is saved, because nothing has changed.
 */
public class ListCommand extends Command {
    /**
     * Creates a command that will show the whole list.
     *
     * <p>Nothing is passed in: the list arrives as an argument to
     * {@link #execute(TaskList, Ui, Storage)}.
     */
    public ListCommand() {
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showList(tasks);
    }
}
