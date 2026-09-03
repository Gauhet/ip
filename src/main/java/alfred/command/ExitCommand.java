package alfred.command;

import alfred.Parser;
import alfred.Storage;
import alfred.Ui;
import alfred.task.TaskList;

/**
 * Says goodbye and ends the conversation.
 *
 * <p>Made by {@link Parser} for a {@code bye}, and by the command loop when the
 * input runs out. Treating those as one command keeps the parting message from
 * being printed in two places and drifting apart.
 */
public class ExitCommand extends Command {
    /** Creates a command that will say goodbye and end the session. */
    public ExitCommand() {
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showFarewell();
    }

    @Override
    public boolean isExit() {
        return true;
    }
}
