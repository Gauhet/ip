import java.util.ArrayList;
import java.util.List;

/**
 * Alfred the Butler: a personal chatbot that keeps a list of tasks, taking one
 * command per line until the user says {@code bye} or the input runs out. The
 * list is saved to disk after every change and read back at startup, so it
 * outlives a single run.
 *
 * <p>This class holds the command loop and nothing else that can be given a
 * home of its own: what the user sees and types is {@link Ui}'s, what a typed
 * line means is {@link Parser}'s, and what is written to and read from disk is
 * {@link Storage}'s.
 */
public class AlfredTheButler {
    /**
     * Greets the user, then handles one command per line until {@code bye} or
     * the end of the input: {@code list}, {@code on <date>},
     * {@code mark <number>}, {@code unmark <number>}, {@code delete <number>},
     * and the three that add a task, {@code todo}, {@code deadline}, and
     * {@code event}. Any other word is refused rather than guessed at.
     *
     * <p>Commands run inside a {@code try} so that a mistake in what was typed
     * becomes an ordinary reply and the loop carries on. Catching in one place
     * lets each method throw its own message without knowing how it is printed.
     *
     * <p>The saved tasks are read back before the first command, and every
     * command that changes the list is followed by a save, so that the file on
     * disk always matches what the user has just been shown.
     */
    public static void main(String[] args) {
        Ui ui = new Ui();
        List<Task> tasks = new ArrayList<>();
        Storage storage = new Storage();
        boolean isRunning = true;

        ui.showWelcome();
        // Loaded in its own try, because this runs before the loop below and so
        // cannot rely on the loop's catch. A file that cannot be read leaves
        // the empty list above in place rather than stopping the program.
        try {
            Storage.LoadResult loaded = storage.load();
            tasks = loaded.tasks();
            // Said only when there is something to say. On a first run there is
            // no file yet, and announcing that nothing came back would be noise.
            if (!tasks.isEmpty()) {
                ui.showLoaded(tasks.size());
            }
            // Warned about separately, and even when nothing else was restored,
            // because the damaged lines are dropped from the file as soon as the
            // list next changes.
            if (loaded.skippedLines() > 0) {
                ui.showSkippedLines(loaded.skippedLines());
            }
        } catch (AlfredException e) {
            ui.showError(e.getMessage());
        }
        while (isRunning) {
            // End of input is treated as `bye`, so that a piped or redirected
            // session that simply runs out of lines finishes the same way a
            // typed one does instead of failing to read a line that is not there.
            if (!ui.hasNextCommand()) {
                break;
            }
            try {
                Parser.ParsedCommand parsed = Parser.parse(ui.readCommand());
                String arguments = parsed.arguments();

                // Left null by the commands that do not add anything, which is
                // what tells the code below there is nothing to store.
                Task added = null;

                // Set by every command that changes the list, so that the save
                // below happens once, in one place, rather than in each arm.
                boolean isListChanged = false;

                // An arrow switch, so no arm can fall through into the next by
                // accident, and so `break` keeps its usual meaning in the loop.
                // No default arm is needed: an unknown keyword has already been
                // refused by the parser, so every value reaching here is listed.
                switch (parsed.command()) {
                case BYE -> isRunning = false;
                case LIST -> ui.showList(tasks);
                case UNMARK -> {
                    int index = Parser.parseTaskIndex(arguments, tasks.size());
                    tasks.get(index).unmarkDone();
                    isListChanged = true;
                    ui.showUnmarked(tasks.get(index));
                }
                case MARK -> {
                    int index = Parser.parseTaskIndex(arguments, tasks.size());
                    tasks.get(index).markDone();
                    isListChanged = true;
                    ui.showMarked(tasks.get(index));
                }
                case DELETE -> {
                    int index = Parser.parseTaskIndex(arguments, tasks.size());
                    Task removed = tasks.remove(index);
                    isListChanged = true;
                    ui.showRemoved(removed, tasks.size());
                }
                case TODO -> added = Parser.parseToDo(arguments);
                case DEADLINE -> added = Parser.parseDeadline(arguments);
                case EVENT -> added = Parser.parseEvent(arguments);
                case ON -> ui.showTasksOn(tasks, Parser.parseOnDate(arguments));
                }

                // Storing and confirming is the same for every kind of task, so
                // it is done once here rather than repeated in each arm above.
                if (added != null) {
                    tasks.add(added);
                    isListChanged = true;
                    ui.showAdded(added, tasks.size());
                }

                // Saved after the reply, so that a command the user has been
                // told succeeded is on disk before the next one is read.
                if (isListChanged) {
                    storage.save(tasks);
                }
            } catch (AlfredException e) {
                ui.showError(e.getMessage());
            } catch (RuntimeException e) {
                // A safety net, not a substitute for handling: everything the
                // user can get wrong is refused above with a message of its own.
                // This one catches the mistakes in this program, so that a bug
                // in one command costs that command rather than the session and
                // the tasks typed since the last save.
                ui.showInternalError(e);
            }
        }
        ui.showFarewell();
    }
}
