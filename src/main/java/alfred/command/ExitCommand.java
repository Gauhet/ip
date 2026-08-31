package alfred.command;

import alfred.Parser;
import alfred.Storage;
import alfred.Ui;
import alfred.task.TaskList;

/**
 * Says goodbye and ends the conversation.
 *
 * <p>Made by {@link Parser} for a {@code bye}, and by the command loop when the
 * input runs out. Those are the same thing to the user — a session that is
 * over — and treating them as one command is what keeps the parting message
 * from being printed in two places and drifting apart.
 */
public class ExitCommand extends Command {
    /**
     * Creates a command that will say goodbye and end the session.
     *
     * <p>Nothing is passed in, because the parting is the same however the
     * session came to an end.
     */
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
