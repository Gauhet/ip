package alfred.command;

import alfred.AlfredException;
import alfred.Parser;
import alfred.Storage;
import alfred.Ui;
import alfred.task.TaskList;

/**
 * Something the user has asked for, ready to be carried out.
 *
 * <p>A command is made by {@link Parser} and holds whatever the line supplied.
 * What it acts on — the task list, the screen, the save file — arrives as
 * arguments to {@link #execute(TaskList, Ui, Storage)}.
 *
 * <p>Each kind is a subclass, which lets the command loop carry one out without
 * knowing which it is.
 */
public abstract class Command {
    /**
     * Sets up the part of a command that every kind shares, which is nothing.
     *
     * <p>Protected rather than public, because only a subclass can say what
     * carrying a command out means.
     */
    protected Command() {
    }

    /**
     * Carries out what the user asked for.
     *
     * <p>A command that changes the task list is responsible for saving it, so
     * that the file on disk matches what the user has just been told.
     *
     * @param tasks the tasks to act on.
     * @param ui what to tell the user through.
     * @param storage where to keep the tasks.
     * @throws AlfredException if the command cannot be carried out, with a
     *         message written for the person who typed it.
     */
    public abstract void execute(TaskList tasks, Ui ui, Storage storage) throws AlfredException;

    /**
     * Says whether this command ends the conversation.
     *
     * <p>False for all but one command, so it is answered here and overridden by
     * the one that says otherwise.
     *
     * @return true if nothing more should be read after this command.
     */
    public boolean isExit() {
        return false;
    }
}
