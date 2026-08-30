/**
 * Shows every stored task, in the order they are stored.
 *
 * <p>Nothing is saved, because nothing has changed. That is the whole
 * difference between this command and the ones above it, and it is visible
 * here rather than in a flag the loop has to read.
 */
public class ListCommand extends Command {
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showList(tasks);
    }
}
