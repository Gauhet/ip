/**
 * One class for each thing the user can ask for, all of them subclasses of
 * {@link alfred.command.Command}.
 *
 * <p>A command is built by {@link alfred.Parser} once the line asking for it has
 * been understood, and holds whatever that line supplied. What it acts on — the
 * task list, the screen, the save file — arrives later, as arguments to
 * {@link alfred.command.Command#execute}.
 *
 * <p>Splitting the commands up this way is what keeps the command loop from
 * growing: it carries one out without knowing which it is.
 *
 * <p>A command that alters the list saves it, so that the file on disk matches
 * what the user has just been told. The commands that only show tasks —
 * {@link alfred.command.ListCommand}, {@link alfred.command.OnCommand}, and
 * {@link alfred.command.FindCommand} — save nothing.
 */
package alfred.command;
