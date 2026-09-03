package alfred.command;

import java.time.LocalDate;

import alfred.Storage;
import alfred.Ui;
import alfred.task.TaskList;

/**
 * Shows the tasks that fall on one day.
 *
 * <p>The day is kept as a {@link LocalDate} rather than as the text that was
 * typed, so that what counts as a date is settled once, while the line is being
 * read.
 */
public class OnCommand extends Command {
    /** The day being asked about. */
    private final LocalDate date;

    /**
     * Creates a command that will show one day's tasks.
     *
     * @param date the day being asked about
     */
    public OnCommand(LocalDate date) {
        this.date = date;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showTasksOn(tasks, date);
    }
}
