package alfred.command;

import alfred.AlfredException;
import alfred.Parser;
import alfred.Storage;
import alfred.Ui;
import alfred.task.TaskList;

/**
 * Something the user has asked for, ready to be carried out.
 *
 * <p>A command is made by {@link Parser} once the line that asked for it has
 * been understood, and holds whatever that line supplied — a task to add, a
 * number, a date. What it does not hold is anything about the program it will
 * act on: the task list, the screen, and the save file arrive as arguments to
 * {@link #execute(TaskList, Ui, Storage)}, so one command object could be
 * carried out against any of them.
 *
 * <p>Each kind of command is a subclass, which is what lets the command loop
 * carry one out without knowing which it is. The loop used to be a switch over
 * every keyword, so adding a command meant editing it; now a new command is a
 * new subclass and a line in the parser, and the loop does not change.
 */
public abstract class Command {
    /**
     * Sets up the part of a command that every kind shares, which is nothing.
     *
     * <p>Whatever a command needs either arrives in the subclass's own
     * constructor, such as the task an {@link AddCommand} is to add, or is
     * handed to {@link #execute(TaskList, Ui, Storage)} when the time comes.
     * Protected rather than public, because a bare command is not a command:
     * only a subclass can say what carrying it out means.
     */
    protected Command() {
    }

    /**
     * Carries out what the user asked for.
     *
     * <p>A command that changes the task list is responsible for saving it, so
     * that the file on disk matches what the user has just been told.
     *
     * @param tasks the tasks to act on
     * @param ui what to tell the user through
     * @param storage where to keep the tasks
     * @throws AlfredException if the command cannot be carried out, with a
     *         message written for the person who typed it
     */
    public abstract void execute(TaskList tasks, Ui ui, Storage storage) throws AlfredException;

    /**
     * Says whether this command ends the conversation.
     *
     * <p>False for all but one command, so it is answered here rather than in
     * each subclass, and overridden by the one that says otherwise.
     *
     * @return true if nothing more should be read after this command
     */
    public boolean isExit() {
        return false;
    }
}
