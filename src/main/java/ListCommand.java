/**
 * Shows every stored task, in the order they are stored.
 *
 * <p>Nothing is saved, because nothing has changed. A command that alters the
 * list saves it; this one has nothing to save, and that shows in what
 * {@link #execute(TaskList, Ui, Storage)} does rather than in a flag the loop
 * has to read.
 */
public class ListCommand extends Command {
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showList(tasks);
    }
}
