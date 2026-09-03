package alfred.command;

import alfred.Storage;
import alfred.Ui;
import alfred.task.TaskList;

/**
 * Shows the tasks whose description contains a keyword.
 *
 * <p>Nothing is saved, because searching changes nothing, which is the same
 * reason {@link ListCommand} and {@link OnCommand} save nothing.
 */
public class FindCommand extends Command {
    /** The text being searched for. */
    private final String keyword;

    /**
     * Creates a command that will show the tasks matching one keyword.
     *
     * @param keyword the text to search descriptions for.
     */
    public FindCommand(String keyword) {
        this.keyword = keyword;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showMatchingTasks(tasks, keyword);
    }
}
